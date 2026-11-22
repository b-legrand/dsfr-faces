# Intégration continue et publication

## Intégration continue (`.github/workflows/ci.yml`)

Lancée à chaque push sur `main`, sur chaque pull request, à la demande (`workflow_dispatch`) et par le
workflow de release.

| Job | Contenu |
|---|---|
| `build` | `mvn verify` en Java 17 (Temurin) : Node et le DSFR récupérés par Maven, compilation, tests unitaires, migration des pages RichFaces du showcase. Publie le WAR du showcase comme artefact du job. |
| `e2e` | Récupère le WAR, puis `docker compose up … --exit-code-from e2e` : GlassFish 8 + Playwright (Chromium, Firefox), exactement comme en local ([demarrage.md](demarrage.md#tests-dintégration)). |

En cas d'échec, les rapports sont joints à l'exécution : `surefire-reports`, `playwright-report` (rapport
HTML + traces des tests en échec) et le journal de GlassFish dans la sortie du job. Avec `CI=true`
(positionné par GitHub), Playwright refait une fois un test en échec et refuse les `test.only`.

Le cache Maven (`actions/setup-java`) contient aussi le Node téléchargé par frontend-maven-plugin.

## Publier une version (`.github/workflows/release.yml`)

Les versions sont publiées sur **GitHub Packages** (registre Maven du dépôt `b-legrand/dsfr-faces`),
sous le `groupId` `fr.gouv.dsfr`. La branche `main` reste en `X.Y.Z-SNAPSHOT` ; c'est l'étiquette qui
fixe la version publiée.

```sh
git switch main && git pull
git tag v0.1.0
git push origin v0.1.0
```

Le workflow :

1. vérifie l'étiquette (`vX.Y.Z`, ou `vX.Y.Z-rc.1` pour une préversion) ;
2. relance toute la CI (build, tests unitaires, tests Playwright) : rien n'est publié si elle échoue ;
3. applique la version aux pom (`versions:set`, sans commit) ;
4. publie avec `mvn -Prelease deploy` (profil `release` : jars des sources et de la javadoc) les modules
   `dsfr-faces-parent`, `-resources`, `-core`, `-html`, `-richfaces-compat` et `-migrate`. Le showcase
   n'est pas publié comme paquet Maven (`maven.deploy.skip`) ;
5. crée la release GitHub, avec des notes générées depuis les pull requests et les commits, et ces
   fichiers :
   - `dsfr-faces-migrate-X.Y.Z.jar` et `migrate-fields.sh` : migration facultative, à placer dans le même
     dossier (`sh migrate-fields.sh --dry-run src/main/webapp`) ;
   - `migrate-richfaces.sh` : migration obligatoire (`sed`) ;
   - `dsfr-faces-showcase-X.Y.Z.war` : showcase, déployable sur un serveur Jakarta EE 11.

Aucun secret à configurer : le workflow utilise le `GITHUB_TOKEN` (permissions `contents: write` et
`packages: write`, limitées au job de publication).

Après la release, passer `main` à la version suivante :

```sh
mvn versions:set -DnewVersion=0.2.0-SNAPSHOT -DgenerateBackupPoms=false -DprocessAllModules=true
```

GitHub Packages refuse de republier une version existante. Pour refaire une version ratée, supprimer la
version des paquets dans l'onglet *Packages* du dépôt, supprimer la release et l'étiquette, puis
recommencer.

Pour vérifier le profil `release` en local, sans rien publier :

```sh
mvn -Prelease deploy -DaltDeploymentRepository=github::file://$PWD/target/repo
```

## Utiliser une version publiée

GitHub Packages exige une authentification, **même pour un dépôt public**. Il faut un jeton
GitHub (*personal access token* classique) avec la permission `read:packages`.

`~/.m2/settings.xml` (ou les secrets de la CI de l'application) :

```xml
<settings>
  <servers>
    <server>
      <id>github-dsfr-faces</id>
      <username>VOTRE_LOGIN_GITHUB</username>
      <password>${env.GITHUB_TOKEN}</password>
    </server>
  </servers>
</settings>
```

`pom.xml` de l'application :

```xml
<repositories>
  <repository>
    <id>github-dsfr-faces</id>
    <url>https://maven.pkg.github.com/b-legrand/dsfr-faces</url>
  </repository>
</repositories>

<dependencies>
  <!-- application RichFaces migrée par sed ; sinon dsfr-faces-core (+ dsfr-faces-html) -->
  <dependency>
    <groupId>fr.gouv.dsfr</groupId>
    <artifactId>dsfr-faces-richfaces-compat</artifactId>
    <version>0.1.0</version>
  </dependency>
</dependencies>
```

Dans GitHub Actions, le `GITHUB_TOKEN` d'un autre dépôt ne donne accès au paquet que si ce dépôt a été
autorisé dans les réglages du paquet (*Manage Actions access*).

Un dépôt d'entreprise (Nexus, Artifactory) peut servir de proxy vers ce registre, pour que les
développeurs n'aient pas de jeton à gérer.

## Maven Central (plus tard)

Maven Central ne demande pas d'authentification aux utilisateurs, mais exige de prouver la propriété du
`groupId`. `fr.gouv.dsfr` ne peut être revendiqué que par le propriétaire du domaine `gouv.fr`. Les
options sont :

- `io.github.b-legrand`, vérifié automatiquement par le compte GitHub ;
- un domaine possédé par le projet.

Il faudra aussi :
- un compte sur central.sonatype.com ;
- une signature GPG des artefacts ;
- les éléments `developers` dans le pom ;
- deux secrets dans le dépôt (jeton Central, clé GPG).

Les noms des packages Java (`fr.gouv.dsfr.faces`) ne changeraient pas.
