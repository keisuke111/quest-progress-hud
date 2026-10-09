"""Reject accidentally bundled third-party bytecode in the standalone HUD jar."""
import sys
from pathlib import Path, PurePosixPath
from zipfile import ZipFile

OWN_PACKAGE = "io/github/keisuke111/questprogresshud/"


def verify(path):
    with ZipFile(path) as archive:
        names = archive.namelist()
        errors = []
        if len(names) != len(set(names)):
            errors.append("duplicate archive entries")
        classes = [name for name in names if name.endswith(".class")]
        if not classes:
            errors.append("no HUD bytecode")
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
        print(f"Verified {path}: {len(classes)} HUD classes; no bundled external bytecode/jars")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        sys.exit("Usage: verify_jar.py JAR [JAR ...]")
    for argument in sys.argv[1:]:
        verify(Path(argument))

