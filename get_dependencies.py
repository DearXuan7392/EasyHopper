import sys
import requests

def get_latest_neoform(mc_version):
    """Get the latest NeoForm version from NeoForged Maven"""
    url = (
        "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoform"
    )
    try:
        response = requests.get(url, timeout=10)
        if response.status_code == 200:
            versions = response.json().get("versions", [])
            # Filter NeoForm versions that start with the corresponding MC version
            matching = [v for v in versions if v.startswith(mc_version)]
            return matching[-1] if matching else "No matching version found"
    except Exception as e:
        return f"Failed to retrieve ({e})"
    return "No matching version found"

def get_latest_neoforge(mc_version):
    """Get the latest NeoForge version from NeoForged Maven"""
    url = (
        "https://maven.neoforged.net/api/maven/versions/releases/net/neoforged/neoforge"
    )
    try:
        response = requests.get(url, timeout=10)
        if response.status_code == 200:
            versions = response.json().get("versions", [])
            matching = [v for v in versions if v.startswith(mc_version)]
            return matching[-1] if matching else "No matching version found"
    except Exception as e:
        return f"Failed to retrieve ({e})"
    return "No matching version found"

def get_latest_fabric_api(mc_version):
    """Get the latest Fabric API version from Fabric Meta API"""
    url = "https://meta.fabricmc.net/v2/versions/fabric-api"
    try:
        response = requests.get(url, timeout=10)
        if response.status_code == 200:
            data = response.json()
            for item in data:
                # Check the list of MC versions supported by this Fabric API version
                game_versions = item.get("gameVersions", [])
                if mc_version in game_versions:
                    return item.get("version")
    except Exception as e:
        return f"Failed to retrieve ({e})"
    return "No matching version found"

def get_latest_fabric_loader():
    """Get the latest stable Fabric Loader version from Fabric Meta API"""
    url = "https://meta.fabricmc.net/v2/versions/loader"
    try:
        response = requests.get(url, timeout=10)
        if response.status_code == 200:
            data = response.json()
            for item in data:
                if item.get("stable", False):
                    return item.get("version")
            return data[0].get("version") if data else "Unknown"
    except Exception as e:
        return f"Failed to retrieve ({e})"
    return "No matching version found"

def fetch_dependencies(mc_version):
    print(f"\nFetching latest dependency information for Minecraft {mc_version}, please wait...\n")

    neoform = get_latest_neoform(mc_version)
    neoforge = get_latest_neoforge(mc_version)
    fabric_api = get_latest_fabric_api(mc_version)
    fabric_loader = get_latest_fabric_loader()

    # Format output as gradle.properties configuration content
    result = f"""# =========================================
Auto-generated dependency configuration (Minecraft {mc_version})
=========================================
minecraft_version={mc_version}

# NeoForm Version
neo_form_version={neoform}

# Fabric
fabric_version={fabric_api}
fabric_loader_version={fabric_loader}

# NeoForge
neoforge_version={neoforge}
"""
    print(result)

if __name__ == "__main__":
    # Support command-line argument input or interactive input
    if len(sys.argv) > 1:
        mc_ver = sys.argv[1]
    else:
        mc_ver = input("Please enter Minecraft version (e.g., 1.20.4 or 26.2): ").strip()

    if mc_ver:
        fetch_dependencies(mc_ver)
    else:
        print("The entered version number cannot be empty!")