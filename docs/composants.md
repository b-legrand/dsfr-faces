# Référence des composants

Espace de noms : `xmlns:dsfr="http://gouv.fr/dsfr-faces"`.

Les exemples de HTML généré supposent `jakarta.faces.SEPARATOR_CHAR` = `-` et un formulaire `id="form"`.

| Balise | Composant DSFR | Remplace |
|---|---|---|
| [`dsfr:head`](#dsfrhead) | CSS, icônes, favicons, JS | chargement des ressources et skins RichFaces |
| [`dsfr:inputText`](#dsfrinputtext) | Champ de saisie | `h:inputText` + `rich:message`, `rich:calendar` (`type="date"`) |
| [`dsfr:button`](#dsfrbutton) | Bouton | `h:commandButton`, `a4j:commandButton` (avec `f:ajax`) |
| [`dsfr:messages`](#dsfrmessages) | Alerte | `rich:messages`, `h:messages` |

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
