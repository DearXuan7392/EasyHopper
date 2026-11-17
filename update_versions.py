#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
根据给定的 Minecraft 游戏版本, 自动获取 gradle.properties 中各依赖项的最新兼容版本。

用法:
    python update_versions.py 26.3            # 默认: 只获取并对比, 不写入文件
    python update_versions.py 26.3 --write    # 获取并覆盖 gradle.properties
    python update_versions.py                 # 不传版本时, 读取文件中当前的 minecraft_version

只依赖 Python 标准库, 无需 pip install。

各字段的数据来源 (与 gradle.properties 中的注释一致):
    minecraft_version_range : 由游戏版本推导 [当前版本, 下一版本)
    neo_form_version        : https://github.com/neoforged/NeoForm 的 tag
    fabric_version          : https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml
    fabric_loader_version   : https://meta.fabricmc.net/v2/versions/loader (最新 stable)
    neoforge_version        : https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge
    yacl_version            : https://api.modrinth.com/v2/project/yacl/version       (必须同时支持 fabric + neoforge)
    cloth_config_version    : https://api.modrinth.com/v2/project/cloth-config/version (必须同时支持 fabric + neoforge)
    modmenu_version         : https://api.modrinth.com/v2/project/modmenu/version    (fabric, 仅 release)
    snakeyaml_version       : https://repo1.maven.org/maven2/org/yaml/snakeyaml/maven-metadata.xml

注: neoforge_loader_version_range 等固定字段不会被修改。
"""

import argparse
import json
import re
import sys
import urllib.request
from pathlib import Path

USER_AGENT = "EasyHopper-version-updater/1.0 (+https://github.com/DearXuan/EasyHopper)"
DEFAULT_PROPERTIES = Path(__file__).resolve().parent / "gradle.properties"

REQUEST_TIMEOUT = 45


# --------------------------------------------------------------------------- #
# 通用工具
# --------------------------------------------------------------------------- #

def http_get(url: str, accept: str = "application/json, application/xml, text/xml, */*") -> str:
    """发送 GET 请求并返回文本。"""
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT, "Accept": accept})
    with urllib.request.urlopen(req, timeout=REQUEST_TIMEOUT) as resp:
        return resp.read().decode("utf-8", errors="replace")


def http_get_json(url: str):
    return json.loads(http_get(url))


def natural_key(text: str):
    """把版本字符串拆成可比较的元组, 例如 0.166.2+26.3 -> (0, 166, 2, 26, 3)。"""
    return tuple(int(p) if p.isdigit() else p for p in re.split(r"(\d+)", text) if p != "")


def read_properties(path: Path):
    """读取 properties, 保留原始行与换行符。"""
    with path.open("r", encoding="utf-8", newline="") as fh:
        lines = fh.readlines()
    values = {}
    for line in lines:
        m = re.match(r"^\s*([A-Za-z0-9_.\-]+)\s*=\s*(.*?)\s*$", line.rstrip("\r\n"))
        if m:
            values[m.group(1)] = m.group(2)
    return lines, values


def next_minecraft_version(version: str) -> str:
    """26.3 -> 26.4 ; 26.1.2 -> 26.1.3。"""
    parts = version.split(".")
    try:
        parts[-1] = str(int(parts[-1]) + 1)
    except ValueError:
        parts[-1] = parts[-1] + ".1"
    return ".".join(parts)


# --------------------------------------------------------------------------- #
# 各数据源
# --------------------------------------------------------------------------- #

def fetch_minecraft_version_range(game: str) -> str:
    return "[%s, %s)" % (game, next_minecraft_version(game))


def fetch_neoform_version(game: str) -> str:
    """NeoForm 版本来自 GitHub tag: v26.3-1 -> 26.3-1。"""
    tags = http_get_json(
        "https://api.github.com/repos/neoforged/NeoForm/tags?per_page=100"
    )
    pattern = re.compile(r"^v" + re.escape(game) + r"-(\d+)$")
    candidates = []
    for tag in tags:
        m = pattern.match(tag.get("name", ""))
        if m:
            candidates.append((int(m.group(1)), tag["name"][1:]))
    if not candidates:
        raise RuntimeError("未找到 Minecraft %s 对应的 NeoForm 版本" % game)
    return max(candidates)[1]


def fetch_fabric_api_version(game: str) -> str:
    xml = http_get(
        "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml",
        accept="application/xml, text/xml, */*",
    )
    versions = re.findall(r"<version>([^<]+)</version>", xml)
    candidates = [v for v in versions if v.endswith("+" + game)]
    if not candidates:
        raise RuntimeError("未找到 Minecraft %s 对应的 Fabric API 版本" % game)
    return max(candidates, key=natural_key)


def fetch_fabric_loader_version(_game: str) -> str:
    versions = http_get_json("https://meta.fabricmc.net/v2/versions/loader")
    stable = [v["version"] for v in versions if v.get("stable")]
    if not stable:
        raise RuntimeError("未找到稳定的 Fabric Loader 版本")
    return stable[0]  # API 按新到旧排序


def fetch_neoforge_version(game: str) -> str:
    """从 NeoForged Maven 的版本列表取该游戏版本下最新的构建。"""
    url = "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge"
    try:
        data = http_get_json(url)
        versions = data["versions"] if isinstance(data, dict) else data
    except Exception:
        # 备用: 直接读取 maven-metadata.xml
        xml = http_get(
            "https://maven.neoforged.net/net/neoforged/neoforge/maven-metadata.xml",
            accept="application/xml, text/xml, */*",
        )
        versions = re.findall(r"<version>([^<]+)</version>", xml)

    # NeoForge 版本形如 26.3.0.110 或 26.2.0.28-beta
    prefix = game + "."
    candidates = []
    for raw in versions:
        if not raw.startswith(prefix):
            continue
        stable = "-beta" not in raw and "-alpha" not in raw and "-" not in raw
        core = raw.split("-")[0]
        numbers = tuple(int(p) for p in core.split(".") if p.isdigit())
        candidates.append((stable, numbers, raw))
    if not candidates:
        raise RuntimeError("未找到 Minecraft %s 对应的 NeoForge 版本" % game)
    # 优先正式版 (无 -beta), 其次按数字版本最大
    candidates.sort(key=lambda x: (x[0], x[1]))
    return candidates[-1][2]


_LOADER_SUFFIX = re.compile(r"[-+](fabric|neoforge|quilt|forge)$", re.IGNORECASE)


def _modrinth_base(version_number: str) -> str:
    """3.9.7+26.3-fabric -> 3.9.7+26.3 ; 26.3.159+fabric -> 26.3.159"""
    return _LOADER_SUFFIX.sub("", version_number)


def fetch_modrinth_version(project: str, game: str, *, dual_loader: bool,
                           loaders=None, release_only=True) -> str:
    """
    从 Modrinth 获取指定游戏版本下最新的兼容版本。

    dual_loader=True 时, 要求同一个基础版本同时存在 fabric 与 neoforge 两种文件。
    """
    versions = http_get_json(
        "https://api.modrinth.com/v2/project/%s/version" % project
    )
    groups = {}
    for item in versions:
        if game not in item.get("game_versions", []):
            continue
        if release_only and item.get("version_type") != "release":
            continue
        item_loaders = {l.lower() for l in item.get("loaders", [])}
        if loaders and not item_loaders.intersection({l.lower() for l in loaders}):
            continue
        base = _modrinth_base(item["version_number"])
        info = groups.setdefault(base, {"loaders": set(), "types": set()})
        info["loaders"].update(item_loaders)
        info["types"].add(item.get("version_type"))

    if dual_loader:
        groups = {
            base: info for base, info in groups.items()
            if {"fabric", "neoforge"}.issubset(info["loaders"])
        }
    if not groups:
        raise RuntimeError(
            "Modrinth 项目 %s 没有 Minecraft %s 的%s版本"
            % (project, game, "双端" if dual_loader else "兼容")
        )
    return max(groups.keys(), key=natural_key)


def fetch_snakeyaml_version(_game: str) -> str:
    xml = http_get(
        "https://repo1.maven.org/maven2/org/yaml/snakeyaml/maven-metadata.xml",
        accept="application/xml, text/xml, */*",
    )
    m = re.search(r"<release>([^<]+)</release>", xml)
    if not m:
        raise RuntimeError("无法解析 snakeyaml 的最新版本")
    return m.group(1)


# --------------------------------------------------------------------------- #
# 主流程
# --------------------------------------------------------------------------- #

def build_fetchers(game: str):
    return {
        "minecraft_version": lambda: game,
        "minecraft_version_range": lambda: fetch_minecraft_version_range(game),
        "neo_form_version": lambda: fetch_neoform_version(game),
        "fabric_version": lambda: fetch_fabric_api_version(game),
        "fabric_loader_version": lambda: fetch_fabric_loader_version(game),
        "neoforge_version": lambda: fetch_neoforge_version(game),
        "yacl_version": lambda: fetch_modrinth_version("yacl", game, dual_loader=True),
        "cloth_config_version": lambda: fetch_modrinth_version(
            "cloth-config", game, dual_loader=True
        ),
        "modmenu_version": lambda: fetch_modrinth_version(
            "modmenu", game, dual_loader=False, loaders=["fabric", "quilt"]
        ),
        "snakeyaml_version": lambda: fetch_snakeyaml_version(game),
    }


def write_properties(path: Path, lines, new_values: dict) -> None:
    out = []
    for line in lines:
        m = re.match(r"^(\s*)([A-Za-z0-9_.\-]+)(\s*=\s*)(.*?)(\s*)$", line.rstrip("\r\n"))
        ending = "\r\n" if line.endswith("\r\n") else ("\n" if line.endswith("\n") else "")
        if m and m.group(2) in new_values:
            out.append("%s%s%s%s%s" % (
                m.group(1), m.group(2), m.group(3), new_values[m.group(2)], ending
            ))
        else:
            out.append(line)
    path.write_text("".join(out), encoding="utf-8", newline="")


def main() -> int:
    parser = argparse.ArgumentParser(description="自动更新 EasyHopper 的 gradle.properties 依赖版本")
    parser.add_argument("game_version", nargs="?", help="Minecraft 版本, 例如 26.3")
    parser.add_argument("--write", action="store_true", help="将结果写入 gradle.properties (默认仅对比)")
    parser.add_argument("--file", type=Path, default=DEFAULT_PROPERTIES, help="properties 文件路径")
    args = parser.parse_args()

    path = args.file
    if not path.exists():
        print("错误: 找不到文件 %s" % path, file=sys.stderr)
        return 2

    lines, current = read_properties(path)
    game = args.game_version or current.get("minecraft_version")
    if not game:
        print("错误: 未提供游戏版本, 且文件中没有 minecraft_version", file=sys.stderr)
        return 2

    print("目标游戏版本: %s" % game)
    print("配置文件: %s\n" % path)

    results = {}
    errors = {}
    for key, fetch in build_fetchers(game).items():
        try:
            results[key] = fetch()
        except Exception as exc:  # noqa: BLE001 - 逐字段容错
            errors[key] = "%s: %s" % (type(exc).__name__, exc)
            results[key] = None

    # 输出对比表
    width = max(len(k) for k in results)
    print("%-*s | %-22s | %-22s | %s" % (width, "字段", "当前文件", "获取结果", "状态"))
    print("-" * (width + 62))
    diffs = 0
    for key, fetched in results.items():
        old = current.get(key, "<不存在>")
        if fetched is None:
            status = "获取失败"
        elif old == fetched:
            status = "一致"
        else:
            status = "不同"
            diffs += 1
        print("%-*s | %-22s | %-22s | %s" % (
            width, key, old, fetched if fetched is not None else "(见下方错误)", status
        ))

    if errors:
        print("\n获取失败的字段:")
        for key, err in errors.items():
            print("  - %s: %s" % (key, err))

    print("\n共 %d 个字段与当前文件不同。" % diffs)

    if args.write:
        if errors:
            print("存在获取失败的字段, 为避免写入不完整数据, 已取消写入。", file=sys.stderr)
            return 1
        if diffs == 0:
            print("所有字段均已一致, 无需写入。")
            return 0
        write_properties(path, lines, results)
        print("已更新: %s" % path)
    else:
        print("当前为对比模式 (未写入文件)。确认无误后加 --write 参数执行覆盖。")

    return 0


if __name__ == "__main__":
    sys.exit(main())
