#!/bin/sh
# Migration FACULTATIVE (après scripts/migrate-richfaces.sh) : enveloppe chaque
# h:outputLabel + champ + message des pages dans dsfr:field (groupe DSFR), sans changer les id.
# Voir docs/migration-richfaces.md, « Migration facultative ».
#   scripts/migrate-fields.sh [--dry-run] <dossier|fichier.xhtml>...
# Nécessite Java 17+, et le jar : à côté du script (archive d'une release GitHub) ou construit
# dans le dépôt (mvn -pl dsfr-faces-migrate package).
set -eu

here=$(cd "$(dirname "$0")" && pwd)
jar=$(ls "$here"/dsfr-faces-migrate-*.jar "$here"/../dsfr-faces-migrate/target/dsfr-faces-migrate-*.jar 2>/dev/null \
    | grep -v -e '-sources\.jar$' -e '-javadoc\.jar$' | head -n 1 || true)
if [ -z "$jar" ]; then
    echo "jar introuvable : le placer à côté du script, ou lancer mvn -pl dsfr-faces-migrate package" >&2
    exit 2
fi
exec java -jar "$jar" "$@"
