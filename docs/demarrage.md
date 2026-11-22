# Démarrage

## Prérequis

| Outil | Version |
|---|---|
| Java | 17 ou plus (exigence de Jakarta EE 11) |
| Maven | 3.9 |
| Node.js / npm | inutile : le build télécharge sa propre version (voir ci-dessous) |
| Docker + Compose | pour le showcase et les tests d'intégration ; GNU sed pour la page de migration du showcase (`gsed` sur macOS) |
| Serveur | tout runtime Jakarta EE 11 (Web Profile suffit), ou un conteneur de servlets avec Faces 4.1 et CDI |

## Construire

```sh
mvn clean install
```

Le DSFR est récupéré depuis npm **par le cycle de vie Maven**, dans le module `dsfr-faces-resources` :

1. `generate-resources` :
   - [frontend-maven-plugin](https://github.com/eirslett/frontend-maven-plugin) télécharge Node
     (`<node.version>` du pom parent, actuellement v24.22.2 LTS). L'archive est mise en cache dans le
     dépôt Maven local et décompressée dans `target/node/`.
   - `npm ci` est lancé dans `dsfr-faces-resources/`, d'après `package.json` et `package-lock.json`.
   - l'enforcer vérifie que `node_modules/@gouvfr/dsfr/dist/dsfr.min.css` existe ;
2. `process-resources` : `dist/` est copié (sans les `.map`) dans `META-INF/resources/dsfr` du jar.

Licence du DSFR : le script `preinstall` du paquet `@gouvfr/dsfr` vérifie l'acceptation des
modalités d'utilisation. Le build la déclare avec la variable d'environnement `DSFR_ACCEPT_LICENSE=1`
(configurée dans le pom de `dsfr-faces-resources`), de sorte que le build passe partout, CI comprise.
Le fichier `dsfr-faces-resources/.dsfr.yml` (`accept-license: …`) reste là pour un `npm ci` lancé à
la main dans ce dossier.

Hors ligne, ou pour gagner du temps quand `node_modules/` est déjà présent :
`mvn package -Dskip.installnodenpm -Dskip.npm`.

Monter de version : modifier `@gouvfr/dsfr` dans `dsfr-faces-resources/package.json` et régénérer
`package-lock.json` (`npm install` dans ce dossier, avec le Node de `target/node/` si besoin). Il faut
aussi mettre à jour `<dsfr.version>` dans le pom parent (utilisé pour le cache-busting `?v=`) : les deux
valeurs doivent rester alignées.

### Sans JDK ni Maven en local

```sh
docker run --rm --network host -u "$(id -u):$(id -g)" -v "$PWD":/src -w /src -e HOME=/tmp \
  maven:3.9-eclipse-temurin-17 mvn -B -Dmaven.repo.local=/tmp/m2 clean package
```

`--network host` contourne les erreurs de réseau bridge de Docker rencontrées sur certains postes.

## Utiliser dans une application

```xml
<dependency>
  <groupId>fr.gouv.dsfr</groupId>
  <artifactId>dsfr-faces-core</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

`dsfr-faces-core` tire `dsfr-faces-resources` (les fichiers DSFR) de façon transitive.

Pour une application RichFaces migrée par `sed`, dépendre plutôt de `dsfr-faces-richfaces-compat` (qui
tire `dsfr-faces-core`) et retirer les jars RichFaces : voir
[migration-richfaces.md](migration-richfaces.md#module-de-compatibilité-dsfr-faces-richfaces-compat).

### Configuration recommandée (`web.xml`)

```xml
<!-- Pas de ':' dans les identifiants client (sélecteurs CSS, JS du DSFR) -->
<context-param>
  <param-name>jakarta.faces.SEPARATOR_CHAR</param-name>
  <param-value>-</param-value>
</context-param>
```

Comme `-` devient le séparateur, ne plus utiliser `-` dans les `id` des composants (préférer le camelCase).

Pour des messages de validation en français (`faces-config.xml`) :

```xml
<application>
  <locale-config>
    <default-locale>fr</default-locale>
    <supported-locale>fr</supported-locale>
  </locale-config>
</application>
```

### Page minimale

```xml
<!DOCTYPE html>
<html lang="fr" xmlns:h="jakarta.faces.html" xmlns:f="jakarta.faces.core"
      xmlns:dsfr="http://gouv.fr/dsfr-faces">
<h:head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1"/>
  <title>Mon application</title>
  <dsfr:head/>
</h:head>
<h:body>
  <main class="fr-container" id="contenu">
    ...
  </main>
</h:body>
</html>
```

`<dsfr:head/>` charge les feuilles de style, les icônes, les favicons et les scripts du DSFR, et
initialise le thème (`data-fr-scheme`).

## Essayer le showcase

Le WAR `dsfr-faces-showcase/target/dsfr-faces-showcase.war` se déploie sur n'importe quel serveur
Jakarta EE 11. Le plus simple est de lancer le service `showcase` de `compose.yml` (GlassFish 8.0.5,
implémentation de référence, qui embarque Mojarra 4.1) :

```sh
mvn clean package
docker compose up -d --build --wait showcase
# http://localhost:8080/dsfr-faces-showcase/
# http://localhost:8080/dsfr-faces-showcase/richfaces/inscription.xhtml   (page RichFaces migrée)
docker compose down
```

Après un nouveau build, relancer `docker compose up -d --build --wait showcase` (l'image recopie le WAR).

## Tests d'intégration

Les tests [Playwright](https://playwright.dev/) sont dans `e2e/` et tournent dans Docker, sans rien
installer sur le poste :

```sh
mvn clean package
docker compose up --build --abort-on-container-exit --exit-code-from e2e
docker compose down
```

- Le service `showcase` démarre GlassFish avec le WAR. Le service `e2e` attend que la page réponde
  (healthcheck), puis lance `npm ci` et `npm test` dans l'image `mcr.microsoft.com/playwright`, sous
  Chromium et Firefox.
- Le code de sortie est celui des tests. Le rapport HTML est écrit dans `e2e/playwright-report/`, et les
  traces des tests en échec dans `e2e/test-results/`.
- Le conteneur de test tourne avec l'utilisateur `1000:1000`, pour que `e2e/node_modules` et les
  rapports vous appartiennent. Si votre uid est différent :
  `HOST_UID=$(id -u) HOST_GID=$(id -g) docker compose up …`.
- Le service ne s'appelle pas `app` : `.app` est un domaine de premier niveau en HSTS préchargé, et les
  navigateurs forceraient `https://app`.

Tests couverts :

| Fichier | Vérifie |
|---|---|
| `tests/showcase.spec.ts` | chargement du DSFR, erreurs des champs obligatoires, validation au blur, envoi ajax et non ajax |
| `tests/richfaces-migration.spec.ts` | page RichFaces migrée par `sed` : en-tête DSFR injecté, `rich:clientId`, `a4j:ajax` et `rich:message` (`ajaxRendered`), `a4j:commandButton` avec `render` hors du formulaire et `oncomplete` |

Chaque test échoue aussi si la page écrit une erreur dans la console.

### Version de Playwright

L'image Docker (`compose.yml`) et la bibliothèque `@playwright/test` (`e2e/package.json`) doivent avoir
**exactement la même version** : chaque version de Playwright attend des navigateurs précis. Le script
`e2e/scripts/check-playwright-version.mjs` le vérifie avant les tests (`pretest`) et échoue avec un
message explicite en cas d'écart. Il compare aussi la version installée dans `node_modules`.

Pour monter de version :

1. changer le tag de l'image dans `compose.yml` ;
2. changer la version dans `e2e/package.json` ;
3. régénérer le verrou avec la nouvelle image :

   ```sh
   docker run --rm -u "$(id -u):$(id -g)" -e HOME=/tmp -v "$PWD/e2e":/e2e -w /e2e \
     mcr.microsoft.com/playwright:vX.Y.Z-noble npm install
   ```

Le contrôle se lance aussi sans Docker (CI) : `node e2e/scripts/check-playwright-version.mjs`.
