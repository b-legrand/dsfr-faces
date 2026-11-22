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

