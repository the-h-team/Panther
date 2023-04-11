# Panther
A Java library for an enhanced workflow.

[![](https://jitpack.io/v/the-h-team/Panther.svg)](https://jitpack.io/#the-h-team/Panther)

---

## Modules
All modules currently target Java 8.

### panther-containers
Custom collections-style utilities
### panther-placeholder
Text replacements
### panther-common
The main library of Panther
### panther-paste
Library for Pastebin and hastebin

---

## Building
```shell
./gradlew assemble shadowJar
```
Outputs can then be found under `<module>/build/libs/`
with the filename `<module>-<version>.jar`. Files with
this pattern contain code for only their respective
assembly.

`shadowJar` prepares another archive for some modules
that includes all of that module's project-level
and/or external dependencies.
The classifier for this archive is either `all` or
`bundled`, dependent on whether the archive includes
only project dependencies (`all`) or may include both
project-level and external dependencies (`bundled`).

###### Copyright 2022 Sanctum Team
