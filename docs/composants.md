# Référence des composants

Espace de noms : `xmlns:dsfr="http://gouv.fr/dsfr-faces"`.

Les exemples de HTML généré supposent `jakarta.faces.SEPARATOR_CHAR` = `-` et un formulaire `id="form"`.

| Balise | Composant DSFR | Remplace |
|---|---|---|
| [`dsfr:head`](#dsfrhead) | CSS, icônes, favicons, JS | chargement des ressources et skins RichFaces |
| [`dsfr:inputText`](#dsfrinputtext) | Champ de saisie | `h:inputText` + `rich:message`, `rich:calendar` (`type="date"`) |
| [`dsfr:button`](#dsfrbutton) | Bouton | `h:commandButton`, `a4j:commandButton` (avec `f:ajax`) |
| [`dsfr:messages`](#dsfrmessages) | Alerte | `rich:messages`, `h:messages` |
| [`dsfr:message`](#dsfrmessage) | Messages de champ | `rich:message`, `h:message` |
| [`dsfr:field`](#dsfrfield) | Groupe de champ | structure `h:outputLabel` + champ + message existante |
| [`dsfr:inputTextarea`](#dsfrinputtextarea) | Zone de texte | `h:inputTextarea` |
| [`dsfr:inputSecret`](#dsfrinputsecret) | Mot de passe | `h:inputSecret` |
| [`dsfr:calendar`](#dsfrcalendar) | Champ date natif | `rich:calendar` |
| [`dsfr:selectOneMenu`](#dsfrselectonemenu) | Liste déroulante | `h:selectOneMenu`, `rich:select` |
| [`dsfr:selectOneRadio`](#dsfrselectoneradio-et-dsfrselectmanycheckbox) | Boutons radio (+ riches) | `h:selectOneRadio` |
| [`dsfr:selectManyCheckbox`](#dsfrselectoneradio-et-dsfrselectmanycheckbox) | Cases à cocher | `h:selectManyCheckbox` |
| [`dsfr:selectBooleanCheckbox`](#dsfrselectbooleancheckbox-et-dsfrtoggle) | Case à cocher | `h:selectBooleanCheckbox` |
| [`dsfr:toggle`](#dsfrselectbooleancheckbox-et-dsfrtoggle) | Interrupteur | — |
| [`dsfr:fieldset`](#dsfrfieldset) | Ensemble de champs | — |
| [`dsfr:commandLink`](#dsfrcommandlink) | Lien d'action | `h:commandLink`, `a4j:commandLink` |
| [`dsfr:dataTable`, `dsfr:column`](#dsfrdatatable-et-dsfrcolumn) | Tableau (tri) | `rich:dataTable`, `rich:column`, `h:dataTable` |
| [`dsfr:dataScroller`](#dsfrdatascroller) | Pagination | `rich:dataScroller` |
| [`dsfr:panel`](#dsfrpanel) | Carte sans lien | `rich:panel` |
| [`dsfr:accordion`, `dsfr:accordionItem`, `dsfr:collapsiblePanel`](#accordéons) | Accordéons | `rich:accordion`, `rich:accordionItem`, `rich:collapsiblePanel` |
| [`dsfr:tabPanel`, `dsfr:tab`](#dsfrtabpanel-et-dsfrtab) | Onglets | `rich:tabPanel`, `rich:tab` |
| [`dsfr:popupPanel`](#dsfrpopuppanel) | Modale | `rich:popupPanel` |

Les balises `rich:` et `a4j:` des pages migrées sont décrites dans
[migration-richfaces.md](migration-richfaces.md#module-de-compatibilité-dsfr-faces-richfaces-compat).

---

## `dsfr:head`

À placer dans `<h:head>`. Composant sans état, qui se rend lui-même.

| Attribut | Défaut | Description |
|---|---|---|
| `scheme` | `system` | `system`, `light`, `dark`. Vide : vous gérez `data-fr-scheme` sur `<html>` vous-même (utile avec une CSP stricte, car sinon un petit script en ligne est émis). |
| `icons` | `true` | Charge les classes utilitaires d'icônes `fr-icon-*` (`utility/icons/icons.min.css`). |
| `favicon` | `true` | Favicons et manifeste web du DSFR. |
| `themeColor` | `#000091` | Balise `<meta name="theme-color">`. |

Scripts : `dsfr.module.min.js` (`type="module"`, donc différé) et le repli `nomodule defer`
(`legacy.nomodule.min.js`, `dsfr.nomodule.min.js`). Ils peuvent donc rester dans le `<head>`.
Toutes les URL portent `?v={version DSFR}` pour le cache.

---

## `dsfr:inputText`

Champ de saisie complet : groupe, libellé, aide, champ et zone de messages. Étend `UIInput` : prend en
charge les convertisseurs, les validateurs, `required`, `f:ajax`, les `valueChangeListener` et les
attributs pass-through.

| Attribut | Défaut | Description |
|---|---|---|
| `label` | (obligatoire) | Libellé. Sert aussi de `{0}` dans les messages de validation Faces. |
| `hint` | | Texte d'aide (`fr-hint-text`) affiché dans le libellé. |
| `type` | `text` | `email`, `tel`, `url`, `number`, `date`, `search`, `password`… La valeur d'un `password` n'est jamais réaffichée. |
| `value`, `required`, `converter`, `validator`, `valueChangeListener`, `immediate` | | Comme `h:inputText`. |
| `requiredMessage`, `converterMessage`, `validatorMessage` | | Messages personnalisés. |
| `placeholder`, `autocomplete`, `inputmode`, `maxlength` | | Attributs HTML. `autocomplete` est exigé par le RGAA (11.13) sur les données personnelles. |
| `disabled`, `readonly` | `false` | Un champ désactivé ou en lecture seule n'est pas décodé. |
| `styleClass` | | Classes ajoutées au `<input>`. |

Événements `f:ajax` : `valueChange` (par défaut, déclenché sur `change`), `change`, `blur`, `focus`,
`input`, `keydown`, `keyup`, `click`.

La conversion est automatique selon le type de la propriété liée (`Integer`, `LocalDate`, énumérations…),
grâce aux convertisseurs par type de Faces.

HTML généré, en erreur :

```html
<div id="form-email" class="fr-input-group fr-input-group--error">
  <label class="fr-label" for="form-email-input">Adresse électronique *
    <span class="fr-hint-text">Format attendu : nom@domaine.fr</span></label>
  <input class="fr-input" id="form-email-input" name="form-email" type="email" value="x"
         aria-describedby="form-email-messages" aria-invalid="true" aria-required="true"
         autocomplete="email" onblur="mojarra.ab('form-email',event,'blur',0,'@this')">
  <div class="fr-messages-group" id="form-email-messages" aria-live="polite">
    <p class="fr-message fr-message--error" id="form-email-messages-0">Le format… est invalide.</p>
  </div>
</div>
```

États du groupe :
- `fr-input-group--error` si le composant est invalide ou porte un message d'erreur ;
- `fr-input-group--info` s'il ne porte que des messages d'information ;
- `fr-input-group--disabled` s'il est désactivé.

> Éviter `f:ajax event="keyup"` ou `input` avec `render="@this"` : remplacer le champ lui fait perdre le focus.

---

## `dsfr:button`

Bouton lié à une action. Étend `UICommand` : `action`, `actionListener`, `immediate`, `f:ajax`.

| Attribut | Défaut | Description |
|---|---|---|
| `value` | (obligatoire) | Libellé. |
| `action`, `actionListener`, `immediate` | | Comme `h:commandButton`. |
| `type` | `submit` | `submit`, `button`, `reset`. |
| `priority` | `primary` | `primary`, `secondary`, `tertiary`, `tertiary-no-outline`. |
| `size` | `md` | `sm`, `md`, `lg`. |
| `icon` | | Classe d'icône DSFR, p. ex. `fr-icon-send-plane-line`. |
| `iconPosition` | `left` | `left`, `right`, `only`. Avec `only`, le libellé sert de `title`. |
| `title`, `disabled`, `styleClass` | | |
| `onclick` | | JavaScript exécuté avant les comportements ajax ; s'il renvoie `false`, l'action est annulée (`return confirm('…')`). |

Événements `f:ajax` : `action` (par défaut, déclenché sur `click`), `click`, `focus`, `blur`.

```html
<button type="submit" id="form-send" name="form-send"
        class="fr-btn fr-icon-send-plane-line fr-btn--icon-left"
        onclick="mojarra.ab('form-send',event,'action','@form','@form');return false">Envoyer</button>
```

> Dans un `fr-btns-group`, c'est le **groupe** qui positionne les icônes : ajouter
> `fr-btns-group--icon-left` (ou `-right`) sur le groupe. Sinon, le DSFR réduit tout bouton avec icône
> à un bouton icône seule de 40 px.

---

## `dsfr:messages`

Messages Faces sous forme d'alertes DSFR. Étend `UIMessages`.

| Attribut | Défaut | Description |
|---|---|---|
| `globalOnly` | `false` | Uniquement les messages qui ne sont rattachés à aucun composant. |
| `for` | | Uniquement les messages du composant indiqué. |
| `showSummary` | `true` | Affiche le résumé. |
| `showDetail` | `false` | Affiche le détail. |
| `redisplay` | `true` | Réaffiche les messages déjà rendus ailleurs. |
| `titleTag` | `h3` | Balise du titre d'alerte (`h2` à `h6` ou `p`), pour respecter la hiérarchie des titres (RGAA 9.1). |
| `styleClass` | | Classes du conteneur. |
| `ajaxRendered` | `true` | Rafraîchi à chaque requête ajax, sans figurer dans `render` (comme `rich:messages`). |

- Le résumé et le détail sont différents : alerte avec titre (`fr-alert__title`) et paragraphe.
- Sinon : alerte compacte (`fr-alert--sm`).
- Sévérités : `INFO` donne `fr-alert--info`, `WARN` donne `fr-alert--warning`, `ERROR` et `FATAL`
  donnent `fr-alert--error`. Faces n'a pas de sévérité « succès ».
- `role="alert"` n'est ajouté que sur les réponses ajax : les lecteurs d'écran annoncent les nouveaux
  messages sans tout relire au chargement de la page.
- Le conteneur `<div id="…">` est toujours rendu, ce qui en fait une cible `render` valide même vide.

```html
<div id="form-msgs">
  <div class="fr-alert fr-alert--info" role="alert">
    <h2 class="fr-alert__title">Formulaire envoyé</h2>
    <p>Merci Dupont, nous vous répondrons…</p>
  </div>
</div>
```

---

## `dsfr:message`

Messages d'un seul composant, sous forme de messages DSFR en ligne. Étend `UIMessage`. Sert à côté d'un
champ qui n'est pas un composant DSFR (p. ex. `h:inputText`) : les champs `dsfr:` affichent déjà leurs
propres messages.

| Attribut | Défaut | Description |
|---|---|---|
| `for` | (obligatoire) | Identifiant du composant. Recherche à la RichFaces : relative, puis conteneurs de nommage englobants, puis toute la vue. |
| `redisplay` | `true` | Réaffiche les messages déjà rendus ailleurs. |
| `ajaxRendered` | `true` | Rafraîchi à chaque requête ajax, sans figurer dans `render` (comme `rich:message`). |
| `styleClass` | | Classes ajoutées à `fr-messages-group`. |

```html
<div class="fr-messages-group" id="signup-loginMessage" aria-live="polite">
  <p class="fr-message fr-message--error" id="signup-loginMessage-0">Identifiant : erreur de validation…</p>
</div>
```

Le conteneur est toujours rendu (zone `aria-live` présente avant l'arrivée des messages, cible `render`
valide).

---

## `ajaxRendered`

Tout composant dont l'attribut `ajaxRendered` vaut `true` est rafraîchi à chaque requête ajax, en plus des
zones indiquées par `render`. C'est le comportement par défaut de `dsfr:messages` et `dsfr:message`.
Il ne s'applique pas si la requête rafraîchit `@all`, si le composant est déjà dans une zone rafraîchie,
ou si la source de la requête porte `limitRender="true"` (`a4j:`).

---

## `dsfr:field`

Groupe DSFR (`fr-input-group`, ou `fr-select-group` pour une liste) **autour de composants standard
existants**, sans changer leurs identifiants. C'est la cible de la [migration facultative](migration-richfaces.md#migration-facultative--groupes-dsfr-avec-dsfrfield).
Pour une nouvelle page, préférer `dsfr:inputText`.

```xml
<dsfr:field id="loginField" hint="3 à 20 caractères">
  <h:outputLabel for="login" value="Identifiant"/>
  <h:inputText id="login" value="#{bean.login}" required="true"/>
  <h:message id="loginMessage" for="login"/>
</dsfr:field>
```

| Attribut | Défaut | Description |
|---|---|---|
| `hint` | | Aide affichée dans le libellé (`fr-hint-text`). Nécessite `dsfr-faces-html`. |
| `styleClass` | | Classes ajoutées au groupe. |

- **Champ du groupe :** le premier enfant `UIInput`. **Message :** l'enfant `h:message`, `rich:message` ou
  `dsfr:message` dont le `for` désigne ce champ.
- **État :** `fr-input-group--error` si le champ est invalide ou a un message d'erreur, `--disabled` s'il
  est désactivé.
- **Avec `dsfr-faces-html`** (tiré par `dsfr-faces-richfaces-compat`) :
  - le libellé reçoit ` *` si le champ est obligatoire, puis l'aide ;
  - le champ reçoit `aria-required="true"` et `aria-describedby` vers le message.
- **Ajax :** rafraîchi automatiquement par toute requête ajax qui exécute son champ (sa valeur soumise est
  réaffichée, rien n'est perdu), sauf pour les événements de frappe envoyés par le champ lui-même.

```html
<div id="request-emailField" class="fr-input-group fr-input-group--error">
  <label for="request-email" class="fr-label">Adresse électronique *</label>
  <input id="request-email" type="text" name="request-email" value="x" class="fr-input"
         aria-invalid="true" aria-required="true" aria-describedby="request-emailMessage" onblur="…">
  <div class="fr-messages-group" id="request-emailMessage" aria-live="polite">
    <p class="fr-message fr-message--error" id="request-emailMessage-0">Le format…</p>
  </div>
</div>
```

---

## Champs : principe commun

`dsfr:inputTextarea`, `dsfr:inputSecret`, `dsfr:calendar` et `dsfr:selectOneMenu` suivent la même règle :

- **avec `label`** : groupe DSFR complet (`fr-input-group` ou `fr-select-group`). Comme pour
  `dsfr:inputText`, l'enveloppe porte l'identifiant client et le champ est `{id}-input`. L'enveloppe
  contient le libellé (avec `*` et `hint`), les messages, `aria-describedby`, `aria-invalid` et
  `aria-required`.
- **sans `label`** : le champ seul porte l'identifiant client. Un `h:outputLabel for`, un `h:message` ou un
  `dsfr:field` existants restent valides. C'est le cas des pages RichFaces migrées (`rich:calendar`,
  `rich:select`).

Attributs communs : `value`, `required` et les messages, `converter`, `validator`, `immediate`, `label`,
`hint`, `disabled`, `readonly`, `title`, `styleClass`. Ils acceptent `f:ajax` / `a4j:ajax` (`valueChange`
par défaut, émis sur `change`).

## `dsfr:inputTextarea`

`<textarea class="fr-input">`. Attributs : `rows`, `placeholder`, `maxlength`.

## `dsfr:inputSecret`

Composant « Mot de passe » du DSFR (`fr-password`), avec la case « Afficher » gérée par le script du DSFR.

- `autocomplete` : `current-password` ou `new-password`.
- La valeur n'est pas réaffichée, sauf avec `redisplay="true"`.

## `dsfr:calendar`

Champ date natif (`input type="date"`) : le navigateur affiche la date dans sa langue et envoie toujours
`yyyy-MM-dd`.

| Type de la valeur | Conversion |
|---|---|
| `LocalDate` | directe |
| `LocalDateTime` | début de journée |
| `java.util.Date` | début de journée dans `timeZone` (par défaut, le fuseau du serveur) |
| `String` | formatée avec `datePattern`, ISO sinon |

- `min` et `max` sont au format `yyyy-MM-dd`.
- Un convertisseur explicite est prioritaire, mais il reçoit alors la valeur ISO.
- Attributs RichFaces sans objet (`popup`, `enableManualInput`, `locale`) : ignorés.

## `dsfr:selectOneMenu`

`<select class="fr-select">`, options issues de `f:selectItem` et `f:selectItems` (objets `SelectItem`,
tableaux, collections, `Map` libellé → valeur, objets avec `var` / `itemValue` / `itemLabel`), groupes en
`optgroup`.

- `placeholder` (ou `defaultLabel` de `rich:select`) ajoute une première option vide, désactivée si le
  champ est obligatoire, comme dans les exemples du DSFR.

## `dsfr:selectOneRadio` et `dsfr:selectManyCheckbox`

Ensemble de champs DSFR (`fr-fieldset`) :

- `label` devient la légende, et chaque élément devient un `fr-radio-group` ou un `fr-checkbox-group` ;
- la description d'un élément (`itemDescription`) est affichée comme aide ;
- l'ensemble porte l'identifiant client ; en erreur, il passe en `fr-fieldset--error` avec les messages
  en dessous.

| Attribut | Description |
|---|---|
| `inline` | Éléments sur une ligne (`fr-fieldset__element--inline`). |
| `rich` (radio) | Boutons radio riches : la description de l'élément est l'URL d'un pictogramme DSFR (svg). |

Valeur de `dsfr:selectManyCheckbox` : tableau, `List`, `Set` ou `SortedSet`. Les éléments sont convertis
selon le type du tableau ou des valeurs des éléments. Un bouton radio obligatoire sans choix est bien en
erreur.

## `dsfr:selectBooleanCheckbox` et `dsfr:toggle`

Valeur booléenne.

- `dsfr:selectBooleanCheckbox` : `fr-checkbox-group`, avec `size="sm"` pour la petite case.
- `dsfr:toggle` : interrupteur (`fr-toggle`).
  - `checkedLabel` / `uncheckedLabel` : par défaut « Activé » / « Désactivé ».
  - `labelLeft` : libellé à gauche.

## `dsfr:fieldset`

`fr-fieldset` avec `legend` (ou facette `legend`) et `hint`. Chaque enfant est placé dans un
`fr-fieldset__element` (`inline` pour les aligner).

L'ensemble passe en erreur :
- si un champ qu'il contient est invalide ;
- ou si un message est rattaché à son identifiant (message d'erreur de groupe, ajouté par le bean).

## `dsfr:commandLink`

Lien d'action `a.fr-link` (`icon`, `iconPosition`, `size` `sm`/`lg`, `title`, `disabled`, `onclick`). Le
contenu est `value`, ou à défaut les enfants.

- **Avec `f:ajax`** (ou en `a4j:commandLink`) : requête ajax.
- **Sans** : le clic envoie le formulaire, avec les `f:param`.
- Les `f:param` enfants sont aussi envoyés en ajax (comme pour `dsfr:button`). Un paramètre avec l'attribut
  `noEscape="true"` a pour valeur une expression JavaScript, évaluée au clic (`DsfrFaces.params`).
- `onclick` est exécuté d'abord ; s'il renvoie `false`, l'action est annulée.

Le DSFR recommande un bouton pour les actions : le lien sert surtout à la compatibilité.

## `dsfr:dataTable` et `dsfr:column`

Tableau DSFR (`fr-table` et ses conteneurs) :

| Attribut | Description |
|---|---|
| `value`, `var`, `rows`, `first` | Comme `h:dataTable`. |
| `caption` (ou facette `caption` / `header`) | Titre du tableau (`caption`). La facette `header` de RichFaces devient le titre. |
| `size` | `sm`, `md`, `lg`. |
| `bordered`, `noScroll` | Variantes du DSFR. |
| `rowClasses`, `columnClasses` | Listes séparées par des virgules, appliquées en boucle. |
| `noDataLabel` (ou facette `noData`) | Ligne affichée quand il n'y a pas de données. |

Colonnes : facettes `header` et `footer`, `styleClass`, `headerClass`.

**Tri**, comme le tri intégré de RichFaces :

- `sortBy="#{c.nom}"` rend la colonne triable : un bouton « Trier » apparaît dans l'en-tête, avec
  `aria-sort`.
- Chaque clic alterne l'ordre croissant et décroissant. Il désactive le tri des autres colonnes et revient à
  la première page.
- Le tri est fait en ajax : le tableau et ses `dsfr:dataScroller` sont rafraîchis.
- Les chaînes sont comparées selon la langue de la vue (« Écully » se range avec les E).
- `sortOrder` (`ascending`, `descending`, `unsorted`) peut être lié au modèle (chaîne ou enum).
- Le tri porte sur une copie des lignes, dans un ordre stable d'une requête à l'autre : les actions dans
  les lignes visent bien la ligne affichée.

Le tableau doit être dans un formulaire. Avec `dsfr-faces-html`, `h:dataTable` est rendu de la même façon,
sans tri.

## `dsfr:dataScroller`

Pagination DSFR (`fr-pagination`) du tableau désigné par `for`. Le tableau peut se trouver ailleurs dans la
vue : il est recherché comme dans RichFaces.

- Les liens « Première page », « Page précédente », numéros, « Page suivante » et « Dernière page » envoient
  une requête ajax qui rafraîchit le tableau et la pagination.
- `maxPages` : nombre de numéros affichés, 10 par défaut.
- `boundaryControls` et `stepControls` : `hide` masque les liens de bout et de pas.
- `page` : expression du numéro de page courant, mise à jour.

## `dsfr:panel`

Carte DSFR sans lien (`fr-card fr-card--no-arrow`) :

- titre `header` ou facette `header`, dans un `h3` (`headerTag` pour un autre niveau) ;
- contenu dans `fr-card__desc` (`bodyClass`).

## Accordéons

- **`dsfr:accordion`** : groupe d'accordéons (`fr-accordions-group`), un seul ouvert à la fois.
  - `activeItem` : nom de l'élément ouvert.
  - L'élément ouvert est gardé d'une requête à l'autre par un champ caché, et écrit dans l'expression de
    `activeItem` si elle est modifiable.
- **`dsfr:accordionItem`** :
  - `header` ou facette `header` ;
  - `name` : par défaut l'id ;
  - `headingTag` : par défaut `h3` ;
  - `disabled`.
- **`dsfr:collapsiblePanel`** : un accordéon seul ; son état (`expanded`) est gardé de la même façon.

Le changement d'élément se fait côté client, par le script du DSFR. `switchType` est accepté et ignoré.

## `dsfr:tabPanel` et `dsfr:tab`

Onglets DSFR (`fr-tabs`). Chaque `dsfr:tab` (`header` ou facette `header`, `name`, `icon`, `disabled`)
rend son panneau, qui porte son identifiant client.

| `switchType` | Comportement |
|---|---|
| `ajax` (défaut, comme RichFaces) | Seul l'onglet actif est rendu et traité. Un clic recharge les onglets en ajax, sans valider l'onglet quitté. |
| `server` | Idem, par un envoi complet du formulaire. |
| `client` | Tous les onglets sont rendus et traités ; le script du DSFR change d'onglet. |

`activeItem` (nom de l'onglet actif) est gardé d'une requête à l'autre et écrit dans son expression.
`label` est l'`aria-label` de la liste d'onglets (« Onglets » par défaut).

## `dsfr:popupPanel`

Modale DSFR (`dialog.fr-modal`) avec bouton « Fermer » :

| Attribut | Description |
|---|---|
| `header` (ou facette `header`) | Titre (`h1`, ou `titleTag`). |
| `size` | `sm`, `md` (défaut), `lg`. |
| `show` | Ouvre la modale à l'affichage, y compris après une mise à jour ajax (p. ex. `show="#{bean.selection != null}"`). |

Ouverture et fermeture en JavaScript :

```js
DsfrFaces.component('form-popup').show();   // et .hide()
```

Avec la couche de compatibilité, `#{rich:component('popup')}.show()` fonctionne aussi. Une modale ouverte
et rafraîchie par une requête ajax est rouverte automatiquement.

Les attributs RichFaces sans objet (`modal`, `autosized`, `width`, `height`, `domElementAttachment`) sont
ignorés.

## API JavaScript

`dsfr:head` charge `dsfr-faces/dsfr-faces.js` :

| Appel | Composant |
|---|---|
| `DsfrFaces.component(id).show()` / `.hide()` | `dsfr:popupPanel` |
| `DsfrFaces.component(id).switchToItem(nom)` | `dsfr:tabPanel`, `dsfr:accordion` |
| `DsfrFaces.component(id).switchPanel()` | `dsfr:collapsiblePanel` |

La couche de compatibilité ajoute `RichFaces.component(id)`, qui délègue à `DsfrFaces.component(id)`.

