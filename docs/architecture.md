# Architecture

## Modules

```
dsfr-faces/
├── pom.xml                  parent : versions (Java 17, Jakarta EE 11 provided, DSFR, Node)
├── dsfr-faces-resources/    dist/ du DSFR -> META-INF/resources/dsfr
│   ├── package.json         @gouvfr/dsfr, installé par le build Maven (npm ci)
│   ├── package-lock.json
│   └── .dsfr.yml            acceptation de licence pour un npm ci manuel
├── dsfr-faces-core/         composants, renderers, taglib, API JS (META-INF/resources/dsfr-faces/dsfr-faces.js)
│   └── fr.gouv.dsfr.faces
│       ├── Dsfr                       constantes, URL des ressources, version
│       ├── component/                 UIComponent (état via StateHelper)
│       ├── render/                    Renderers
│       │   ├── DsfrRendererBase       comportements client (f:ajax), sévérités, groupe de messages, ajax direct
│       │   ├── DsfrInputRendererBase  décodage, conversion, libellé
│       │   ├── DsfrFieldRendererBase  champs à contrôle unique : groupe complet ou contrôle seul
│       │   └── DsfrChoicesRendererBase  boutons radio et cases dans un fieldset
│       ├── util/SelectItems           éléments de f:selectItem / f:selectItems
│       ├── ajax/                      PartialViewContext : ajaxRendered, limitRender
│       └── util/Components            recherche d'identifiants à la RichFaces
├── dsfr-faces-html/         classes DSFR sur les balises h: (enveloppe du kit HTML_BASIC)
│   └── fr.gouv.dsfr.faces.html
│       ├── DsfrRenderKitFactory   RenderKitFactory standard qui enveloppe HTML_BASIC
│       ├── DsfrRenderKit          renderers enveloppés, h:messages / h:message -> renderers dsfr
│       ├── DsfrClassRenderer      délègue au renderer de l'implémentation
│       └── DsfrClassWriter        ajoute la classe à l'élément principal
├── dsfr-faces-richfaces-compat/   taglibs aux URI RichFaces (rich, a4j), sans jar RichFaces
│   ├── META-INF/rich.taglib.xml   http://richfaces.org/rich : balises + fonctions EL
│   ├── META-INF/a4j.taglib.xml    http://richfaces.org/a4j
│   └── fr.gouv.dsfr.faces.richfaces
│       ├── A4jAjaxHandler, A4jCommandHandler, AjaxSupport   a4j: -> AjaxBehavior standard
│       ├── RichAjaxBehavior       AjaxBehavior + recherche d'identifiants dans toute la vue
│       ├── RichFunctions          fonctions EL rich:*
│       └── AutoHeadListener       ajoute dsfr:head aux vues qui n'en ont pas
├── dsfr-faces-migrate/      outil de migration facultative (Java, sans dépendance) : dsfr:field
│   └── fr.gouv.dsfr.faces.migrate
│       ├── XmlLite            analyseur XML qui garde les positions (édition en place)
│       ├── FieldMigration     motif libellé + champ + message -> dsfr:field
│       └── Main               ligne de commande
├── dsfr-faces-showcase/     WAR de démonstration
│   ├── src/main/richfaces/         page RichFaces d'origine, migrée par le sed -> /richfaces/
│   └── src/main/richfaces-fields/  page RichFaces d'origine, sed + dsfr-faces-migrate -> /richfaces-fields/
├── scripts/migrate-richfaces.sh   migration obligatoire (sed)
├── scripts/migrate-fields.sh      migration facultative (dsfr-faces-migrate)
├── compose.yml              tests d'intégration : GlassFish + Playwright
└── e2e/                     tests Playwright
```

## Choix techniques

### Ressources DSFR servies statiquement, pas par le resource handler Faces

Les CSS du DSFR référencent polices et icônes par des `url(...)` relatives. Le resource handler Faces
(`/jakarta.faces.resource/x.xhtml?ln=…`) casse ces chemins. Les fichiers sont donc placés dans
`META-INF/resources/dsfr/` d'un jar : tout conteneur Servlet 3.0+ les sert tels quels à
`/{contexte}/dsfr/…`, et les chemins relatifs restent valides.

### Renderers Java plutôt que composants composites

- **Composants avec une valeur** (champs, listes, cases) : `UIComponent` et `Renderer` Java. Les
  composites gèrent mal `EditableValueHolder`, les validateurs et les identifiants, et sont lents.
- **Composants de mise en page sans état** (carte, encadré, en-tête, pied de page…) : prévus en
  fichiers de balises Facelets.

### Ajax standard

Pas de couche ajax maison : les composants implémentent `ClientBehaviorHolder` et acceptent `<f:ajax>`.
La compatibilité `a4j:` (voir [migration](migration-richfaces.md)) est construite au-dessus du même
`AjaxBehavior` que `f:ajax` : le script client est produit par l'implémentation Faces.

### Classes DSFR sur les balises `h:` sans réécrire leurs renderers

`dsfr-faces-html` ne réimplémente pas `h:inputText` & co. : réécrire les renderers standard ferait perdre
les comportements propres à chaque implémentation (attributs, ajax, pass-through). `DsfrRenderKitFactory`
enveloppe le kit `HTML_BASIC`, et chaque renderer concerné est enveloppé à son tour. Pendant
`encodeBegin` / `encodeEnd`, un `ResponseWriterWrapper` repère le premier élément attendu (`input`,
`select`, `label`…) ouvert avec le composant, fusionne la classe DSFR dans l'attribut `class` écrit par
le renderer, ou l'ajoute juste avant la fermeture de la balise ouvrante.

### `dsfr:field` et `dsfr-faces-html`

`dsfr:field` (cœur) ne rend que le `div` du groupe et son état. Le décor des enfants standard (mention
obligatoire et aide dans le libellé, `aria-required` et `aria-describedby` sur le champ) est ajouté par
`dsfr-faces-html` : `Decoration` regarde si le parent du composant rendu est un `DsfrField`, et le
`DsfrClassWriter` écrit ces ajouts dans le HTML produit par le renderer de l'implémentation. Mojarra et
MyFaces ouvrent le libellé dans `encodeBegin` et le ferment dans `encodeEnd` : la suite du libellé est
écrite juste avant `</label>`, dans le second appel.

### Outil de migration : édition en place

`dsfr-faces-migrate` n'utilise ni DOM ni XSLT, qui réécriraient tout le fichier (attributs, espaces,
entités). `XmlLite` relève la position de chaque élément et les zones à ne pas ré-indenter (valeurs
d'attribut, commentaires, CDATA). `FieldMigration` produit des insertions de texte appliquées de la fin vers
le début du fichier.

### `ajaxRendered` par un PartialViewContext

`DsfrPartialViewContextFactory` (déclaré dans le `faces-config.xml` du cœur) enveloppe le
`PartialViewContext` de l'implémentation. Au rendu d'une réponse ajax, il parcourt l'arbre
(`SKIP_UNRENDERED`, `SKIP_ITERATION`) et ajoute à `getRenderIds()` les composants dont l'attribut
`ajaxRendered` vaut `true`, sauf ceux déjà contenus dans une zone rafraîchie. L'attribut de composant
`fr.gouv.dsfr.limitRender.<événement>` (posé par `limitRender` des balises `a4j:`) désactive ce
mécanisme pour la requête.

Les `dsfr:field` sont ajoutés de la même façon quand leur champ figure dans `getExecuteIds()` (ou sous un
identifiant exécuté, ou `@all`), sauf si l'événement est une frappe (`keyup`, `keydown`, `keypress`,
`input`) envoyée par ce champ.

### Attributs de rendu lus dans la table d'attributs

Les composants de la phase 2 ne déclarent en Java que ce qui a un comportement : état conservé
(`activeItem`, `expanded`, `sortOrder`, `switchType`), types et événements ajax. Les attributs de simple
rendu (`label`, `hint`, `placeholder`, `size`…) sont lus par les renderers avec `getAttributes()`, qui
évalue aussi les expressions. Attention : une propriété Java dont le getter lirait `getAttributes()` sous
le même nom bouclerait à l'infini.

### Actions sans comportement client : la source de la requête

Le tri, la pagination et les onglets ajax envoient directement `faces.ajax.request(this, event, …)` :
l'identifiant de l'élément cliqué (p. ex. `form-table-sort-0`) arrive dans `jakarta.faces.source`, et le
renderer le reconnaît au décodage (`DsfrRendererBase.isSubmitted`). En ajax, seule la source compte, car
faces.js peut envoyer des champs nommés d'après des sources précédentes. En envoi complet, un champ ou
bouton au même nom suffit. Les composants concernés déclarent `@ResourceDependency` sur `faces.js`.

### Indépendance vis-à-vis du serveur

Uniquement des API en `provided` (`jakarta.jakartaee-web-api` 11). Les fichiers `faces-config.xml` et
la taglib sont déclarés en version 4.0 pour fonctionner aussi sous Faces 4.0. Aucun plugin ni
descripteur propre à un serveur.

## Conventions des renderers

### L'enveloppe porte le clientId

Pour un champ d'identifiant client `form-email` :

| Élément | id |
|---|---|
| `div.fr-input-group` | `form-email` (le clientId) |
| `input` | `form-email-input` (`name="form-email"`) |
| `div.fr-messages-group` | `form-email-messages` |

Conséquences :
- `render="@this"` ou `render="email"` rafraîchit le libellé, le champ et l'état d'erreur ensemble ;
- l'enveloppe est passée comme source de la requête ajax (`ClientBehaviorContext` avec
  `sourceId = clientId`), donc `jakarta.faces.source` et `execute="@this"` désignent bien le composant
  côté serveur.

### Accessibilité

- `<label for>` relié au champ, l'aide est incluse dans le libellé ;
- `aria-describedby` pointe vers le groupe de messages, toujours présent (zone `aria-live="polite"`
  qui doit exister avant que son contenu change) ;
- `aria-invalid="true"` en erreur ;
- `aria-required="true"` plutôt que `required` : la validation native du navigateur ne doit pas
  court-circuiter la validation Faces et ses messages DSFR ;
- les messages rendus sont marqués `FacesMessage.rendered()` (pas d'avertissement « messages non
  affichés » en mode Development).

### Attributs pass-through

Seul l'élément principal (`<input>`, `<button>`…) est ouvert avec le composant en second argument de
`ResponseWriter.startElement` ; les autres éléments le sont avec `null`. Mojarra et MyFaces écrivent
les attributs `p:` (`xmlns:p="jakarta.faces.passthrough"`) sur l'élément ouvert avec le composant.

### Comportements client

`DsfrRendererBase.renderBehaviors` traduit les événements logiques en événements DOM
(`valueChange` devient `change`, `action` devient `click`) et enchaîne plusieurs comportements avec
`faces.util.chain`. Pour un bouton, il ajoute `return false` quand un comportement soumet lui-même
(ajax). `decodeBehaviors` ne déclenche les comportements que si le composant est la source de la requête.

### Identifiants

Le séparateur recommandé est `-` (`jakarta.faces.SEPARATOR_CHAR`). Les suffixes `-input`, `-messages`
et `-messages-N` sont réservés.

## Ajouter un composant

1. **Composant** dans `component/`.
   - Étendre le composant standard le plus proche (`UIInput`, `UICommand`, `UIData`…).
   - Implémenter `ClientBehaviorHolder` si `f:ajax` doit être accepté.
   - Stocker les propriétés via `getStateHelper()`.
   - Annoter `@FacesComponent(TYPE)` et fixer le `rendererType` dans le constructeur.
2. **Renderer** dans `render/`.
   - Étendre `DsfrRendererBase` (ou `DsfrInputRendererBase` pour un champ).
   - Annoter `@FacesRenderer(componentFamily, rendererType)`.
   - Reproduire **exactement** le HTML des exemples officiels (`node_modules/@gouvfr/dsfr/example/component/…`).
3. **Balise** dans `META-INF/dsfr.taglib.xml`, avec la description de chaque attribut.
4. **Exemple** dans le showcase et **documentation** dans [composants.md](composants.md).
