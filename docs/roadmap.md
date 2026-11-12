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
- 🟡 `dsfr:messages` : alertes DSFR. Pas encore `ajaxRendered`.
- ✅ Showcase vérifié dans Chrome sur GlassFish 8.0.5 / Mojarra 4.1.17.
- ✅ Documentation en français.

## Phase 1 : couche de compatibilité RichFaces (rend le `sed` possible)

C'est la phase qui conditionne l'objectif. Sans elle, le `sed` produit des pages qui ne se chargent pas.

- ⬜ **Taglib `http://richfaces.org/rich`** reprenant les noms de balises RichFaces 4.5, servie par un
  module `dsfr-faces-richfaces-compat`. Les URI des pages n'étant pas modifiées par le `sed`, c'est elle
  qui sera résolue.
- ⬜ **Taglib `http://richfaces.org/a4j`** : `a4j:ajax`, `commandButton`, `commandLink`,
  `outputPanel`, `repeat`, `jsFunction`.
- ⬜ **Fonctions EL** dans la taglib `rich` : `component`, `clientId`, `element`, `findComponent`,
  `isUserInRole`, `jQuerySelector`.
- ⬜ **`ajaxRendered`** : un `PartialViewContext` personnalisé ajoute à chaque réponse ajax les
  composants `ajaxRendered` (`messages`, `message`, `a4j:outputPanel`).
- ⬜ **API JS minimale** `RichFaces.component(id)` / `#{rich:component}` : `show()`, `hide()` et
  `switchToItem()` branchés sur l'API JS du DSFR.
- ⬜ **Tolérance des attributs** : accepter les attributs RichFaces sans équivalent et avertir en mode
  Development.
- ⬜ **Script `dsfr-faces-migrate`** : inventaire (balises, fonctions EL, `RichFaces.*`, skins, CSS
  `rf-*`), application du `sed`, rapport de ce qui n'est pas pris en charge.
- ⬜ **Application RichFaces de référence** (échantillon réaliste) migrée en CI pour mesurer la couverture.

## Phase 2 : composants P1

Par ordre de valeur pour une application de gestion :

| Composant | Balises couvertes | État |
|---|---|---|
| Messages de champ | `rich:message` | ⬜ |
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

## Phase 3 : kit de rendu HTML et composants P2 / P3

- ⬜ **Module `dsfr-faces-html`** (optionnel) : renderers `HTML_BASIC` remplacés pour appliquer les
  classes DSFR aux balises `h:` laissées intactes par le `sed`.
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
| Aucun test automatisé | uniquement une vérification manuelle dans Chrome pour l'instant |
| Marge des alertes | les alertes DSFR n'ont pas de marge externe : prévoir `alertStyleClass` (p. ex. `fr-mb-3w`) sur `dsfr:messages` |
| Version DSFR en double | dans `dsfr-faces-resources/package.json` et `pom.xml` (`<dsfr.version>`) ; à lire depuis `node_modules/@gouvfr/dsfr/package.json` au build |
| Taille du jar de ressources | 5 Mo, car tout `dist/` est copié (y compris `dist/dsfr/` en double et les CSS par composant) ; à réduire au strict nécessaire |
| Script en ligne de `dsfr:head` | incompatible avec une CSP stricte sans `scheme=""` ; prévoir un nonce |
| `jsessionid` dans les URL de ressources | à la première visite, par `encodeResourceURL` (comportement standard), qui gêne le cache |
| Mention « champ obligatoire » | astérisque dans le libellé ; à rendre configurable (texte, ou mention globale seule) |
| Séparateur `-` | les `id` de composants ne doivent pas contenir `-` ; le documenter dans le script de migration |
| Mode « succès » | Faces n'a pas de sévérité succès : convention à définir (p. ex. `INFO` + attribut) pour `fr-alert--success` / `fr-message--valid` |
| Ajax : source = enveloppe | vérifié sous Mojarra ; à vérifier sous MyFaces |
