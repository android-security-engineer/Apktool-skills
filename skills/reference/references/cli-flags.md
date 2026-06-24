# Core Commands & CLI Flags

Decode/build/framework operations and their options. For analysis-command output fields see [commands.md](commands.md).

## Core commands

| Command | Alias | Target | Description |
|---------|-------|--------|-------------|
| `decode` | `d` | apk | Decode an APK to smali + resources |
| `build` | `b` | dir | Build an APK from a decoded directory |
| `install-framework` | `if` | apk | Install a framework APK for resource decoding |
| `clean-frameworks` | `cf` | — | Remove installed framework files |
| `list-frameworks` | `lf` | — | List installed framework files |
| `publicize-resources` | `pr` | arsc | Make all resources public in an ARSC file |

## `decode` options

| Flag | Long | Meaning |
|------|------|---------|
| `-o` | `--output` | Output directory |
| `-f` | `--force` | Overwrite existing output directory |
| `-s` | `--no-src` | Do not decode sources (resources only) |
| `-r` | `--no-res` | Do not decode resources (smali only) |
| `-a` | `--all-src` | Decode all sources, including unknown DEX files |
| | `--only-manifest` | Decode only `AndroidManifest.xml` |
| | `--no-debug-info` | Strip `.local`/`.param`/`.line` debug directives |
| | `--no-assets` | Do not copy the `assets/` directory |
| | `--keep-broken-res` | Keep resources that fail to decode |
| | `--ignore-raw-values` | Ignore raw attribute values in XML resources |
| | `--match-original` | Keep files closest to original (best for re-signing) |
| | `--res-resolve-mode` | Resource resolve mode: `remove` \| `dummy` \| `keep` |
| `-p` | `--frame-path` | Framework directory to use |
| `-t` | `--frame-tag` | Framework tag to use |
| `-l` | `--lib` | Shared library in `package:path` form |

## `build` options

| Flag | Long | Meaning |
|------|------|---------|
| `-o` | `--output` | Output APK path |
| `-f` | `--force` | Overwrite / skip change detection |
| | `--no-apk` | Produce files but do not package the APK |
| | `--no-crunch` | Disable resource crunching |
| | `--copy-original` | Copy original `AndroidManifest.xml` and `META-INF` |
| | `--debuggable` | Set `android:debuggable="true"` in the built APK |
| | `--net-sec-conf` | Inject a permissive network security config |
| | `--aapt` | Path to an external `aapt`/`aapt2` binary |
| `-p` | `--frame-path` | Framework directory to use |

## Framework commands

```bash
apktool install-framework framework-res.apk   # -p <dir>, -t <tag>
apktool list-frameworks                        # -p <dir>
apktool clean-frameworks                        # -a / --all
apktool publicize-resources resources.arsc
```
