# 🎯 FozmineTimedItem

**A lightweight, high-performance Minecraft Spigot/Paper plugin for managing timed items with full integration for popular item plugins.**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.16%2B-green)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-8%2B-orange)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📖 Overview

**FozmineTimedItem** allows server administrators to give players items that expire after a specified duration. When an item expires, it can be automatically replaced with another item or simply removed from the player's inventory.

The plugin is designed to be **highly optimized**, **production-ready**, and **fully integrated** with popular item plugins like MMOItems, ItemsAdder, Nexo, Oraxen, and CraftEngine.

---

## ✨ Features

- **Multi-Platform Item Support** – Works seamlessly with:
    - [MMOItems](https://www.spigotmc.org/resources/mmoitems.39267/) – MMOItems items
    - [ItemsAdder](https://www.spigotmc.org/resources/itemsadder.73355/) – Custom items and textures
    - [Nexo](https://www.spigotmc.org/resources/nexo.103398/) – Nexo items
    - [Oraxen](https://www.spigotmc.org/resources/oraxen.72481/) – Oraxen custom items
    - [CraftEngine](https://www.spigotmc.org/resources/craftengine.107848/) – CraftEngine custom items
    - **Vanilla** – Minecraft items with CustomModelData support

- **Precise Time Management**
    - Time precision down to **milliseconds** (no rounding)
    - Support for units: `s` (seconds), `m` (minutes), `h` (hours), `d` (days)
    - Plain numbers default to seconds (e.g., `3600` = 1 hour)
    - Maximum duration capped at **3650 days** to prevent overflow

- **Smart Expiration Handling**
    - Items can be **replaced** with another item or **removed** on expiration
    - Replacement configuration per item type
    - **Zero-TPS impact** – Distributed scheduler checks players gradually

- **Real-time Detection**
    - Scheduler runs periodically to check all online players
    - **Event-driven** detection: InventoryClick, Interact, ItemHeld events instantly catch expired items

- **Clean Lore Display**
    - Shows remaining time in `HH:mm:ss` format (e.g., `01:30:45`)
    - Supports **HEX colors** (`&#RRGGBB`)
    - Automatically updates lore when players interact with items

- **High Performance**
    - Only checks a configurable number of players per tick (default: 5)
    - Optimized to handle large servers with 100+ players without TPS drops
    - Uses **PersistentDataContainer** (PDC) for storing data – no legacy NBT libraries

---

## 📦 Installation

1. **Download** the latest `FozmineTimedItem.jar` from the [Releases](https://github.com/phantam/FozmineTimedItem/releases) page.
2. **Place** the JAR file into your server's `plugins/` folder.
3. **Restart** your server (or use `/reload` if you must, but restart is recommended).
4. **Configure** the `config.yml` file (see [Configuration](#-configuration) section).
5. **Reload** the plugin with `/ti reload` (or restart).

---

## ⚙️ Configuration

### `config.yml` Structure

```yaml
# ═══════════════════════════════════════════════════════════════════
#                    FozmineTimedItem - Config.yml
# ═══════════════════════════════════════════════════════════════════

# 1. DISPLAY SETTINGS
# ───────────────────────────────────────────────────────────────────

# Lore format for remaining time
# %value% - Remaining time (HH:mm:ss format)
expiry-period-format: '&#FFAA00⏳ &fExpires in: &7%value%'

# Message when an item is given successfully
# %amount% - Stack size
# %item%   - Display name of the item
# %player% - Receiver's name
give-message: '&aGave &e%amount% &f%item% &ato &e%player%'

# Time unit formatting
# %d - Numeric value
unit-format:
  second: '%d s'
  minute: '%d m'
  hour: '%d h'
  day: '%d d'
  seconds: '%d s'
  minutes: '%d m'
  hours: '%d h'
  days: '%d d'

# 2. SCHEDULER SETTINGS
# ───────────────────────────────────────────────────────────────────

# How often to check for expired items (in ticks, 20 ticks = 1 second)
#   - Distributed to avoid lag
#   - Recommended: 5-20
item-check-interval: 5

# How many players to check per tick
#   - Lower = less lag, slower detection
#   - Higher = more lag, faster detection
#   - Recommended: 3-10
players-per-tick: 5

# Date format (internal reference only)
date-format: HH:mm:ss

# 3. EXPIRATION REPLACEMENT
# ───────────────────────────────────────────────────────────────────

expired-item-replace:
  # Enable/disable replacement globally
  enable: true

  # Define replacement keys
  # Format: KEY: "SOURCE:VALUE"
  #
  # Supported SOURCEs:
  #   VANILLA   -> VANILLA:MATERIAL[:CUSTOM_MODEL_DATA]
  #   MMOITEM   -> MMOITEM:TYPE:ID
  #   ITEMSADDER -> ITEMSADDER:NAMESPACE:ID  or  ITEMSADDER:ID
  #   NEXO      -> NEXO:ID
  #   ORAXEN    -> ORAXEN:ID
  #   CRAFTENGINE -> CRAFTENGINE:NAMESPACE:PATH
  END: "VANILLA:EMERALD"
  SWORD_REPLACE: "MMOITEM:SWORD:WOOD_SWORD"

# 4. ITEM MAPPINGS (used with /ti give)
# ───────────────────────────────────────────────────────────────────

# Format:
#   <CUSTOM_ID>:
#     item: "SOURCE:VALUE"
#     expired: <REPLACEMENT_KEY>   # Optional – if omitted, uses CUSTOM_ID as key
item-mappings:
  # MMOItems example
  SWORD:
    item: "MMOITEM:SWORD:IRON_SWORD"
    expired: "SWORD_REPLACE"

  # Vanilla example
  DIAMOND:
    item: "VANILLA:DIAMOND"

  # Vanilla with CustomModelData
  CUSTOM_DIAMOND:
    item: "VANILLA:DIAMOND:173"

  # ItemsAdder example
  MY_ITEM:
    item: "ITEMSADDER:MY_NAMESPACE:MY_ITEM"
    expired: "ITEMSADDER_REPLACE"

  # Nexo example
  NEXO_SWORD:
    item: "NEXO:my_sword_id"
    expired: "NEXO_REPLACE"

  # Oraxen example
  ORAXEN_ITEM:
    item: "ORAXEN:my_custom_item"
    expired: "ORAXEN_REPLACE"

  # CraftEngine example
  CRAFT_ITEM:
    item: "CRAFTENGINE:myplugin:custom_sword"
    expired: "CRAFTENGINE_REPLACE"
```

---

## 🎮 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/ti give <player> <id> <amount> <time>` | `fozminetimeditem.give` | Give a timed item to a player |
| `/ti reload` | `fozminetimeditem.reload` | Reload the plugin configuration |

### Time Format

| Format | Meaning | Example |
|--------|---------|---------|
| `30s` | 30 seconds | `/ti give Player1 DIAMOND 1 30s` |
| `5m` | 5 minutes | `/ti give Player1 SWORD 1 5m` |
| `2h` | 2 hours | `/ti give Player1 DIAMOND 64 2h` |
| `1d` | 1 day | `/ti give Player1 SWORD 1 1d` |
| `3600` | 3600 seconds = 1 hour | `/ti give Player1 DIAMOND 1 3600` |

### Usage Examples

```bash
# Give 1 MMOItems sword expiring in 30 minutes
/ti give Player1 SWORD 1 30m

# Give 64 diamonds expiring in 1 day
/ti give Player1 DIAMOND 64 1d

# Give 5 ItemsAdder items expiring in 2 hours
/ti give Player1 MY_ITEM 5 2h

# Give 1 Nexo item expiring in 30 seconds
/ti give Player1 NEXO_SWORD 1 30s
```

---

## 🔧 Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `fozminetimeditem.give` | `op` | Allows use of `/ti give` |
| `fozminetimeditem.reload` | `op` | Allows use of `/ti reload` |

---

## 🛠️ How It Works

### Architecture

1. **Item Creation** – When `/ti give` is executed, the plugin:
    - Resolves the item definition using the appropriate resolver (MMOItems, ItemsAdder, etc.)
    - Stores the expiration timestamp in **PersistentDataContainer** (PDC)
    - Adds a lore line showing the remaining time

2. **Expiration Detection**
    - **Scheduler**: Runs periodically (configurable interval) and checks a limited number of players per tick to avoid lag
    - **Event Listeners**: Detect expired items instantly when players click, hold, or interact with them

3. **Expiration Handling**
    - If replacement is enabled and configured, the expired item is replaced with the specified item
    - Otherwise, the item is removed from the player's inventory

### Performance Optimizations

- **Distributed player checking** – Only checks 5 players per tick by default
- **Event-driven validation** – Catches expiration on interaction without waiting for the scheduler
- **No reflection in core logic** – Clean, direct API calls
- **Lightweight dependencies** – Only uses what's necessary

---

## 🔌 Integration Support

| Plugin | Support Level | Notes |
|--------|---------------|-------|
| MMOItems | ✅ Full | Works with any type and ID |
| ItemsAdder | ✅ Full | Supports namespace:ID and plain ID |
| Nexo | ✅ Full | Uses NexoAPI directly |
| Oraxen | ✅ Full | Uses OraxenItems API |
| CraftEngine | ✅ Full | Uses CraftEngine API (with reflection fallback) |
| Vanilla | ✅ Full | Supports CustomModelData |

---

## 🐛 Reporting Issues

If you encounter any issues, please:

1. Check the console logs for error messages.
2. Ensure your `config.yml` is properly formatted (no tabs, correct indentation).
3. Verify that the required item plugins are installed and enabled.
4. Open an issue on [GitHub Issues](https://github.com/phantam/FozmineTimedItem/issues) with:
    - Server version (Paper/Spigot and version)
    - Plugin versions (MMOItems, ItemsAdder, etc.)
    - Full error log (if any)
    - Steps to reproduce the issue

---

## 📚 License

This project is licensed under the **MIT License** – see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**phantam** – [GitHub](https://github.com/TKILLLL)

---

## 🙏 Acknowledgements

- [PaperMC](https://papermc.io) – The best Minecraft server software
- [MMOItems](https://www.spigotmc.org/resources/mmoitems.39267/) – For providing a powerful item API
- All the plugin developers whose APIs we integrate with ❤️

---

## 📦 Building from Source

```bash
git clone https://github.com/phantam/FozmineTimedItem.git
cd FozmineTimedItem
mvn clean package
```

The compiled JAR will be in `target/FozmineTimedItem-1.0.0.jar`.

---

**Enjoy managing your timed items! 🎉**