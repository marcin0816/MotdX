# MotdX

Animated MOTD plugin for Paper servers with MiniMessage support, multiple animation modes, server icon cycling, ActionBar messages, time-based schedules, and premium account detection.

**Requires:** Paper 26.1.2+  
**Java:** 21+  
**Optional:** PlaceholderAPI, AuthMe Reloaded

---

## Features

### Animated MOTD
Cycle through multiple MOTD frames on every server ping. Each frame supports full [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting with gradients, colors, bold, italic, and more.

**Animation modes:**
| Mode | Description |
|------|-------------|
| `sequential` | Plays frames in order: 1, 2, 3, 1, 2, 3… |
| `random` | Picks a random frame on each ping |
| `shuffle` | Shuffles all frames, plays through them, then reshuffles |

### Animated Server Icons
Cycle 64x64 PNG icons from the `plugins/MotdX/icons/` directory.

**Icon modes:**
| Mode | Description |
|------|-------------|
| `sync` | Changes in sync with the MOTD frame |
| `sequential` | Independent sequential order |
| `random` | Random icon on each ping |
| `shuffle` | Shuffled order, reshuffles when done |

### ActionBar Animation
Broadcast animated ActionBar messages to all online players who have the required permission. Supports `{online}`, `{max}`, and `{tps}` placeholders.

### Time-Based Schedules
Override the normal MOTD with specific frames during defined time ranges (e.g. morning, evening). Supports overnight ranges like `22:00–06:00`.

### Conditions
Automatically show a different MOTD based on server state:

| Condition | Trigger |
|-----------|---------|
| `full` | Online players >= max players |
| `whitelist` | Server whitelist is enabled |
| `low` | Online players <= configurable threshold |
| `maintenance` | Toggled via `/motdx maintenance` or enabled in config |

### Premium Account Detection
On player join, checks the Mojang API asynchronously to determine if the username belongs to a real premium account. If so, displays a configurable title in the center of the screen prompting the player to use `/premium` for automatic login.

Once a player uses `/premium`, their UUID is saved to `plugins/MotdX/premium-used.txt` and the notification is never shown again.

**Requirements to activate:**
- `premium-check.enabled: true`
- `premium-check.enablePremium: true`
- **AuthMe Reloaded** must be loaded on the server

### PlaceholderAPI Support
Exposes the following placeholders via PlaceholderAPI:

| Placeholder | Description |
|-------------|-------------|
| `%motdx_maintenance%` | `true` / `false` — maintenance mode state |
| `%motdx_mode%` | Current animation mode |
| `%motdx_frames%` | Number of loaded MOTD frames |

### Update Checker
On startup, checks GitHub Releases for a newer version and logs a message with the download link if one is available. Configurable via `github-owner` and `github-repo` in `config.yml`.

---

## Commands

All commands require the `motdx.use` permission.

| Command | Description |
|---------|-------------|
| `/motdx reload` | Reload configuration and all frames/icons |
| `/motdx maintenance` | Toggle maintenance mode on/off |
| `/motdx preview` | Preview the current MOTD frame in chat |
| `/motdx set <mode>` | Change animation mode (`sequential`, `random`, `shuffle`) |
| `/motdx info` | Display plugin info (frames, mode, maintenance, icons) |

---

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `motdx.use` | Access to `/motdx` command | op |
| `motdx.reload` | Reload config | op |
| `motdx.maintenance` | Toggle maintenance mode | op |
| `motdx.preview` | Preview current MOTD in chat | op |
| `motdx.set` | Change animation mode on the fly | op |
| `motdx.info` | View plugin info | op |
| `motdx.actionbar` | Receive ActionBar animation | op |

---

## Configuration

```yaml
animation:
  mode: sequential   # sequential | random | shuffle

motds:
  - line1: "<gradient:gold:yellow><bold>► MyServer ◄</bold></gradient>"
    line2: "<gray>Online: <yellow>{online}<gray>/<yellow>{max}"
  - line1: "<gold><bold>✦ MyServer ✦</bold></gold>"
    line2: "<aqua>myserver.net"
    # Optional overrides:
    # players-online: 42
    # players-max: 100

icons:
  enabled: false
  mode: sync   # sync | sequential | random | shuffle

actionbar:
  enabled: false
  interval: 40         # ticks (20 = 1 second)
  permission: motdx.actionbar
  frames:
    - "<gradient:gold:yellow>► MyServer ◄</gradient>"

schedules:
  enabled: false
  entries:
    - name: "morning"
      from: "06:00"
      to:   "12:00"
      motds:
        - line1: "<gold><bold>Good morning!</bold></gold>"
          line2: "<gray>myserver.net"

conditions:
  full:
    enabled: false
    line1: "<red><bold>SERVER FULL</bold></red>"
    line2: "<gray>Please try again later"
  whitelist:
    enabled: false
    line1: "<yellow><bold>🔒 WHITELIST</bold></yellow>"
    line2: "<gray>Server is whitelisted"
  low:
    enabled: false
    threshold: 5
    line1: "<green><bold>► MyServer ◄</bold></green>"
    line2: "<gray>Server is almost empty — be the first!"
  maintenance:
    enabled: false
    line1: "<yellow><bold>⚙ MAINTENANCE MODE ⚙</bold></yellow>"
    line2: "<gray>Check back soon"

premium-check:
  enabled: true
  enablePremium: false   # set to true + AuthMe must be loaded
  title: "<gold><bold>Premium account detected!"
  subtitle: "<yellow>Use <white>/premium<yellow> for automatic login"

update-checker:
  enabled: true
  github-owner: marcin0816
  github-repo: MotdX
```

---

## Installation

1. Drop `MotdX.jar` into your `plugins/` folder
2. Start the server — `plugins/MotdX/config.yml` will be generated
3. Edit the config to your liking
4. Run `/motdx reload`

To use animated icons, place 64x64 PNG files in `plugins/MotdX/icons/` and set `icons.enabled: true`.

---

## Building from Source

```bash
git clone https://github.com/marcin0816/MotdX.git
cd MotdX
mvn clean package
```

The built jar will be in `target/motdx-<version>.jar`.

---

## License

MIT
