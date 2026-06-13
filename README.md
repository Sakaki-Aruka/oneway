# Oneway

Tiny CLI tool for Modrinth.

# Features

- Create version release
- Check version conflict

# Commands
## Create

Base: `java -jar Oneway-<VERSION>.jar version`  

### Exit codes

| Code | Meaning |
|------|---------|
| 0 | Success |
| 3 | Invalid arguments |
| 4 | Request failed |

## Required arguments
- `--token <TOKEN>` : PAT (Personal Access Token)
- `-f <FILE_PATH>` : Upload file path
- `-V <VERSION>` : Release elements version. (e.g. `1.0.0`)
- `-g <GAME_VERSIONS>` : Target Minecraft version. Split by commas.
  - There are two version formats.
  - Single : Specify single version. (e.g. `1.21.1`, `1.21.11,26.1`)
  - Range  : Specify range versions. (e.g. `1.12~1.21`)
- `-t <RELEASE_TYPE>` : Release types
  - `release` : Standard version
  - `beta` : Beta
  - `alpha` : Alpha
- `-l <PLATFORM_TYPES>` : Platform names.
- `-p <PROJECT_ID>` : Project ID

### Platform types

<details><summary>Overview</summary>

```text
babric
bta-babric
bukkit
bungeecord
canvas
datapack
fabric
folia
forge
iris
java-agent
legacy-fabric
liteloader
minecraft
modloader
neoforge
nilloader
optifine
ornithe
paper
purpur
quilt
rift
spigot
sponge
vanilla
velocity
waterfall
```

</details>

## Option arguments

- `--version-name <NAME>` : Release title. (e.g. `Version 1.0.0`)
- `--changelog <FILE_PATH>` : Attached changelogs' filepath
- `-d <DEPENDENCY>` : Dependency information. 
  - `--dependency` : Alias
  - [Format](#dependency-format)
- `--featured` : Recommended on projects' top page
- `-s <STATUS>` : Release status
  - `--status` : Alias
  - `listed`, `draft`, `unlisted`, `archived`
- `-e` : Displays errors
  - `--show-error` : Alias
- `--show-response` : Displays http response
- `--test` : Use test api endpoint

### Dependency format

Format: `v=<version_id>/p=<project_id>/f=<file_name>/t=<type>`

- `t` (type) is required. Valid values: `required`, `optional`, `incompatible`, `embedded`
- `f` (file_name) is an external file name, not a local path
- Valid combinations: `v+t`, `p+t`, `v+p+t`, `f+t`

Example (depends on custom-crafter-api 5.2.1):
```
-d v=MHzOV8q4/t=required
```

## Check version conflict

Base: `java -jar Oneway-<VERSION>.jar not-exists <PROJECT_ID> <VERSION>`

### Exit codes

| Code | Meaning |
|------|---------|
| 0 | Version does not exist (no conflict) |
| 3 | Request or parse error |
| 4 | Version already released |

### Option arguments

- `-e` : Displays errors
  - `--show-error` : Alias
