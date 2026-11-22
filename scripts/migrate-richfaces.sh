#!/bin/sh
# Migre les pages RichFaces d'un dossier vers dsfr-faces (voir docs/migration-richfaces.md).
# Applique exactement la commande documentée dans le README :
#   - rich:xxx      -> dsfr:xxx (balises et fonctions EL)
#   - xmlns:rich=   -> xmlns:dsfr= (l'URI http://richfaces.org/rich ne change pas)
# Les balises a4j: ne sont pas modifiées.
# Nécessite GNU sed (\< et -i sans argument) : sur macOS, installer gsed.
set -eu

if [ $# -ne 1 ] || [ ! -d "$1" ]; then
    echo "usage: $0 <dossier contenant des .xhtml>" >&2
    exit 2
fi

find "$1" -name '*.xhtml' -exec sed -i -e 's/\<rich:/dsfr:/g' -e 's/xmlns:rich=/xmlns:dsfr=/g' {} +
