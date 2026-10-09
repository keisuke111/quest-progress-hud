# Dependency security triage — 2026-10-09

## Result and scope

All 34 initial Dependabot alerts were reviewed. **They remain unresolved and open.** This is a provenance assessment, not a claim that Minecraft, NeoForge, or ATM10 is free of vulnerabilities.

The supported target remains Minecraft 1.21.1, NeoForge 21.1.251, FTB Quests 2101.1.36, and ATM10 v8.2. The HUD source, quest denominator, one-second refresh, and published v0.0.9 jar are unchanged.

The Gradle dependency submission scans build tooling and the game development classpath together. Its `settings.gradle` manifest label and `direct` relationship do not mean that these libraries are declared in that file or embedded in the HUD jar.

## Verified provenance

The [audit run](https://github.com/keisuke111/quest-progress-hud/actions/runs/37886540862) preserves `dependency-provenance.txt` and the submitted dependency graph as the `dependency-audit` artifact. The reports resolve:

| Dependency | Resolved version | Origin | Initial alerts |
| --- | --- | --- | --- |
| Commons Compress | 1.18 | NeoFormRuntime 2.0.31 → DiffPatch 2.0.0.36 build tool | 1–6 |
| Netty common/handler/codec/native epoll | 4.1.97.Final | Minecraft 1.21.1 library constraints | 7–9, 11, 19–24, 27–28 |
| Commons Lang | 3.14.0; 3.9 in build tool | Minecraft/NeoForge; DiffPatch | 10 |
| LZ4 Java | 1.8.0 | Minecraft 1.21.1 library constraints | 12–13, 25, 30–34 |
| Log4j core/api | 2.22.1 | Minecraft and NeoForge loader dependencies | 14, 16–18, 26 |
| Plexus Utils | 3.3.0 | NeoForge loader → Maven Artifact 3.8.5 | 15 |
| JLine Reader | 3.20.0 | NeoForge loader → TerminalConsoleAppender 1.3.0 | 29 |

Minecraft also supplies Commons Compress 1.26.0, which is outside the affected ranges of alerts 1–6. The old build-tool copy is a separate dependency. Alert 10 spans both the game and build tool; categories overlap.

The published jar was inspected: its classes are under `io/github/keisuke111/questprogresshud/`, with no embedded jars or external bytecode. CI now checks this boundary on every build. This prevents accidental bundling; it does not patch libraries provided by the launcher or other mods.

## Treatment

- Keep all alerts visible; no dismissals, graph exclusions, or global version forcing were used.
- ModDevGradle 2.0.148 and Foojay resolver 1.0.0 were already the latest plugin releases at review time ([ModDevGradle](https://plugins.gradle.org/plugin/net.neoforged.moddev), [Foojay](https://plugins.gradle.org/plugin/org.gradle.toolchains.foojay-resolver-convention)).
- DiffPatch has updated source using Commons Compress 1.27.1 ([upstream build](https://github.com/TheCBProject/DiffPatch/blob/master/build.gradle)). However, the selected [NeoFormRuntime tool coordinates](https://github.com/neoforged/NeoFormRuntime/blob/main/src/main/resources/tools.properties) still select DiffPatch 2.0.0.36. Its executable `all` jar relocates its dependencies. Forcing Commons Compress on a Gradle configuration would not update that embedded copy. A supported tool-chain update must replace the executable too and verify the actual patch operation before declaring alerts 1–6 fixed.
- Game/loader libraries are owned by Minecraft/NeoForge and supplied by the installation. Updating development dependencies alone would not fix an existing ATM10 installation. A platform update needs separate compatibility testing; the HUD does not inject replacement Netty, logging, LZ4, Plexus or JLine libraries.
- The HUD does not create TLS/SNI endpoints, compressed-data parsers, remote logging appenders, archive extractors, or an interactive console. These absence checks limit direct HUD exposure, but do not establish that the surrounding game or other mods cannot reach the affected APIs. In particular, alert 28's critical rating is retained; its documented SNI/mTLS trigger is not implemented by the HUD.
- Builds run in disposable GitHub-hosted runners with bounded job timeouts. Do not run untrusted builds with personal credentials or a shared privileged environment. This limits consequences of build-tool failures and does not remove vulnerable code.

## Per-alert review

Patched versions below are the versions reported by each advisory at review time, not verified compatible replacements for this modpack. `None` means no patch under the original `org.lz4` package coordinates. Several advisories describe fixes in the maintained `at.yawk.lz4` fork (for example 1.10.1 or 1.11.4); updating only to original-coordinate 1.8.1 does not fix every LZ4 alert.

| Alert | Package | Advisory | Reported patch | Trigger / status |
| --- | --- | --- | --- | --- |
| [#1](https://github.com/keisuke111/quest-progress-hud/security/dependabot/1) | `org.apache.commons:commons-compress` | [GHSA-53x6-4x5p-rrvv](https://github.com/advisories/GHSA-53x6-4x5p-rrvv) | 1.19 | Archive filename encoding; open, upstream-owned |
| [#2](https://github.com/keisuke111/quest-progress-hud/security/dependabot/2) | `org.apache.commons:commons-compress` | [GHSA-7hfm-57qf-j43q](https://github.com/advisories/GHSA-7hfm-57qf-j43q) | 1.21 | 7z codec traversal; open, upstream-owned |
| [#3](https://github.com/keisuke111/quest-progress-hud/security/dependabot/3) | `org.apache.commons:commons-compress` | [GHSA-crv7-7245-f45f](https://github.com/advisories/GHSA-crv7-7245-f45f) | 1.21 | 7z input lengths; open, upstream-owned |
| [#4](https://github.com/keisuke111/quest-progress-hud/security/dependabot/4) | `org.apache.commons:commons-compress` | [GHSA-xqfj-vm6h-2x34](https://github.com/advisories/GHSA-xqfj-vm6h-2x34) | 1.21 | 7z input lengths; open, upstream-owned |
| [#5](https://github.com/keisuke111/quest-progress-hud/security/dependabot/5) | `org.apache.commons:commons-compress` | [GHSA-mc84-pj99-q6hh](https://github.com/advisories/GHSA-mc84-pj99-q6hh) | 1.21 | ZIP input lengths; open, upstream-owned |
| [#6](https://github.com/keisuke111/quest-progress-hud/security/dependabot/6) | `org.apache.commons:commons-compress` | [GHSA-4g9r-vxhx-9pgx](https://github.com/advisories/GHSA-4g9r-vxhx-9pgx) | 1.26.0 | Corrupt DUMP archive parsing; open, upstream-owned |
| [#7](https://github.com/keisuke111/quest-progress-hud/security/dependabot/7) | `io.netty:netty-handler` | [GHSA-4g8c-wm8x-jfhw](https://github.com/advisories/GHSA-4g8c-wm8x-jfhw) | 4.1.118.Final | Native TLS engine packet validation; open, upstream-owned |
| [#8](https://github.com/keisuke111/quest-progress-hud/security/dependabot/8) | `io.netty:netty-common` | [GHSA-xq3w-v528-46rv](https://github.com/advisories/GHSA-xq3w-v528-46rv) | 4.1.115.Final | Local Windows environment file; open, upstream-owned |
| [#9](https://github.com/keisuke111/quest-progress-hud/security/dependabot/9) | `io.netty:netty-common` | [GHSA-389x-839f-4rhx](https://github.com/advisories/GHSA-389x-839f-4rhx) | 4.1.118.Final | Local Windows environment file (incomplete prior fix); open, upstream-owned |
| [#10](https://github.com/keisuke111/quest-progress-hud/security/dependabot/10) | `org.apache.commons:commons-lang3` | [GHSA-j288-q9x7-2f5v](https://github.com/advisories/GHSA-j288-q9x7-2f5v) | 3.18.0 | ClassUtils.getClass recursion; also present in DiffPatch tool dependencies; open, upstream-owned |
| [#11](https://github.com/keisuke111/quest-progress-hud/security/dependabot/11) | `io.netty:netty-codec` | [GHSA-3p8m-j85q-pgmj](https://github.com/advisories/GHSA-3p8m-j85q-pgmj) | 4.1.125.Final | Untrusted compressed input to Netty decoders; open, upstream-owned |
| [#12](https://github.com/keisuke111/quest-progress-hud/security/dependabot/12) | `org.lz4:lz4-java` | [GHSA-vqf4-7m7x-wgfc](https://github.com/advisories/GHSA-vqf4-7m7x-wgfc) | 1.8.1 | Untrusted LZ4 input / native memory access; open, upstream-owned |
| [#13](https://github.com/keisuke111/quest-progress-hud/security/dependabot/13) | `org.lz4:lz4-java` | [GHSA-cmp6-m4wj-q63q](https://github.com/advisories/GHSA-cmp6-m4wj-q63q) | None | Java LZ4 decompression into reused buffers; open, upstream-owned |
| [#14](https://github.com/keisuke111/quest-progress-hud/security/dependabot/14) | `org.apache.logging.log4j:log4j-core` | [GHSA-vc5p-v9hr-52mj](https://github.com/advisories/GHSA-vc5p-v9hr-52mj) | 2.25.3 | TLS SocketAppender configuration; open, upstream-owned |
| [#15](https://github.com/keisuke111/quest-progress-hud/security/dependabot/15) | `org.codehaus.plexus:plexus-utils` | [GHSA-6fmv-xxpf-w3cw](https://github.com/advisories/GHSA-6fmv-xxpf-w3cw) | 3.6.1 | Plexus Expand.extractFile archive extraction; open, upstream-owned |
| [#16](https://github.com/keisuke111/quest-progress-hud/security/dependabot/16) | `org.apache.logging.log4j:log4j-core` | [GHSA-3pxv-7cmr-fjr4](https://github.com/advisories/GHSA-3pxv-7cmr-fjr4) | 2.25.4 | XmlLayout with forbidden XML characters; open, upstream-owned |
| [#17](https://github.com/keisuke111/quest-progress-hud/security/dependabot/17) | `org.apache.logging.log4j:log4j-core` | [GHSA-6hg6-v5c8-fphq](https://github.com/advisories/GHSA-6hg6-v5c8-fphq) | 2.25.4 | TLS logging verifyHostName configuration; open, upstream-owned |
| [#18](https://github.com/keisuke111/quest-progress-hud/security/dependabot/18) | `org.apache.logging.log4j:log4j-core` | [GHSA-445c-vh5m-36rj](https://github.com/advisories/GHSA-445c-vh5m-36rj) | 2.25.4 | Rfc5424Layout stream logging configuration; open, upstream-owned |
| [#19](https://github.com/keisuke111/quest-progress-hud/security/dependabot/19) | `io.netty:netty-codec` | [GHSA-mj4r-2hfc-f8p6](https://github.com/advisories/GHSA-mj4r-2hfc-f8p6) | 4.1.133.Final | Lz4FrameDecoder untrusted block lengths; open, upstream-owned |
| [#20](https://github.com/keisuke111/quest-progress-hud/security/dependabot/20) | `io.netty:netty-handler` | [GHSA-3qp7-7mw8-wx86](https://github.com/advisories/GHSA-3qp7-7mw8-wx86) | 4.1.135.Final | IPv6 IpSubnetFilter rules; open, upstream-owned |
| [#21](https://github.com/keisuke111/quest-progress-hud/security/dependabot/21) | `io.netty:netty-handler` | [GHSA-x4gw-5cx5-pgmh](https://github.com/advisories/GHSA-x4gw-5cx5-pgmh) | 4.1.135.Final | Server-side SNI ClientHello size handling; open, upstream-owned |
| [#22](https://github.com/keisuke111/quest-progress-hud/security/dependabot/22) | `io.netty:netty-transport-native-epoll` | [GHSA-w573-9ffj-6ff9](https://github.com/advisories/GHSA-w573-9ffj-6ff9) | 4.1.135.Final | Unix domain sockets receiving file descriptors; open, upstream-owned |
| [#23](https://github.com/keisuke111/quest-progress-hud/security/dependabot/23) | `io.netty:netty-handler` | [GHSA-c653-97m9-rcg9](https://github.com/advisories/GHSA-c653-97m9-rcg9) | 4.1.135.Final | TLS wrapping a plain trust manager; open, upstream-owned |
| [#24](https://github.com/keisuke111/quest-progress-hud/security/dependabot/24) | `io.netty:netty-codec` | [GHSA-558v-64gr-wgg4](https://github.com/advisories/GHSA-558v-64gr-wgg4) | 4.1.136.Final | Bzip2Decoder malformed compressed input; open, upstream-owned |
| [#25](https://github.com/keisuke111/quest-progress-hud/security/dependabot/25) | `org.lz4:lz4-java` | [GHSA-xx22-p4ch-683r](https://github.com/advisories/GHSA-xx22-p4ch-683r) | None | Native XXHash invalid buffer ranges; open, upstream-owned |
| [#26](https://github.com/keisuke111/quest-progress-hud/security/dependabot/26) | `org.apache.logging.log4j:log4j-api` | [GHSA-qv9r-c865-cp47](https://github.com/advisories/GHSA-qv9r-c865-cp47) | 2.25.5 | MapMessage JSON non-finite numeric values; open, upstream-owned |
| [#27](https://github.com/keisuke111/quest-progress-hud/security/dependabot/27) | `io.netty:netty-handler` | [GHSA-fccg-mwvh-qqg4](https://github.com/advisories/GHSA-fccg-mwvh-qqg4) | 4.1.137.Final | Server-side fragmented TLS ClientHello SNI parsing; open, upstream-owned |
| [#28](https://github.com/keisuke111/quest-progress-hud/security/dependabot/28) | `io.netty:netty-handler` | [GHSA-c4c3-7fpv-j4q5](https://github.com/advisories/GHSA-c4c3-7fpv-j4q5) | 4.1.137.Final | Critical: SNI-only mTLS gate with permissive fallback context; open, upstream-owned |
| [#29](https://github.com/keisuke111/quest-progress-hud/security/dependabot/29) | `org.jline:jline-reader` | [GHSA-5q95-hrpc-m3w3](https://github.com/advisories/GHSA-5q95-hrpc-m3w3) | 3.30.15 | JLine HISTORY_IGNORE controlled regex configuration; open, upstream-owned |
| [#30](https://github.com/keisuke111/quest-progress-hud/security/dependabot/30) | `org.lz4:lz4-java` | [GHSA-6cx8-rjf8-pr8g](https://github.com/advisories/GHSA-6cx8-rjf8-pr8g) | None | LZ4 with-length convenience decompression API; open, upstream-owned |
| [#31](https://github.com/keisuke111/quest-progress-hud/security/dependabot/31) | `org.lz4:lz4-java` | [GHSA-4v53-57pg-c464](https://github.com/advisories/GHSA-4v53-57pg-c464) | None | LZ4BlockInputStream untrusted compressed lengths; open, upstream-owned |
| [#32](https://github.com/keisuke111/quest-progress-hud/security/dependabot/32) | `org.lz4:lz4-java` | [GHSA-gm45-99xc-r7wv](https://github.com/advisories/GHSA-gm45-99xc-r7wv) | None | LZ4FrameInputStream concatenated empty frames; open, upstream-owned |
| [#33](https://github.com/keisuke111/quest-progress-hud/security/dependabot/33) | `org.lz4:lz4-java` | [GHSA-343h-94h5-c4wr](https://github.com/advisories/GHSA-343h-94h5-c4wr) | None | LZ4BlockInputStream non-default stopOnEmptyBlock=false; open, upstream-owned |
| [#34](https://github.com/keisuke111/quest-progress-hud/security/dependabot/34) | `org.lz4:lz4-java` | [GHSA-mcr4-qmvw-px4g](https://github.com/advisories/GHSA-mcr4-qmvw-px4g) | None | JNI extraction in a shared writable temporary directory; open, upstream-owned |

## Recheck before release

1. Review the latest dependency graph and all open alerts after any toolchain/platform update.
2. For the build-tool update, verify the executed DiffPatch jar and its embedded dependency versions, then run a fresh Minecraft artifact generation (not only a cached build) and the HUD checks.
3. For a platform update, validate the actual launcher libraries and exercise single-player, multiplayer, disconnect/rejoin, and team progress with the supported pack.
4. Close an alert only after a verified dependency update or a documented reachability assessment sufficient for the specific alert. A successful build or several hours of play does not establish security remediation.

The current result supports informed beta evaluation, **not** a statement that all dependency alerts have been fixed. A release policy requiring zero known dependency alerts is not satisfied by this target environment.

