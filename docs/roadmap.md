# Feuille de route

But final : migrer une application RichFaces vers le DSFR avec une seule commande `sed` sur les `.xhtml` :
`sed -i -e 's/\<rich:/dsfr:/g' -e 's/xmlns:rich=/xmlns:dsfr=/g'`
(voir [migration-richfaces.md](migration-richfaces.md)).

Légende : ✅ fait · 🟡 partiel · ⬜ à faire

## Prochaines priorités (après la 0.1.0)

Une page migrée par le `sed` qui contient une balise `rich:` ou `a4j:` non couverte ne se charge pas du
tout. L'ordre proposé suit donc ce qui casse le plus de pages :

1. ✅ **`a4j:` P2** : `outputPanel`, `status`, `region`, `param`, `jsFunction`, `poll`, `repeat`,
   `actionListener` et la forme englobante de `a4j:ajax`.
2. **Inventaire avant migration** dans le script de migration. Lancé sur une application réelle, il donne
   l'ordre de traitement des composants P2 / P3.
3. **Mise en page DSFR** : `header`, `footer`, `breadcrumb`, `skiplinks`, nécessaires à toute
   application DSFR.
4. **MyFaces dans la matrice e2e**, avant d'ajouter d'autres composants. Les balises `a4j:` reposent sur
   des points à vérifier sous MyFaces : `params` de `faces.ajax.request`, ordre des écouteurs
   `faces.ajax.addOnEvent`, et soumission partielle.

## Phase 0 : fondations (v0.1)

- ✅ Build Maven multi-module : `resources`, `core` et `showcase` ; DSFR récupéré par npm dans le cycle de vie Maven (frontend-maven-plugin, `DSFR_ACCEPT_LICENSE=1`).
- ✅ Cible Jakarta Faces 4.1 / Java 17, API en `provided`, sans dépendance à un serveur.
- ✅ Ressources DSFR 1.15.4 servies statiquement depuis `META-INF/resources/dsfr`.
- ✅ `dsfr:head` : CSS, icônes, favicons, JS module et `nomodule`, thème.
- ✅ `dsfr:inputText` : groupe, aide, messages, ARIA, conversion par type, `f:ajax`.
- ✅ `dsfr:button` : priorités, tailles, icônes, ajax ou non.
- ✅ `dsfr:messages` : alertes DSFR, `ajaxRendered` par défaut.
- ✅ `dsfr:message` : messages DSFR d'un composant, `ajaxRendered` par défaut.
- ✅ Tests d'intégration Playwright (Chromium, Firefox) via `docker compose`, versions image / bibliothèque
  vérifiées.
- ✅ Showcase vérifié dans Chrome sur GlassFish 8.0.5 / Mojarra 4.1.17.
- ✅ Documentation en français.

## Phase 1 : couche de compatibilité RichFaces (rend le `sed` possible)

C'est la phase qui conditionne l'objectif. Sans elle, le `sed` produit des pages qui ne se chargent pas.

- 🟡 **Taglib `http://richfaces.org/rich`** dans le module `dsfr-faces-richfaces-compat` (les URI des
  pages ne sont pas modifiées par le `sed`, c'est elle qui est résolue). Fait : `messages`, `message`,
  `dataTable`, `column`, `dataScroller`, `panel`, `collapsiblePanel`, `accordion`, `accordionItem`,
  `tabPanel`, `tab`, `popupPanel`, `calendar`, `select`. Restent les balises P2 / P3 (phase 3).
- ✅ **Taglib `http://richfaces.org/a4j`**, sur l'`AjaxBehavior` standard et `faces.ajax.request` :
  - `ajax` (formes imbriquée et englobante), `commandButton`, `commandLink` ;
  - `outputPanel`, `status` (nommé ou non), `region` (`@region`), `param` (`assignTo`, `noEscape`) ;
  - `jsFunction`, `poll`, `repeat`, `actionListener`.

  Page de démonstration `richfaces/a4j.xhtml`, testée par `richfaces-a4j.spec.ts`. Restent `a4j:push`,
  `a4j:queue` / `attachQueue` / `log` et `a4j:mediaOutput` (phase 3).
- ✅ **Fonctions EL** dans la taglib `rich` : `component`, `clientId`, `element`, `findComponent`,
  `isUserInRole`, `jQuerySelector`.
- ✅ **`ajaxRendered`** et **`limitRender`** : un `PartialViewContext` ajoute à chaque réponse ajax les
  composants `ajaxRendered`.
- ✅ **Recherche d'identifiants à la RichFaces** (`render`, `execute`, `for`, fonctions EL) : relative,
  puis conteneurs de nommage englobants, puis toute la vue.
- ✅ **Injection automatique de l'en-tête DSFR** dans les vues sans `dsfr:head` (paramètre
  `fr.gouv.dsfr.richfaces.AUTO_HEAD`).
- ✅ **API JS minimale** `RichFaces.component(id)` / `#{rich:component}` : `show()`, `hide()`,
  `switchToItem()` et `switchPanel()`, sur l'API `DsfrFaces.component(id)` et celle du DSFR.
- ⬜ **Tolérance des attributs** : accepter les attributs RichFaces sans équivalent et avertir en mode
  Development.
- ✅ **Migration facultative** `dsfr-faces-migrate` (`scripts/migrate-fields.sh`) : enveloppe les motifs
  libellé + champ + message dans `dsfr:field` (groupe DSFR), en conservant la mise en forme ; tests
  unitaires et page de démonstration distincte (`/richfaces-fields/`) testée par Playwright.
- ⬜ Migration facultative, niveau suivant : conversion d'un `dsfr:field` en `dsfr:inputText` (change
  l'identifiant client du champ, à relire).
- 🟡 **Script de migration** : `scripts/migrate-richfaces.sh` applique le `sed`, et la release le joint
  avec l'outil facultatif. Restent l'inventaire (balises, fonctions EL, `RichFaces.*`, skins, CSS `rf-*`)
  et le rapport de ce qui n'est pas pris en charge, à lancer avant la migration (priorité 2).
- 🟡 **Application RichFaces de référence** : une page d'origine (`src/main/richfaces/inscription.xhtml`)
  est migrée par le script à chaque build du showcase et testée par Playwright. À étendre à un échantillon
  réaliste pour mesurer la couverture.

## Phase 2 : composants P1

Par ordre de valeur pour une application de gestion :

| Composant | Balises couvertes | État |
|---|---|---|
| Messages de champ | `rich:message` | ✅ `dsfr:message` |
| Tableau + colonnes (tri, lignes alternées, `rows`) | `rich:dataTable`, `rich:column` | ✅ `dsfr:dataTable`, `dsfr:column` (tri simple, pas de filtres) |
| Pagination | `rich:dataScroller` | ✅ `dsfr:dataScroller` |
| Panneau | `rich:panel` | ✅ `dsfr:panel` (carte DSFR sans lien) |
| Accordéons | `rich:collapsiblePanel`, `rich:accordion`, `rich:accordionItem` | ✅ (changement côté client, état gardé) |
| Onglets | `rich:tabPanel`, `rich:tab` (`switchType` client / ajax / server) | ✅ `dsfr:tabPanel`, `dsfr:tab` |
| Modale | `rich:popupPanel` (+ API JS) | ✅ `dsfr:popupPanel` |
| Date | `rich:calendar` → champ date natif | ✅ `dsfr:calendar` |
| Liste déroulante | `rich:select`, `dsfr:selectOneMenu` | ✅ |
| Champs natifs DSFR | `textarea`, `checkbox` (+ groupe), `radio` (+ riche), `toggle`, `password` | ✅ |
| Lien d'action | `dsfr:commandLink` / `a4j:commandLink` | ✅ |
| Fieldset | regroupement de champs, erreurs de groupe | ✅ `dsfr:fieldset` |

Limites connues de la phase 2 :

- **Tableaux :** pas de filtres (`filterValue`, `filterExpression`), pas de tri multiple, pas de
  `rich:columnGroup` / `rich:collapsibleSubTable` ; `rowKeyVar` et `iterationStatusVar` sont ignorés.
- **Accordéons :** `switchType` ajax / server non pris en charge (tout est rendu, changement côté client).
- **`rich:tabPanel` :** pas d'`itemChangeListener`. En mode ajax, quitter un onglet ne met pas à jour le
  modèle avec ce qui y a été saisi (comme un changement `immediate`).
- **`rich:popupPanel` :** `domElementAttachment` est ignoré ; la modale doit contenir son propre
  formulaire si elle est placée hors d'un formulaire.
- ~~**Liens :** `f:param` d'un `dsfr:commandLink` n'est transmis qu'en envoi complet.~~ Levée : les
  `f:param` (et `a4j:param`) des commandes sont aussi envoyés en ajax.

## Phase 2 bis : kit de rendu HTML

- ✅ **Module `dsfr-faces-html`** : enveloppe du kit `HTML_BASIC` qui ajoute les classes DSFR aux balises
  `h:` laissées intactes par le `sed` (champs, listes, libellés, boutons, liens) et rend `h:messages` /
  `h:message` comme leurs équivalents `dsfr:`. Sans lui, la remise à zéro CSS du DSFR rend les
  `h:inputText` invisibles.
- ✅ `h:dataTable` → tableau DSFR (même rendu que `dsfr:dataTable`, sans tri).
- ⬜ Cases à cocher et boutons radio `h:` (structure `fr-checkbox-group` / `fr-radio-group` à produire).
- ✅ État d'erreur visible des champs `h:` : par `dsfr:field` (migration facultative). Sans lui, l'erreur
  reste signalée par le message et `aria-invalid`.

## Phase 3 : composants P2 / P3
- ⬜ Composants P2 : `autocomplete` (dégradé), `extendedDataTable`, `fileUpload`, `tooltip`,
  `toolbar`, `dropDownMenu` / menus, `panelMenu` → menu latéral, `inputNumberSpinner`, `inputNumberSlider`,
  `componentControl`, `placeholder`, `focus`.
- ⬜ `a4j:` P3 : `push` (→ `f:websocket`), `queue` / `attachQueue` / `log` (à ignorer avec un avertissement),
  `mediaOutput`.
- ⬜ `a4j:status` limité au formulaire ou à la région qui le contient (aujourd'hui : global ou nommé) ;
  `rich:dataScroller` sur un `a4j:repeat`.
- ⬜ Tableaux : filtres (`filterValue`, `filterExpression`), tri multiple, `rich:columnGroup`,
  `rich:collapsibleSubTable`, `rowKeyVar`.
- ⬜ Composants P3 en rendu dégradé : `dataGrid`, `list`, `togglePanel`, `notify`, `inplace*`,
  `progressBar`, `tree`, `pickList`, `orderingList`, `push`.

Composants DSFR sans équivalent RichFaces :

- ⬜ Mise en page de l'application : `header` (en-tête, Marianne, navigation principale), `footer`,
  `breadcrumb`, `skiplinks` (exigés par le RGAA). Priorité 3.
- ⬜ Paramètres d'affichage (thème clair / sombre) via le composant « Paramètres d'affichage » du DSFR.
- ⬜ Contenus, en fichiers de balises : `card`, `tile`, `callout`, `highlight`, `badge`, `tag`, `notice`,
  `stepper` (formulaires en plusieurs étapes), `summary`.
- ⬜ Autres composants DSFR, selon les besoins :
  - barre de recherche, groupe de boutons, contrôle segmenté ;
  - curseur (pourra servir à `rich:inputNumberSlider`) ;
  - gestionnaire de consentement ;
  - lien de téléchargement, citation, transcription ;
  - bouton FranceConnect.
- ⬜ Sévérité « succès » (`fr-alert--success`, `fr-message--valid`) : voir « Mode succès » dans la dette.
- ⬜ Alias RichFaces 3.3 (`rich:modalPanel`, `a4j:support`, `rich:simpleTogglePanel`…), si une
  application cible en a besoin.

## Phase 4 : qualité

- ⬜ **Tests unitaires des renderers**, en comparant le HTML produit aux exemples officiels
  (`node_modules/@gouvfr/dsfr/example`), de sorte qu'une montée de version DSFR se lise comme un diff.
- ✅ **Tests d'intégration** Playwright (Chromium, Firefox) : validation ajax, envoi, tableaux, pagination,
  modales, onglets, accordéons, migrations obligatoire et facultative.
- ⬜ **Matrice de compatibilité** pour tenir la promesse d'indépendance vis-à-vis du serveur :
  - Mojarra et MyFaces ;
  - GlassFish, WildFly, Payara, Open Liberty, TomEE, Tomcat + Mojarra + Weld.
- ⬜ Vérifier les anciens espaces de noms (`java.sun.com`, `xmlns.jcp.org`) sous **MyFaces 4.1**
  (vérifié sous Mojarra 4.1.17 uniquement).
- ⬜ **Audits d'accessibilité automatisés** (axe-core) sur le showcase, et grille RGAA pour les composants.
- ✅ **Intégration continue** (GitHub Actions, `.github/workflows/ci.yml`) : build Maven, tests unitaires,
  tests Playwright par `docker compose`, rapports joints en cas d'échec. Voir [publication.md](publication.md).
- ⬜ Audits (axe-core) dans la CI, et matrice de serveurs en jobs parallèles.
- ⬜ Mises à jour automatiques des dépendances (Dependabot ou Renovate) : actions GitHub, plugins Maven,
  DSFR, Playwright (image et bibliothèque ensemble).

## Phase 5 : publication

- ✅ Licence du code dsfr-faces : **MIT** (`LICENSE`). Le DSFR embarqué garde la sienne, réservée aux
  sites de l'État.
- ✅ **Release automatisée** (`.github/workflows/release.yml`, étiquette `vX.Y.Z`) : la CI complète doit
  passer, puis les jars, avec leurs sources et leur javadoc, sont publiés sur **GitHub Packages**. Une
  release GitHub joint l'outil de migration, les scripts et le WAR du showcase. Première version :
  **0.1.0**.
- ⬜ Publication sur Maven Central, pour que les utilisateurs n'aient plus besoin de jeton. Il faut un
  `groupId` vérifiable (`fr.gouv.dsfr` ne l'est pas sans le domaine `gouv.fr`, par exemple
  `io.github.b-legrand`), une signature GPG et les éléments `developers` du pom.
- 🟡 Politique de version : `0.x` tant que l'API peut changer. Ensuite, suivre les versions mineures du
  DSFR (`dsfr-faces 1.15.x` ↔ DSFR 1.15).
- ⬜ Showcase en ligne, une page par composant, avec le code à copier.
- ⬜ Guide de migration illustré par l'application de référence de la phase 1.

## Dette et points connus

| Sujet | Détail |
|---|---|
| Classes ajoutées par `dsfr-faces-html` | repèrent l'élément principal par le nom de balise et le composant passé à `startElement` ; vérifié sous Mojarra, à vérifier sous MyFaces |
| Tests unitaires | seulement pour l'outil de migration (`dsfr-faces-migrate`) ; les composants ne sont couverts que par les tests Playwright, sous GlassFish / Mojarra uniquement |
| Marge des alertes | les alertes DSFR n'ont pas de marge externe : prévoir `alertStyleClass` (p. ex. `fr-mb-3w`) sur `dsfr:messages` |
| Version DSFR en double | dans `dsfr-faces-resources/package.json` et `pom.xml` (`<dsfr.version>`) ; à lire depuis `node_modules/@gouvfr/dsfr/package.json` au build |
| Taille du jar de ressources | 5 Mo, car tout `dist/` est copié (y compris `dist/dsfr/` en double et les CSS par composant) ; à réduire au strict nécessaire |
| Script en ligne de `dsfr:head` | incompatible avec une CSP stricte sans `scheme=""` ; prévoir un nonce |
| `jsessionid` dans les URL de ressources | à la première visite, par `encodeResourceURL` (comportement standard), qui gêne le cache |
| Mention « champ obligatoire » | astérisque dans le libellé ; à rendre configurable (texte, ou mention globale seule) |
| Séparateur `-` | les `id` de composants ne doivent pas contenir `-` ; le documenter dans le script de migration |
| Mode « succès » | Faces n'a pas de sévérité succès : convention à définir (p. ex. `INFO` + attribut) pour `fr-alert--success` / `fr-message--valid` |
| Ajax : source = enveloppe | vérifié sous Mojarra ; à vérifier sous MyFaces |
| `ajaxRendered` sous MyFaces | suppose que `PartialViewContext.getRenderIds()` est modifiable (exigé par la spécification, vérifié sous Mojarra) |
| Coût de `ajaxRendered` | un parcours de l'arbre par réponse ajax ; à mesurer sur de grosses vues |
| Scripts en ligne | onglets, accordéons, pagination, tri et liens utilisent des gestionnaires `onclick` en ligne : incompatible avec une CSP stricte, comme les comportements `f:ajax` |
| Attributs lus dans la table d'attributs | les nouveaux composants ne déclarent pas de propriétés Java pour leurs attributs de rendu (`label`, `hint`…) : les renderers les lisent avec `getAttributes()` |
| GNU sed requis | `scripts/migrate-richfaces.sh` et le build du showcase utilisent `\<` et `sed -i` (GNU) |
