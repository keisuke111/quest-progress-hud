"""Verify release resources and reject bundled third-party bytecode."""
import json
import sys
import tomllib
from pathlib import Path, PurePosixPath
from zipfile import ZipFile

OWN_PACKAGE = "io/github/keisuke111/questprogresshud/"
ROOT = Path(__file__).resolve().parents[2]


def release_properties():
    return dict(line.split("=", 1) for line in
                (ROOT / "gradle.properties").read_text(encoding="utf-8").splitlines()
                if "=" in line and not line.lstrip().startswith("#"))


def verify(path):
    with ZipFile(path) as archive:
        names = archive.namelist()
        errors = []
        if len(names) != len(set(names)):
            errors.append("duplicate archive entries")
        classes = [name for name in names if name.endswith(".class")]
        if not classes:
            errors.append("no HUD bytecode")
        properties = release_properties()
        metadata_path = "META-INF/neoforge.mods.toml"
        if metadata_path not in names:
            errors.append("missing mod metadata")
        else:
            metadata = tomllib.loads(archive.read(metadata_path).decode("utf-8"))
            mods = metadata.get("mods", [])
            if len(mods) != 1 or mods[0].get("modId") != properties["mod_id"]:
                errors.append("unexpected mod ID")
            if len(mods) != 1 or mods[0].get("version") != properties["mod_version"]:
                errors.append("packaged version does not match gradle.properties")
            if metadata.get("license") != properties["mod_license"]:
                errors.append("unexpected mod license")
        if "META-INF/LICENSE" not in names:
            errors.append("missing license file")
        elif archive.read("META-INF/LICENSE") != (ROOT / "LICENSE").read_bytes():
            errors.append("packaged license does not match repository")
        languages = {}
        for locale in ("en_us", "ja_jp"):
            resource = f"assets/{properties['mod_id']}/lang/{locale}.json"
            if resource not in names:
                errors.append("missing language resource: " + locale)
                continue
            languages[locale] = json.loads(archive.read(resource))
            expected = json.loads((ROOT / "src/main/resources" / resource).read_text(encoding="utf-8"))
            if languages[locale] != expected:
                errors.append("packaged language differs from source: " + locale)
        if len(languages) == 2 and languages["en_us"].keys() != languages["ja_jp"].keys():
            errors.append("English and Japanese translation keys differ")
        for name in names:
            parts = PurePosixPath(name).parts
            if name.startswith("/") or ".." in parts or "\\" in name:
                errors.append("unsafe archive path: " + name)
            if name.lower().endswith(".jar"):
                errors.append("embedded jar: " + name)
            if name.endswith(".class") and not name.startswith(OWN_PACKAGE):
                errors.append("external bytecode: " + name)
            if name.startswith("META-INF/jarjar/"):
                errors.append("jar-in-jar metadata: " + name)
        if errors:
            raise ValueError(str(path) + ": " + "; ".join(errors))
        print(f"Verified {path}: version {properties['mod_version']}, license and both languages; "
              f"{len(classes)} HUD classes; no bundled external bytecode/jars")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        sys.exit("Usage: verify_jar.py JAR [JAR ...]")
    for argument in sys.argv[1:]:
        verify(Path(argument))

