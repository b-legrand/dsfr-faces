# dsfr-faces

Bibliothèque de composants **Jakarta Faces** qui génèrent le balisage du
[Système de Design de l'État (DSFR)](https://www.systeme-de-design.gouv.fr/).

Objectif : moderniser les applications JSF / RichFaces existantes **sans réécrire leurs pages**.
À terme, la migration de l'interface doit se résumer à :

```sh
find src/main/webapp -name '*.xhtml' -exec sed -i -e 's/\<rich:/dsfr:/g' -e 's/xmlns:rich=/xmlns:dsfr=/g' {} +
```

(La seconde expression est nécessaire : la déclaration `xmlns:rich="…"` ne contient pas `rich:`.)

Les balises gardent leurs noms et leurs attributs RichFaces (`dsfr:dataTable`, `dsfr:popupPanel`,
`dsfr:tabPanel`…), mais produisent du HTML DSFR accessible. Voir
[docs/migration-richfaces.md](docs/migration-richfaces.md).

> **État : v0.1, preuve de concept.** 4 composants (`head`, `inputText`, `button`, `messages`).
> La couche de compatibilité RichFaces qui rend possible la migration par `sed` reste à écrire :
> voir la [feuille de route](docs/roadmap.md).

## Principes

- **Cible :** Jakarta Faces 4.1 (Jakarta EE 11), Java 17+. Compatible Faces 4.0 (aucune API propre à 4.1).
- **Indépendante du serveur :** uniquement des API Jakarta en `provided`, aucun descripteur ni plugin
  propre à un serveur. Fonctionne avec Mojarra ou MyFaces.
- **DSFR non modifié :** les CSS et JS officiels sont embarqués tels quels depuis npm.
- **Ajax standard :** `<f:ajax>`, pas de couche ajax propriétaire.
- **Accessibilité intégrée :** libellés, aides, messages d'erreur et attributs ARIA sont reliés par
  les renderers (RGAA).

## Démarrage rapide

```sh
mvn clean package   # télécharge Node, lance npm ci (DSFR) puis construit les modules
```

Aucun Node.js n'est requis sur le poste : le DSFR est récupéré par le cycle de vie Maven
(`dsfr-faces-resources`, via frontend-maven-plugin).

Sans JDK local :

```sh
docker run --rm --network host -u "$(id -u):$(id -g)" -v "$PWD":/src -w /src -e HOME=/tmp \
  maven:3.9-eclipse-temurin-17 mvn -B -Dmaven.repo.local=/tmp/m2 clean package
```

Puis déployer `dsfr-faces-showcase/target/dsfr-faces-showcase.war` sur n'importe quel serveur
Jakarta EE 11 (voir [docs/demarrage.md](docs/demarrage.md)).

```xml
<html lang="fr" xmlns:h="jakarta.faces.html" xmlns:f="jakarta.faces.core"
      xmlns:dsfr="http://gouv.fr/dsfr-faces">
<h:head><dsfr:head/></h:head>
<h:body>
  <h:form id="form">
    <dsfr:messages id="msgs" globalOnly="true"/>
    <dsfr:inputText id="email" label="Adresse électronique" type="email"
                    value="#{bean.email}" required="true">
      <f:ajax event="blur" render="@this"/>
    </dsfr:inputText>
    <dsfr:button value="Envoyer" action="#{bean.enregistrer}">
      <f:ajax execute="@form" render="@form"/>
    </dsfr:button>
  </h:form>
</h:body>
</html>
```

## Modules

| Module | Contenu |
|---|---|
| `dsfr-faces-resources` | `dist/` du DSFR sous `META-INF/resources/dsfr`, servi statiquement à `/{contexte}/dsfr/...` |
| `dsfr-faces-core` | Composants, renderers, `META-INF/dsfr.taglib.xml`. Dépend de `-resources`. |
| `dsfr-faces-showcase` | WAR de démonstration, portable |

## Documentation

| Document | Contenu |
|---|---|
| [docs/demarrage.md](docs/demarrage.md) | Installation, configuration, build, essai du showcase |
| [docs/composants.md](docs/composants.md) | Référence des composants : attributs et HTML généré |
| [docs/architecture.md](docs/architecture.md) | Choix techniques et conventions des renderers |
| [docs/migration-richfaces.md](docs/migration-richfaces.md) | Stratégie de migration par `sed`, correspondances RichFaces → DSFR |
| [docs/roadmap.md](docs/roadmap.md) | Ce qui reste à faire |

## Licence

Le DSFR est réservé aux sites de l'État français : voir `node_modules/@gouvfr/dsfr/LICENSE.md`
et l'acceptation de licence dans `.dsfr.yml`. La licence du code de dsfr-faces reste à définir.
