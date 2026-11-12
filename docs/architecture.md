# Architecture

## Modules

```
dsfr-faces/
├── pom.xml                  parent : versions (Java 17, Jakarta EE 11 provided, DSFR, Node)
├── dsfr-faces-resources/    dist/ du DSFR -> META-INF/resources/dsfr
│   ├── package.json         @gouvfr/dsfr, installé par le build Maven (npm ci)
│   ├── package-lock.json
│   └── .dsfr.yml            acceptation de licence pour un npm ci manuel
├── dsfr-faces-core/         composants, renderers, taglib
│   └── fr.gouv.dsfr.faces
│       ├── Dsfr                       constantes, URL des ressources, version
│       ├── component/                 UIComponent (état via StateHelper)
│       └── render/                    Renderers
│           ├── DsfrRendererBase       comportements client (f:ajax), sévérités, utilitaires
│           └── DsfrInputRendererBase  décodage, conversion, libellé, groupe de messages
└── dsfr-faces-showcase/     WAR de démonstration
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
La compatibilité `a4j:` (voir [migration](migration-richfaces.md)) sera construite au-dessus de `f:ajax`.

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
