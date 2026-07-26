# EasyHopper

<p align="center">
  <a href="./README.md"><img src="https://raw.githubusercontent.com/lipis/flag-icons/main/flags/4x3/us.svg" width="18" valign="middle"> English</a> | 
  <a href="./README_CN.md"><img src="https://raw.githubusercontent.com/lipis/flag-icons/main/flags/4x3/cn.svg" width="18" valign="middle"> 简体中文</a> | 
  <a href="./README_TW.md"><img src="https://raw.githubusercontent.com/lipis/flag-icons/main/flags/4x3/tw.svg" width="18" valign="middle"> 繁體中文</a> | 
  <img src="https://raw.githubusercontent.com/lipis/flag-icons/main/flags/4x3/de.svg" width="18" valign="middle"> <b>Deutsch</b>
</p>

---

EasyHopper erweitert Vanillatrichter um eine Filterfunktion, ohne neue Blöcke einzuführen. Da keine benutzerdefinierten
Blöcke hinzugefügt werden, kann die Mod jederzeit sicher deinstalliert werden, ohne deine Welt zu beeinträchtigen.

![Screenshot](https://cdn.dearxuan.com/project/easyhopper/screen_us.png)

## Probleme melden

Aufgrund beruflicher Verpflichtungen ist es schwierig, umfangreiche Tests für jedes Szenario durchzuführen. Wenn du auf
Probleme oder Fehler stößt, melde diese bitte auf [GitHub Issues](https://github.com/DearXuan7392/EasyHopper/issues).

Bitte gib bei einer Fehlermeldung Folgendes an:

- Mod-Version und Minecraft-Version.
- Schritte zur Reproduktion des Problems und eine Beschreibung des Fehlers.

## Downloads

- Download über [Modrinth](https://modrinth.com/mod/easy-hopper) (Empfohlen)
- Download über [CurseForge](https://www.curseforge.com/minecraft/mc-mods/easyhopper) (Langsamere Updates)

## Konfiguration & Einrichten der Funktionen

### Zugriff auf die Konfiguration

- **Jedes Szenario:** Du kannst die Datei `./config/easyhopper.yaml` direkt in deinem Spielverzeichnis bearbeiten. Die
  Änderungen werden nach dem erneuten Betreten der Welt wirksam.
- **Einzelspieler:** Du kannst die Einstellungen über die Benutzeroberfläche im Spiel anpassen (erfordert entsprechende
  Abhängigkeiten für die Benutzeroberfläche, siehe unten).
- **LAN-Host:** Der Host kann die Konfiguration im Spiel frei anpassen.
- **Dedicated Server:** Spieler auf einem Server (einschließlich anderer Spieler im LAN) können die Konfiguration im
  Spiel nur ändern, wenn **Operator-Änderungen** (`ALLOW_OP_MODIFY`) in der Konfigurationsdatei des Servers aktiviert
  sind UND der Spieler über **Operator-Rechte (OP)** verfügt.

### Abhängigkeiten für die grafische Benutzeroberfläche (GUI)

#### Für Fabric

Um das Konfigurationsmenü anzuzeigen, musst du Folgendes installieren:

- [Mod Menu](https://modrinth.com/mod/modmenu): Fügt eine Konfigurationsschaltfläche in der Mod-Liste hinzu.
- **Entweder** [Cloth Config API](https://modrinth.com/mod/cloth-config) *(Empfohlen)* **oder
  ** [YACL](https://modrinth.com/mod/yacl): Stellt die grafische Benutzeroberfläche bereit. Wenn beide installiert sind,
  wird bevorzugt Cloth Config API verwendet.

#### Für NeoForge

Installiere **entweder**:

- [Cloth Config API](https://modrinth.com/mod/cloth-config) *(Empfohlen)* **oder
  ** [YACL](https://modrinth.com/mod/yacl). Wenn beide installiert sind, wird bevorzugt Cloth Config API verwendet.

## Funktionen

### Anpassung der Transfergeschwindigkeit

Passe die Abklingzeit (Cooldown) des Trichters sowie die Anzahl der pro Durchgang übertragenen Gegenstände an, um den
Gegenstandsfluss zu beschleunigen oder zu verlangsamen.

### Abklingzeit für die Gegenstandssuche

Fügt eine Abklingzeit hinzu, wenn ein Trichter versucht, nach herumliegenden Gegenständen zu suchen oder diese
einzusaugen. Dies reduziert Serververzögerungen (Lag) bei einer großen Anzahl von Trichtern erheblich. *Hinweis: Dies
kann zeitkritische Redstone-Maschinen wie Hochgeschwindigkeits-Schmelzanlagen beeinträchtigen.*

### Trichter-Gegenstandsfilter

Verwendet den 5. (letzten) Slot eines Trichters als Filter-Slot. Nur Gegenstände desselben Typs wie im 5. Slot können in
den Trichter gelangen oder ausgegeben werden. Wenn ein Spieler manuell einen falschen Gegenstand hineinlegt, bleibt
dieser im Trichter liegen, aber darunter liegende Trichter können weiterhin passende Gegenstände absaugen.

### Leistungsoptimierung (`<= 1.20.4`)

Deaktiviert die Überprüfung auf herumliegende Gegenstände, wenn sich direkt über dem Trichter ein fester Block oder ein
Behälter befindet, um die Serverleistung zu verbessern.

> **Hinweis:** Ab Version `1.20.5` hat Mojang diese Optimierung nativ in den Minecraft-Code integriert, weshalb diese
> Option in neueren Versionen nicht mehr erforderlich ist.
