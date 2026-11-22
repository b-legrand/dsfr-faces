# Feuille de route

But final : migrer une application RichFaces vers le DSFR avec une seule commande `sed` sur les `.xhtml` :
`sed -i -e 's/\<rich:/dsfr:/g' -e 's/xmlns:rich=/xmlns:dsfr=/g'`
(voir [migration-richfaces.md](migration-richfaces.md)).

Légende : ✅ fait · 🟡 partiel · ⬜ à faire

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
  pages ne sont pas modifiées par le `sed`, c'est elle qui est résolue). Fait : `messages`, `message`.
  Les autres balises arrivent avec les composants de la phase 2.
- 🟡 **Taglib `http://richfaces.org/a4j`**. Fait : `a4j:ajax` (forme imbriquée) et
  `a4j:commandButton`, sur l'`AjaxBehavior` standard. Restent : `commandLink`, `outputPanel`, `repeat`,
  `jsFunction` et la forme englobante de `a4j:ajax`.
- ✅ **Fonctions EL** dans la taglib `rich` : `component`, `clientId`, `element`, `findComponent`,
  `isUserInRole`, `jQuerySelector`.
- ✅ **`ajaxRendered`** et **`limitRender`** : un `PartialViewContext` ajoute à chaque réponse ajax les
  composants `ajaxRendered`.
- ✅ **Recherche d'identifiants à la RichFaces** (`render`, `execute`, `for`, fonctions EL) : relative,
  puis conteneurs de nommage englobants, puis toute la vue.
- ✅ **Injection automatique de l'en-tête DSFR** dans les vues sans `dsfr:head` (paramètre
  `fr.gouv.dsfr.richfaces.AUTO_HEAD`).
- ⬜ **API JS minimale** `RichFaces.component(id)` / `#{rich:component}` : `show()`, `hide()` et
  `switchToItem()` branchés sur l'API JS du DSFR.
- ⬜ **Tolérance des attributs** : accepter les attributs RichFaces sans équivalent et avertir en mode
  Development.
- ✅ **Migration facultative** `dsfr-faces-migrate` (`scripts/migrate-fields.sh`) : enveloppe les motifs
  libellé + champ + message dans `dsfr:field` (groupe DSFR), en conservant la mise en forme ; tests
  unitaires et page de démonstration distincte (`/richfaces-fields/`) testée par Playwright.
- ⬜ Migration facultative, niveau suivant : conversion d'un `dsfr:field` en `dsfr:inputText` (change
  l'identifiant client du champ, à relire).
- 🟡 **Script de migration** : `scripts/migrate-richfaces.sh` applique le `sed`. Restent l'inventaire
  (balises, fonctions EL, `RichFaces.*`, skins, CSS `rf-*`) et le rapport de ce qui n'est pas pris en
  charge.
- 🟡 **Application RichFaces de référence** : une page d'origine (`src/main/richfaces/inscription.xhtml`)
  est migrée par le script à chaque build du showcase et testée par Playwright. À étendre à un échantillon
  réaliste pour mesurer la couverture.

## Phase 2 : composants P1

Par ordre de valeur pour une application de gestion :

| Composant | Balises couvertes | État |
|---|---|---|
| Messages de champ | `rich:message` | ✅ `dsfr:message` |
| Tableau + colonnes (tri, lignes alternées, `rows`) | `rich:dataTable`, `rich:column` | ⬜ |
| Pagination | `rich:dataScroller` | ⬜ |
| Panneau | `rich:panel` (carte ou section, à arbitrer) | ⬜ |
| Accordéons | `rich:collapsiblePanel`, `rich:accordion`, `rich:accordionItem` | ⬜ |
| Onglets | `rich:tabPanel`, `rich:tab` (`switchType` client / ajax / server) | ⬜ |
| Modale | `rich:popupPanel` (+ API JS) | ⬜ |
| Date | `rich:calendar` → champ date natif, `datePattern` → convertisseur | ⬜ |
| Liste déroulante | `rich:select`, `dsfr:selectOneMenu` | ⬜ |
| Champs natifs DSFR | `textarea`, `checkbox` (+ groupe), `radio` (+ riche), `toggle`, `password` | ⬜ |
| Lien d'action | `dsfr:commandLink` / `a4j:commandLink` | ⬜ |
| Fieldset | regroupement de champs, erreurs de groupe | ⬜ |

## Phase 2 bis : kit de rendu HTML

- ✅ **Module `dsfr-faces-html`** : enveloppe du kit `HTML_BASIC` qui ajoute les classes DSFR aux balises
  `h:` laissées intactes par le `sed` (champs, listes, libellés, boutons, liens) et rend `h:messages` /
  `h:message` comme leurs équivalents `dsfr:`. Sans lui, la remise à zéro CSS du DSFR rend les
  `h:inputText` invisibles.
- ⬜ `h:dataTable` → tableau DSFR (conteneurs `fr-table` autour de `<table>`), avec `rich:dataTable`.
- ⬜ Cases à cocher et boutons radio `h:` (structure `fr-checkbox-group` / `fr-radio-group` à produire).
- ✅ État d'erreur visible des champs `h:` : par `dsfr:field` (migration facultative). Sans lui, l'erreur
  reste signalée par le message et `aria-invalid`.

## Phase 3 : composants P2 / P3
- ⬜ Composants P2 : `autocomplete` (dégradé), `extendedDataTable`, `fileUpload`, `tooltip`,
  `toolbar`, `dropDownMenu` / menus, `panelMenu` → menu latéral, `inputNumberSpinner`, `inputNumberSlider`,
  `componentControl`, `placeholder`, `focus`.
- ⬜ `a4j:` P2 : `status`, `region`, `param`, `poll`, `actionListener`.
- ⬜ Composants P3 en rendu dégradé : `dataGrid`, `list`, `togglePanel`, `notify`, `inplace*`,
  `progressBar`, `tree`, `pickList`, `orderingList`, `push`.
- ⬜ Mise en page en fichiers de balises : `header`, `footer`, `breadcrumb`, `card`, `tile`, `callout`,
  `highlight`, `badge`, `tag`, `notice`, `skiplinks`, `stepper`, `summary`.
- ⬜ Paramètres d'affichage (thème clair / sombre) via le composant « Paramètres d'affichage » du DSFR.
- ⬜ Alias RichFaces 3.3 (`rich:modalPanel`, `a4j:support`, `rich:simpleTogglePanel`…), si une
  application cible en a besoin.

## Phase 4 : qualité

- ⬜ **Tests unitaires des renderers**, en comparant le HTML produit aux exemples officiels
  (`node_modules/@gouvfr/dsfr/example`), de sorte qu'une montée de version DSFR se lise comme un diff.
- ⬜ **Tests d'intégration** navigateur (Playwright ou Selenium) : validation ajax, envoi, modales, onglets.
- ⬜ **Matrice de compatibilité** pour tenir la promesse d'indépendance vis-à-vis du serveur :
  - Mojarra et MyFaces ;
  - GlassFish, WildFly, Payara, Open Liberty, TomEE, Tomcat + Mojarra + Weld.
- ⬜ Vérifier les anciens espaces de noms (`java.sun.com`, `xmlns.jcp.org`) sous **MyFaces 4.1**
  (vérifié sous Mojarra 4.1.17 uniquement).
- ⬜ **Audits d'accessibilité automatisés** (axe-core) sur le showcase, et grille RGAA pour les composants.
- ⬜ Intégration continue : build, tests, audits.

## Phase 5 : publication

- ⬜ Choisir la licence du code dsfr-faces (le DSFR lui-même reste réservé aux sites de l'État).
- ⬜ Publication sur Maven Central (`groupId` définitif, signature).
- ⬜ Politique de version : suivre les versions mineures du DSFR (`dsfr-faces 1.15.x` ↔ DSFR 1.15).
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
| `RichFaces.component()` | produit par `#{rich:component}`, mais l'objet JS `RichFaces` n'existe pas encore |
| GNU sed requis | `scripts/migrate-richfaces.sh` et le build du showcase utilisent `\<` et `sed -i` (GNU) |
