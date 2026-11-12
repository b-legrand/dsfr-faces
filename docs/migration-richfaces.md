# Migration depuis RichFaces

## Objectif

Migrer l'interface d'une application RichFaces 4.x vers le DSFR **sans réécrire ses pages** :

```sh
find src/main/webapp -name '*.xhtml' -exec sed -i \
  -e 's/\<rich:/dsfr:/g' \
  -e 's/xmlns:rich=/xmlns:dsfr=/g' {} +
```

- **Les deux expressions sont nécessaires.** La déclaration `xmlns:rich="…"` contient `rich=` et
  non `rich:` : avec `s/rich:/dsfr:/g` seul, elle reste inchangée, et les balises `dsfr:` utilisent
  un préfixe non déclaré. Le XML est alors invalide et la page ne se charge pas.
- `\<` (début de mot, GNU sed) évite de toucher un mot qui se terminerait par `rich:` (p. ex.
  `enrich:`). Sur macOS (BSD sed), utiliser `gsed`, ou `[[:<:]]` à la place de `\<`.

Pour que cette commande suffise, dsfr-faces doit fournir une **couche de compatibilité RichFaces** :
mêmes noms de balises, mêmes attributs, mêmes fonctions EL, même API JavaScript de base, mais un rendu
DSFR. Cette couche n'existe pas encore : voir la [feuille de route](roadmap.md), phase 1.

## Ce que fait réellement le `sed`

Prenons une page RichFaces typique :

```xml
<html xmlns:h="http://java.sun.com/jsf/html"
      xmlns:a4j="http://richfaces.org/a4j"
      xmlns:rich="http://richfaces.org/rich">
  <rich:dataTable value="#{bean.lignes}" var="l" rows="10" id="table">
    <rich:column><f:facet name="header">Nom</f:facet>#{l.nom}</rich:column>
  </rich:dataTable>
  <rich:dataScroller for="table"/>
  <a4j:commandButton value="Ouvrir" oncomplete="#{rich:component('popup')}.show()"/>
  <rich:popupPanel id="popup">…</rich:popupPanel>
</html>
```

Après le `sed` :

```xml
<html xmlns:h="http://java.sun.com/jsf/html"
      xmlns:a4j="http://richfaces.org/a4j"
      xmlns:dsfr="http://richfaces.org/rich">        <!-- l'URI n'a pas changé -->
  <dsfr:dataTable value="#{bean.lignes}" var="l" rows="10" id="table">
    <dsfr:column>…</dsfr:column>
  </dsfr:dataTable>
  <dsfr:dataScroller for="table"/>
  <a4j:commandButton … oncomplete="#{dsfr:component('popup')}.show()"/>   <!-- fonction EL renommée -->
  <dsfr:popupPanel id="popup">…</dsfr:popupPanel>
</html>
```

Ce qu'il faut en retenir :

0. **La déclaration est renommée par la seconde expression** (`xmlns:rich=` → `xmlns:dsfr=`) ; la
   première renomme les balises et les fonctions EL.
1. **En XML, seul l'URI compte, pas le préfixe.** `xmlns:dsfr="http://richfaces.org/rich"` reste lié
   à l'URI RichFaces. dsfr-faces doit donc déclarer une taglib de compatibilité dont l'espace de noms
   est **`http://richfaces.org/rich`**. Une fois cette taglib disponible, le `sed` devient un simple
   renommage pour la lisibilité : les pages fonctionneraient même sans lui.
2. **`a4j:` n'est pas touché.** Il ne faut pas ajouter `s/a4j:/dsfr:/` : `xmlns:a4j` deviendrait un
   second `xmlns:dsfr` sur la même balise, ce qui est un attribut dupliqué et donc un XML invalide.
   dsfr-faces doit fournir une seconde taglib à l'URI **`http://richfaces.org/a4j`**.
3. **Les fonctions EL sont renommées aussi** : `#{rich:component('x')}` devient
   `#{dsfr:component('x')}`. La taglib de compatibilité doit donc déclarer `component`, `clientId`,
   `element`, `findComponent`, `isUserInRole` et `jQuerySelector`.
4. **Les balises `h:`, `f:` et `ui:` ne bougent pas.** Les anciens espaces de noms
   (`http://java.sun.com/jsf/*`, `http://xmlns.jcp.org/jsf/*`) sont toujours acceptés par
   Mojarra 4.1.17 (vérifié sur GlassFish 8.0.5). MyFaces 4.1 reste à vérifier. En revanche, un
   `h:inputText` ou `h:commandButton` garde son rendu HTML standard, sans classes DSFR : voir le
   [kit de rendu HTML](#les-composants-h-standard).

## Prérequis hors `sed`

RichFaces 4.5 cible JSF 2.x (`javax.faces`), alors que dsfr-faces cible Jakarta Faces 4.1
(`jakarta.faces`). Avant le `sed`, il faut donc :

| Étape | Outil | Remarque |
|---|---|---|
| Passer de `javax.*` à `jakarta.*` (Java, `web.xml`, `faces-config.xml`, `beans.xml`) | OpenRewrite (`org.openrewrite.java.migrate.jakarta.JakartaEE10`) ou Eclipse Transformer | Les `.xhtml` ne sont presque pas concernés |
| Remplacer les dépendances RichFaces par `dsfr-faces-core` (+ compatibilité) | `pom.xml` | |
| Remplacer les managed beans JSF (`@ManagedBean`, supprimés dans Faces 4.0) | CDI `@Named` + scopes CDI | OpenRewrite propose une recette |
| Retirer les paramètres `org.richfaces.*` du `web.xml` | à la main | skin, `enableControlSkinning`, push… |
| Adapter le code Java qui utilise l'API RichFaces | à la main | `org.richfaces.model.*` (`SortOrder`, `ExtendedDataModel`, `Filter`), `UIDataTable`, `ExtendedDataTableState`… |
| Remplacer les usages de skin dans les pages ou CSS (`#{richSkin.*}`, `#{a4jSkin.*}`) | à la main | variables CSS du DSFR |
| Remplacer le CSS spécifique (`.rf-*`, `.rich-*`) | à la main | ne s'applique plus |

## Inventaire avant migration

Mesurer ce que l'application utilise avant de lancer le `sed` :

```sh
# balises RichFaces et a4j par fréquence
grep -rhoE '<(rich|a4j):[a-zA-Z]+' src/main/webapp | sort | uniq -c | sort -rn
# fonctions EL et API JS RichFaces
grep -rnoE 'rich:(component|clientId|element|findComponent|isUserInRole|jQuerySelector)\(' src/main/webapp
grep -rnoE 'RichFaces\.[a-zA-Z.$]+' src/main/webapp
# skins et CSS RichFaces
grep -rnoE '(richSkin|a4jSkin)\.[a-zA-Z]+|\.(rf|rich)-[a-z-]+' src/main/webapp
```

Un futur script `dsfr-faces-migrate` (feuille de route, phase 1) produira ce rapport et signalera les
balises ou attributs sans équivalent.

## Correspondances `rich:` → DSFR

Légende de priorité :
- **P1** : indispensable pour migrer une application de gestion typique ;
- **P2** : courant ;
- **P3** : rare ou sans équivalent DSFR ;
- **HP** : hors périmètre.

| Balise RichFaces 4.5 | Composant DSFR | Prio. | Remarques |
|---|---|---|---|
| `rich:messages` | Alerte (`fr-alert`) | P1 | `dsfr:messages` existe. Il reste à le rendre `ajaxRendered` par défaut, comme RichFaces. |
| `rich:message` | Message de champ (`fr-message`) | P1 | |
| `rich:dataTable`, `rich:column` | Tableau (`fr-table`) | P1 | `sortBy`, `sortOrder`, `filterValue`, `rows`, `rowClasses`… |
| `rich:dataScroller` | Pagination (`fr-pagination`) | P1 | lié à la table par `for` |
| `rich:panel` | Carte (`fr-card`) ou section avec titre | P1 | rendu à arbitrer : facette `header` → titre |
| `rich:collapsiblePanel` | Accordéon (`fr-accordion`) | P1 | |
| `rich:accordion`, `rich:accordionItem` | Groupe d'accordéons | P1 | |
| `rich:tabPanel`, `rich:tab` | Onglets (`fr-tabs`) | P1 | `switchType` client / ajax / server |
| `rich:popupPanel` | Modale (`fr-modal`) | P1 | `show()`/`hide()` via l'API JS de compatibilité |
| `rich:calendar` | Champ date natif (`fr-input`, `type="date"`) | P1 | `datePattern` → convertisseur ; perte du calendrier riche |
| `rich:select` | Liste déroulante (`fr-select`) | P1 | sans saisie filtrante |
| `rich:autocomplete` | Champ + `datalist` | P2 | pas d'équivalent DSFR, rendu dégradé |
| `rich:extendedDataTable` | Tableau (`fr-table`) | P2 | sans colonnes figées ni redimensionnables |
| `rich:fileUpload` | Ajout de fichier (`fr-upload`) | P2 | |
| `rich:tooltip` | Infobulle (`fr-tooltip`) | P2 | |
| `rich:toolbar`, `rich:toolbarGroup` | Groupe de boutons (`fr-btns-group`) | P2 | |
| `rich:dropDownMenu`, `rich:menuItem`, `rich:menuGroup`, `rich:menuSeparator` | Navigation principale (`fr-nav`, `fr-menu`) | P2 | |
| `rich:panelMenu` et ses enfants | Menu latéral (`fr-sidemenu`) | P2 | profondeur limitée |
| `rich:inputNumberSpinner` | Champ `type="number"` | P2 | |
| `rich:inputNumberSlider` | Curseur (`fr-range`) | P2 | |
| `rich:validator`, `rich:graphValidator` | aucun (validation serveur Bean Validation) | P2 | pas de validation côté client |
| `rich:componentControl` | appel de l'API JS | P2 | ouvrir ou fermer une modale, changer d'onglet |
| `rich:placeholder` | attribut `placeholder` | P2 | |
| `rich:focus` | `autofocus` / script | P2 | |
| `rich:hotKey` | script | P3 | |
| `rich:dataGrid`, `rich:list` | grille DSFR + tuiles ou cartes, listes | P3 | |
| `rich:collapsibleSubTable`, `rich:columnGroup` | `fr-table` (en-têtes groupés) | P3 | |
| `rich:togglePanel`, `rich:togglePanelItem`, `rich:toggleControl` | aucun | P3 | rendu de l'élément actif uniquement |
| `rich:notify*` | Bandeau (`fr-notice`) ou alerte | P3 | |
| `rich:inplaceInput`, `rich:inplaceSelect` | champ classique | P3 | dégradé |
| `rich:progressBar` | aucun (`<progress>` natif) | P3 | |
| `rich:tree`, `rich:treeNode` | aucun (`fr-sidemenu` imbriqué) | P3 | dégradé |
| `rich:pickList`, `rich:orderingList` | aucun (cases à cocher / select multiple) | P3 | dégradé |
| `rich:hashParam`, `rich:jQuery` | — | P3 | pas de jQuery dans le DSFR |
| `rich:editor` | — | HP | éditeur riche tiers (CKEditor) à intégrer à part |
| `rich:contextMenu`, `rich:dragSource`, `rich:dropTarget`, `rich:dragIndicator`, `rich:chart` | — | HP | |

## Correspondances `a4j:` (espace de noms conservé)

| Balise | Implémentation prévue | Prio. |
|---|---|---|
| `a4j:ajax` | gestionnaire de balise délégant à `f:ajax` (`render`, `execute`, `oncomplete`, `onbegin`, `listener`, `limitRender`, `status`) | P1 |
| `a4j:commandButton` | bouton DSFR + ajax implicite | P1 |
| `a4j:commandLink` | lien DSFR (`fr-link`) + ajax implicite | P1 |
| `a4j:outputPanel` | `div`/`span` ; `ajaxRendered="true"` rafraîchi à chaque requête ajax | P1 |
| `a4j:repeat` | `ui:repeat` | P1 |
| `a4j:jsFunction` | `h:commandScript` (Faces 2.3+) | P1 |
| `a4j:status` | indicateur via `faces.ajax.addOnEvent` | P2 |
| `a4j:region` | portée d'`execute` | P2 |
| `a4j:param` | `f:param` + `assignTo` | P2 |
| `a4j:poll` | minuterie + `faces.ajax.request` | P2 |
| `a4j:actionListener` | `f:actionListener` | P2 |
| `a4j:push` | `f:websocket` | P3 |
| `a4j:queue`, `a4j:attachQueue`, `a4j:log` | ignorés (avertissement en mode Development) | P3 |
| `a4j:mediaOutput` | — | HP |

## Les composants `h:` standard

Le `sed` ne touche pas `h:inputText`, `h:selectOneMenu`, `h:commandButton`, `h:dataTable`… alors qu'ils
sont majoritaires dans les pages RichFaces. Un module optionnel **`dsfr-faces-html`** (feuille de
route, phase 2) remplacera les renderers du kit `HTML_BASIC` pour leur appliquer les classes DSFR
(`fr-input`, `fr-select`, `fr-btn`, `fr-label`, `fr-table`…).

Limite : `h:outputLabel` et `h:message` étant des composants séparés du champ, ce mode ne peut pas
produire le groupe complet (`fr-input-group`, aide, messages reliés en ARIA). Il donne des pages
présentables, et le passage aux balises `dsfr:` reste recommandé pour la conformité RGAA.

## Comportements RichFaces implicites à reproduire

- `rich:messages` et `rich:message` sont rafraîchis à **chaque** requête ajax (`ajaxRendered`), sans
  `render` explicite. Sans ce comportement, une application migrée perdrait ses messages d'erreur ajax.
- `a4j:commandButton` et `a4j:commandLink` sont ajax par défaut avec `execute="@form"`.
- `render` accepte des identifiants séparés par des virgules et des expressions EL.
- `#{rich:component('id')}` renvoie un objet JS avec `show()`, `hide()`, `switchToItem()`… Une petite
  API `RichFaces.component(id)` sera fournie au-dessus du JS du DSFR (`dsfr(el).modal.disclose()`…).

## Attributs sans équivalent

Les attributs de présentation RichFaces (`headerClass`, `styleClass`, `style`, `width`, `height`,
`skin`…) seront acceptés pour ne pas casser les pages. Ils seront ignorés, ou appliqués quand c'est
pertinent, avec un avertissement en mode `Development` pour aider à nettoyer les pages progressivement.

## Applications RichFaces 3.3

RichFaces 3.3 utilise les mêmes URI (`http://richfaces.org/rich`, `http://richfaces.org/a4j`) mais
d'autres balises : `rich:modalPanel`, `rich:simpleTogglePanel`, `rich:tabPanel`, `a4j:support`,
`rich:spacer`, `rich:separator`… Ces applications sont souvent en JSF 1.2 / JSP, ce qui demande une
migration préalable vers Facelets. Le support 3.3 se ferait par des alias de balises dans la taglib
de compatibilité (feuille de route, phase 3).
