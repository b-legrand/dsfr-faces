# Démarrage

## Prérequis

| Outil | Version |
|---|---|
| Java | 17 ou plus (exigence de Jakarta EE 11) |
| Maven | 3.9 |
| Node.js / npm | inutile : le build télécharge sa propre version (voir ci-dessous) |
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
Jakarta EE 11. Exemple jetable avec GlassFish 8 (implémentation de référence, qui embarque Mojarra 4.1),
sans rien ajouter au projet :

```sh
mkdir -p /tmp/dsfr-faces-deploy && chmod 777 /tmp/dsfr-faces-deploy
cp dsfr-faces-showcase/target/dsfr-faces-showcase.war /tmp/dsfr-faces-deploy/
docker run -d --name dsfr-faces-gf --network host \
  -v /tmp/dsfr-faces-deploy:/deploy ghcr.io/eclipse-ee4j/glassfish:latest
# http://localhost:8080/dsfr-faces-showcase/
```

- Redéployer : recopier le WAR dans `/tmp/dsfr-faces-deploy/` (autodeploy).
- Arrêter : `docker rm -f dsfr-faces-gf`.
- Ne pas monter le WAR directement dans `domains/domain1/autodeploy` : l'image croirait le domaine
  déjà créé et ne démarrerait pas.

Le showcase a été vérifié dans Chrome (GlassFish 8.0.5, Mojarra 4.1.17) :

- ressources DSFR, polices Marianne et icônes chargées, JS DSFR démarré ;
- validation ajax au blur et à l'envoi ;
- envoi complet de la page ;
- conversion automatique en `Integer` ;
- arbre d'accessibilité correct ;
- aucune erreur dans la console.
