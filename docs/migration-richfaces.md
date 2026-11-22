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

Le script [`scripts/migrate-richfaces.sh`](../scripts/migrate-richfaces.sh) applique exactement cette
commande à un dossier.

Pour que cette commande suffise, dsfr-faces fournit une **couche de compatibilité RichFaces**, le module
`dsfr-faces-richfaces-compat` : mêmes noms de balises, mêmes attributs, mêmes fonctions EL, mais un rendu
DSFR. Elle est encore partielle (voir [l'état du module](#module-de-compatibilité-dsfr-faces-richfaces-compat)
et la [feuille de route](roadmap.md), phase 1).

Démonstration : le showcase contient une page RichFaces d'origine
(`dsfr-faces-showcase/src/main/richfaces/inscription.xhtml`). Le build la migre avec le script et la
publie sous `/richfaces/inscription.xhtml`, et les tests Playwright vérifient qu'elle fonctionne.

## Deux chemins de migration

| | Migration **obligatoire** | Migration **facultative** |
|---|---|---|
| Outil | `sed` ([`scripts/migrate-richfaces.sh`](../scripts/migrate-richfaces.sh)) | `dsfr-faces-migrate` ([`scripts/migrate-fields.sh`](../scripts/migrate-fields.sh)), après le `sed` |
| Effet | renomme `rich:` en `dsfr:` ; les pages fonctionnent avec le rendu DSFR | enveloppe chaque libellé + champ + message dans `dsfr:field` : vrai groupe DSFR (état d'erreur, mention obligatoire, liens ARIA) |
| Identifiants, JavaScript | inchangés | inchangés |
| Revue | aucune | relire le diff ; traiter à la main les champs signalés |
| Démonstration (showcase) | `/richfaces/inscription.xhtml` | `/richfaces-fields/contact.xhtml` |
| Tests e2e | `richfaces-migration.spec.ts` | `richfaces-fields.spec.ts` (pages distinctes) |

Les deux chemins sont testés séparément : chacun a ses propres pages RichFaces d'origine
(`src/main/richfaces` et `src/main/richfaces-fields`), migrées à chaque build du showcase.

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
   Mojarra 4.1.17 (vérifié sur GlassFish 8.0.5). MyFaces 4.1 reste à vérifier. Un
   `h:inputText` ou `h:commandButton` reçoit les classes DSFR grâce au module `dsfr-faces-html` : voir
   [les composants `h:` standard](#les-composants-h-standard).

## Prérequis hors `sed`

RichFaces 4.5 cible JSF 2.x (`javax.faces`), alors que dsfr-faces cible Jakarta Faces 4.1
(`jakarta.faces`). Avant le `sed`, il faut donc :

| Étape | Outil | Remarque |
|---|---|---|
| Passer de `javax.*` à `jakarta.*` (Java, `web.xml`, `faces-config.xml`, `beans.xml`) | OpenRewrite (`org.openrewrite.java.migrate.jakarta.JakartaEE10`) ou Eclipse Transformer | Les `.xhtml` ne sont presque pas concernés |
| **Retirer les jars RichFaces** et ajouter `dsfr-faces-richfaces-compat` (qui tire `dsfr-faces-core`) | `pom.xml` | voir [pourquoi](#pourquoi-retirer-richfaces) |
| Remplacer les managed beans JSF (`@ManagedBean`, supprimés dans Faces 4.0) | CDI `@Named` + scopes CDI | OpenRewrite propose une recette |
| Retirer les paramètres `org.richfaces.*` du `web.xml` | à la main | skin, `enableControlSkinning`, push… |
| Adapter le code Java qui utilise l'API RichFaces | à la main | `org.richfaces.model.*` (`SortOrder`, `ExtendedDataModel`, `Filter`), `UIDataTable`, `ExtendedDataTableState`… |
| Remplacer les usages de skin dans les pages ou CSS (`#{richSkin.*}`, `#{a4jSkin.*}`) | à la main | variables CSS du DSFR |
| Remplacer le CSS spécifique (`.rf-*`, `.rich-*`) | à la main | ne s'applique plus |

### Pourquoi retirer RichFaces

On ne peut pas garder RichFaces pour `a4j:` (ni pour le reste) sous Jakarta Faces 4.1 :

- **Plus maintenu, et en `javax`.** Les dernières versions sont `org.richfaces` 4.5.17.Final (juin 2016)
  et la reprise communautaire `com.github.albfernandez.richfaces` 4.6.21.ayg (janvier 2023). Les deux
  sont compilées pour `javax.faces` (JSF 2.x, Java 8).
- **JavaScript incompatible.** Même convertie en `jakarta` (Eclipse Transformer), la partie JS appelle
  `jsf.ajax`, supprimé par Faces 4.0 (remplacé par `faces.js` / `faces.ajax`).
- **Conflit d'URI.** Le jar RichFaces déclare lui-même `http://richfaces.org/a4j` et
  `http://richfaces.org/rich`, les URI que reprend la couche de compatibilité.
- **Mélange de styles.** Il ramènerait jQuery 1.x et les skins RichFaces (`rf-*`) à côté du DSFR.

`a4j:` n'est pas réécrit pour autant : la couche de compatibilité délègue à l'ajax standard de Faces
(`AjaxBehavior`, celui de `f:ajax`), avec les valeurs par défaut de RichFaces.

## Module de compatibilité `dsfr-faces-richfaces-compat`

```xml
<dependency>
  <groupId>fr.gouv.dsfr</groupId>
  <artifactId>dsfr-faces-richfaces-compat</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Ce qui est pris en charge

| Élément | Comportement |
|---|---|
| `rich:messages` | `dsfr:messages` (alertes DSFR), `ajaxRendered` par défaut |
| `rich:message` | `dsfr:message` (`fr-messages-group`), `ajaxRendered` par défaut |
| `a4j:ajax` | `AjaxBehavior` standard sur le composant parent. Prend en charge `event` (par défaut, l'événement par défaut du parent), `execute` (par défaut `@this`), `render`, `listener` (avec ou sans `AjaxBehaviorEvent`), `disabled`, `immediate`, `resetValues`, `limitRender`, `onbegin`, `onbeforedomupdate`, `oncomplete`, `onerror` et `delay` |
| `a4j:commandButton` | `dsfr:button` avec ajax sur `action`. `execute` vaut `@form` par défaut ; `render`, `oncomplete`, `limitRender`… comme pour `a4j:ajax`. `onclick` est exécuté avant la requête (`return confirm(…)` l'annule). |
| Fonctions EL | `clientId`, `component`, `element`, `findComponent`, `isUserInRole` et `jQuerySelector` |
| En-tête | Les ressources DSFR (`dsfr:head`) sont ajoutées automatiquement au `h:head` des vues qui n'ont pas de `dsfr:head`, comme RichFaces injectait les siennes |

Détails :

- **Recherche des identifiants à la RichFaces.** Pour `render`, `execute`, `for` et les fonctions EL, la
  recherche est relative au composant, puis s'étend aux conteneurs de nommage englobants, puis à toute
  la vue. Ainsi `render="resume"` trouve un composant situé hors du formulaire, ce que `f:ajax` refuse.
- **`render` et `execute`** acceptent des identifiants séparés par des espaces ou des virgules, ou une
  expression EL.
- **`oncomplete`** s'exécute après la mise à jour du DOM (statut `success` de `faces.ajax`), y compris
  quand la validation échoue, comme dans RichFaces. Les expressions EL dans ces scripts sont évaluées.
- **`ajaxRendered`** : à chaque requête ajax, les composants `ajaxRendered` sont ajoutés aux zones
  rafraîchies, sauf si la requête rafraîchit `@all`, si le composant est déjà dans une zone rafraîchie,
  ou si la source porte `limitRender="true"`. Tout composant peut s'y inscrire avec
  `ajaxRendered="true"`.
- **Désactiver l'injection de l'en-tête** :

  ```xml
  <context-param>
    <param-name>fr.gouv.dsfr.richfaces.AUTO_HEAD</param-name>
    <param-value>false</param-value>
  </context-param>
  ```

  Ajouter alors `<dsfr:head/>` aux gabarits.

### Pas encore pris en charge

- La forme englobante de `a4j:ajax` (`<a4j:ajax>` autour de composants) : seule la forme imbriquée dans
  un composant fonctionne.
- `status`, `bypassUpdates`, `data`, `focus` et `requestGroupingId` sont acceptés mais ignorés.
- `#{rich:component('x')}` produit `RichFaces.component('…')`, mais l'objet JS `RichFaces` n'existe pas
  encore : il viendra avec `rich:popupPanel`.
- Toutes les autres balises `rich:` et `a4j:` (voir les tableaux ci-dessous).

## Migration facultative : groupes DSFR avec `dsfr:field`

Après le `sed`, les champs `h:` ont les classes DSFR (`dsfr-faces-html`) mais pas le **groupe** DSFR : pas
d'état d'erreur visible, pas de mention obligatoire, message non relié au champ. L'outil
`dsfr-faces-migrate` ajoute ce groupe sans toucher aux identifiants :

```sh
mvn -pl dsfr-faces-migrate package             # une fois
scripts/migrate-fields.sh --dry-run src/main/webapp   # rapport seul
scripts/migrate-fields.sh src/main/webapp             # modifie les fichiers
```

Avant (après le `sed`) :

```xml
<h:outputLabel for="email" value="Adresse électronique"/>
<h:inputText id="email" value="#{bean.email}" required="true">
    <a4j:ajax event="blur"/>
</h:inputText>
<dsfr:message id="emailMessage" for="email"/>
```

Après :

```xml
<dsfr:field id="emailField">
    <h:outputLabel for="email" value="Adresse électronique"/>
    <h:inputText id="email" value="#{bean.email}" required="true">
        <a4j:ajax event="blur"/>
    </h:inputText>
    <dsfr:message id="emailMessage" for="email"/>
</dsfr:field>
```

Règles de l'outil :

- **Motif reconnu :** un `h:outputLabel for="x"`, suivi du champ `id="x"` (`h:inputText`, `h:inputSecret`,
  `h:inputTextarea`, `h:selectOne*`, `h:selectMany*` en menu ou liste), puis éventuellement de son message
  (`h:message`, `rich:message` ou `dsfr:message` avec `for="x"`). Seuls des espaces et des commentaires
  peuvent les séparer.
- **Non enveloppés, et signalés avec leur numéro de ligne :** les champs dans un `h:panelGrid` (chaque
  enfant y est une cellule), ceux sans libellé juste avant, ceux sans `id`.
- **Le reste du fichier est conservé caractère pour caractère.** L'outil insère l'enveloppe et ré-indente
  les lignes enveloppées, jamais à l'intérieur d'une valeur d'attribut, d'un commentaire ou d'un CDATA.
- **Idempotent :** le relancer ne change rien.
- **Préfixe :** `dsfr:field` utilise le préfixe lié à `http://gouv.fr/dsfr-faces`, ou à défaut celui lié à
  `http://richfaces.org/rich` (c'est `dsfr` après le `sed` ; la taglib de compatibilité déclare aussi
  `field`). Sinon, l'outil déclare `xmlns:dsfr="http://gouv.fr/dsfr-faces"`.
- L'identifiant du groupe vaut `{id du champ}Field`, rendu unique dans la page.
- Code de sortie : 0, 1 si un fichier n'est pas du XML bien formé (il est alors laissé tel quel).

Ce que `dsfr:field` apporte, avec `dsfr-faces-html` (voir [composants.md](composants.md#dsfrfield)) :

- l'état d'erreur du groupe (`fr-input-group--error` : libellé rouge, barre latérale) ;
- la mention obligatoire `*` dans le libellé et `aria-required` sur le champ ;
- `aria-describedby` du champ vers son message ;
- l'aide (`hint="…"`, à ajouter à la main).

En ajax, le groupe est rafraîchi automatiquement quand son champ fait partie du `execute` (blur, envoi du
formulaire…), même sans `render`. La seule exception est la frappe (`keyup`, `input`…) dans le champ
lui-même, pour ne pas remplacer le champ en cours de saisie.

Étape suivante possible, au cas par cas : remplacer le groupe par `dsfr:inputText` (meilleur rendu, mais
l'identifiant client du champ devient `{id}-input`).

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

`scripts/migrate-richfaces.sh` n'applique pour l'instant que le `sed`. Une version complète (feuille de
route, phase 1) produira aussi ce rapport et signalera les balises ou attributs sans équivalent.

## Correspondances `rich:` → DSFR

Légende de priorité :
- **P1** : indispensable pour migrer une application de gestion typique ;
- **P2** : courant ;
- **P3** : rare ou sans équivalent DSFR ;
- **HP** : hors périmètre.

| Balise RichFaces 4.5 | Composant DSFR | Prio. | Remarques |
|---|---|---|---|
| `rich:messages` | Alerte (`fr-alert`) | P1 | ✅ `dsfr:messages`, `ajaxRendered` par défaut |
| `rich:message` | Message de champ (`fr-message`) | P1 | ✅ `dsfr:message`, `ajaxRendered` par défaut |
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
| `a4j:ajax` | ✅ `AjaxBehavior` standard (celui de `f:ajax`) ; `status` ignoré, pas de forme englobante | P1 |
| `a4j:commandButton` | ✅ bouton DSFR + ajax implicite (`execute="@form"`) | P1 |
| `a4j:commandLink` | lien DSFR (`fr-link`) + ajax implicite | P1 |
| `a4j:outputPanel` | `div`/`span` ; `ajaxRendered="true"` (le mécanisme existe déjà dans le cœur) | P1 |
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
sont majoritaires dans les pages RichFaces. Sans traitement, c'est pire qu'un rendu brut : la remise à zéro
CSS du DSFR retire la bordure des champs natifs, qui deviennent **invisibles**.

Le module **`dsfr-faces-html`** (tiré par `dsfr-faces-richfaces-compat`) règle ce problème sans toucher
aux pages. Il enveloppe le kit de rendu `HTML_BASIC` de l'implémentation par la `RenderKitFactory`
standard : les renderers de Mojarra ou MyFaces produisent toujours le HTML (ajax, attributs
pass-through…), et le module ajoute la classe DSFR à leur élément principal.

| Balises | Classe ajoutée |
|---|---|
| `h:inputText`, `h:inputSecret`, `h:inputTextarea` | `fr-input` |
| `h:selectOneMenu`, `h:selectOneListbox`, `h:selectManyMenu`, `h:selectManyListbox` | `fr-select` |
| `h:outputLabel` | `fr-label` |
| `h:commandButton`, `h:button` | `fr-btn` |
| `h:commandLink`, `h:outputLink`, `h:link` | `fr-link` |
| `h:messages` | rendu en alertes DSFR (comme `dsfr:messages`) |
| `h:message` | rendu en messages DSFR (comme `dsfr:message`) |

- La classe est **fusionnée** avec `styleClass` (sans doublon), elle ne la remplace pas.
- Les champs invalides reçoivent `aria-invalid="true"`.
- Désactiver le module : paramètre de contexte `fr.gouv.dsfr.html.ENABLED=false`.

Limites :

- `h:outputLabel` et `h:message` étant des composants séparés du champ, ce mode ne peut pas produire le
  groupe complet (`fr-input-group`, aide, messages reliés en ARIA). En particulier, **le DSFR n'affiche
  l'état d'erreur d'un champ que par son groupe** (`fr-input-group--error`) : un `h:inputText` invalide
  n'a pas de bordure rouge, l'erreur est signalée par le message voisin et par `aria-invalid`.
- Cases à cocher et boutons radio (`h:selectBooleanCheckbox`, `h:selectOneRadio`…) restent natifs : le
  DSFR les dessine à partir d'une structure (`fr-checkbox-group` avec le libellé juste après) que ces
  balises ne produisent pas.
- `h:dataTable` n'est pas encore traité (le tableau DSFR demande des conteneurs autour de `<table>`).

Les pages sont présentables ; le passage aux balises `dsfr:` reste recommandé pour la conformité RGAA.

## Comportements RichFaces implicites à reproduire

- ✅ `rich:messages` et `rich:message` sont rafraîchis à **chaque** requête ajax (`ajaxRendered`), sans
  `render` explicite. Sans ce comportement, une application migrée perdrait ses messages d'erreur ajax.
- ✅ `a4j:commandButton` est ajax par défaut avec `execute="@form"` (`a4j:commandLink` reste à faire).
- ✅ `render` accepte des identifiants séparés par des virgules et des expressions EL, et trouve les
  composants dans toute la vue.
- ✅ Les ressources RichFaces étaient injectées automatiquement : les ressources DSFR le sont aussi.
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
