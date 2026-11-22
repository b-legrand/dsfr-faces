#!/bin/sh
# Migration FACULTATIVE (après scripts/migrate-richfaces.sh) : enveloppe chaque
# h:outputLabel + champ + message des pages dans dsfr:field (groupe DSFR), sans changer les id.
# Voir docs/migration-richfaces.md, « Migration facultative ».
#   scripts/migrate-fields.sh [--dry-run] <dossier|fichier.xhtml>...
# Nécessite Java 17+ et le module construit : mvn -pl dsfr-faces-migrate package
set -eu

root=$(cd "$(dirname "$0")/.." && pwd)
jar=$(ls "$root"/dsfr-faces-migrate/target/dsfr-faces-migrate-*.jar 2>/dev/null | head -n 1 || true)
if [ -z "$jar" ]; then
    echo "jar introuvable : lancer d'abord mvn -pl dsfr-faces-migrate package" >&2
    exit 2
fi
exec java -jar "$jar" "$@"
