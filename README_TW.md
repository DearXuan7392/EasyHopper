# EasyHopper (輕鬆漏斗)

<p align="center">
  <a href="./README.md"><img src="https://raw.githubusercontent.com/lipis/flag-icons/main/flags/4x3/us.svg" width="18" valign="middle"> English</a> | 
  <a href="./README_CN.md"><img src="https://raw.githubusercontent.com/lipis/flag-icons/main/flags/4x3/cn.svg" width="18" valign="middle"> 简体中文</a> | 
  <img src="https://raw.githubusercontent.com/lipis/flag-icons/main/flags/4x3/tw.svg" width="18" valign="middle"> <b>繁體中文</b> | 
  <a href="./README_DE.md"><img src="https://raw.githubusercontent.com/lipis/flag-icons/main/flags/4x3/de.svg" width="18" valign="middle"> Deutsch</a>
</p>

---

EasyHopper 在原版漏斗的基礎上提供分類功能，而無需引入任何額外方塊。因此可以在任何時候移除該 Mod，而不會對存檔造成任何影響。

![截圖](https://cdn.dearxuan.com/project/easyhopper/screen_zh.png)

## 回報問題

由于工作原因，難以花費大量時間進行測試，如果出現任何問題，
請在 [https://github.com/DearXuan7392/EasyHopper/issues](https://github.com/DearXuan7392/EasyHopper/issues) 回報。請附帶
Mod 版本和遊戲版本，並解釋你進行了哪些操作、出現了什麼問題。

## 下載

- 從 [Modrinth](https://modrinth.com/mod/easy-hopper) 下載 (推薦)
- 從 [CurseForge](https://www.curseforge.com/minecraft/mc-mods/easyhopper) 下載 (更新較慢)

## 啟用功能

### 任何情況

- 在**任何情況**下，你都可以在遊戲目錄下 ``./config/easyhopper.yaml`` 裡直接編輯內容，並在重新進入世界後生效。
- 在**單人遊戲**中，你可以透過圖形介面來修改設定，但需要安裝對應的元件，請查看後續說明。
- 在**區域網路連線**中，房主可以任意修改設定。
- 在**伺服器**中 (包括區域網路連線裡的其他玩家)，只有在設定檔中啟用了**管理員修改**，且擁有**管理員**身份的情況下才能修改。

### 對於 Fabric

想要顯示圖形介面，你必須首先下載下列模組：

- [Mod Menu](https://modrinth.com/mod/modmenu)，用於管理模組，並新增設定按鈕。
- 從 [Cloth Config API](https://modrinth.com/mod/cloth-config) (推薦) 或 [YACL](https://modrinth.com/mod/yacl) 中任選一個下載，
  用於提供圖形介面。如果同時存在二個模組，則優先使用 Cloth Config API。

### 對於 NeoForge

- 從 [Cloth Config API](https://modrinth.com/mod/cloth-config) (推薦) 或 [YACL](https://modrinth.com/mod/yacl) 中任選一個下載，
  用於提供圖形介面。如果同時存在二個模組，則優先使用 Cloth Config API。

## 功能

### 傳送速度

你可以修改漏斗的傳送冷卻，以及每次傳送的物品數量，加快或減緩物品流動速度。

### 偵測冷卻

每次嘗試吸取物品，都會使自己進入冷卻，從而避免頻繁偵測掉落物。在大量漏斗的情況下，能夠有效提升效能，但容易導致部分紅石機械出錯，
例如高速熔爐。

### 漏斗分類

將漏斗的第 5 格作為分類物品格，只有相同類型的物品才能進入漏斗或被主動輸出。如果玩家強行放入錯誤的物品，那麼該物品會一直留在漏斗中。
但其他漏斗仍然可以從下方吸取物品。

### 效能最佳化 (``<=1.20.4``)

當漏斗上方為完整方塊時，停用漏斗的掉落物偵測，以提升效能。

自 ``1.20.5`` 版本起，官方已引入該最佳化，無需使用此功能。
