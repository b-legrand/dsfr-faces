# dsfr-faces

Bibliothèque de composants **Jakarta Faces** qui génèrent le balisage du
[Système de Design de l'État (DSFR)](https://www.systeme-de-design.gouv.fr/).

Objectif : moderniser les applications JSF / RichFaces existantes **sans réécrire leurs pages**.
À terme, la migration de l'interface doit se résumer à :

```sh
find src/main/webapp -name '*.xhtml' -exec sed -i -e 's/\<rich:/dsfr:/g' -e 's/xmlns:rich=/xmlns:dsfr=/g' {} +
```

(La seconde expression est nécessaire : la déclaration `xmlns:rich="…"` ne contient pas `rich:`.)

Ensuite, une migration **facultative** (`scripts/migrate-fields.sh`) structure les champs existants en
groupes DSFR (`dsfr:field` : état d'erreur, mention obligatoire, liens ARIA), sans changer les
identifiants. Elle demande une relecture du diff.

Les balises gardent leurs noms et leurs attributs RichFaces (`dsfr:dataTable`, `dsfr:popupPanel`,
`dsfr:tabPanel`…), mais produisent du HTML DSFR accessible. Voir
[docs/migration-richfaces.md](docs/migration-richfaces.md).

> **État : v0.1.** 26 composants `dsfr:` (champs, listes, cases, interrupteur, mot de passe, date, tableau
> triable et pagination, modale, onglets, accordéons, panneau, fieldset, liens et boutons), la compatibilité
> RichFaces pour les balises P1 (`rich:dataTable`, `rich:popupPanel`, `rich:tabPanel`, `a4j:ajax`…, fonctions
> EL, `RichFaces.component`) et le style DSFR des balises `h:` standard. Le showcase contient une page RichFaces d'origine,
> migrée par ce `sed` au build et vérifiée par des tests Playwright. Le reste est dans la
> [feuille de route](docs/roadmap.md).

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
Jakarta EE 11, ou lancer le showcase et les tests d'intégration avec Docker Compose (voir
[docs/demarrage.md](docs/demarrage.md)) :

```sh
docker compose up -d --build --wait showcase   # http://localhost:8080/dsfr-faces-showcase/
docker compose up --build --abort-on-container-exit --exit-code-from e2e   # tests Playwright
docker compose down
```

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
| `dsfr-faces-html` | Classes DSFR sur les balises `h:` standard (`h:inputText`, `h:selectOneMenu`, `h:commandButton`…) sans modifier les pages |
| `dsfr-faces-richfaces-compat` | Taglibs aux URI RichFaces (`rich`, `a4j`) pour les pages migrées par `sed`. Remplace les jars RichFaces. Tire `dsfr-faces-html`. |
| `dsfr-faces-migrate` | Outil de migration facultative : enveloppe libellé + champ + message dans `dsfr:field` |
| `dsfr-faces-showcase` | WAR de démonstration, portable, avec des pages RichFaces migrées au build (chemin obligatoire et chemin facultatif) |
| `e2e/` + `compose.yml` | Tests d'intégration Playwright (GlassFish + Chromium et Firefox, dans Docker) |

## Documentation

| Document | Contenu |
|---|---|
| [docs/demarrage.md](docs/demarrage.md) | Installation, configuration, build, showcase, tests d'intégration |
| [docs/composants.md](docs/composants.md) | Référence des composants : attributs et HTML généré |
| [docs/architecture.md](docs/architecture.md) | Choix techniques et conventions des renderers |
| [docs/migration-richfaces.md](docs/migration-richfaces.md) | Stratégie de migration par `sed`, correspondances RichFaces → DSFR |
| [docs/roadmap.md](docs/roadmap.md) | Ce qui reste à faire |

## Licence

Le DSFR est réservé aux sites de l'État français : voir `node_modules/@gouvfr/dsfr/LICENSE.md`
et l'acceptation de licence dans `.dsfr.yml`. La licence du code de dsfr-faces reste à définir.
