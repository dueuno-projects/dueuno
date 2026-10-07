# Components

Everything in Dueuno is a `Component`. A component is a small web application made of an HTML view, CSS styling, and JavaScript logic. A component can also provide a supporting `Service` or `Controller`.

When building a standard _Dueuno_ application, you usually do not need to write HTML, CSS, or JavaScript unless you are creating a new component.

Each component extends the base `Component` class and shares the following properties and methods.

## Properties

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `id` | `String` |  | Component instance ID. It is mandatory, must be unique, and must be provided in the constructor. |
| `visible` | `Boolean` | `true` | Shows or hides the component without changing the layout. |
| `display` | `Boolean` | `true` | Displays or hides the component by adding it to, or removing it from, the layout. |
| `readonly` | `Boolean` | `false` | Disables readonly controls. |
| `skipFocus` | `Boolean` | `false` | Excludes the component from keyboard and mouse selection. |
| `sticky` | `Boolean` |  | Makes the component stick to the top. |
| `containerSpecs` | `Map` |  | Instructions for the container. The container component may or may not respect them; see the documentation for the specific container component. |
| `textColor` | `String` |  | Text color in CSS format. |
| `backgroundColor` | `String` |  | Background color in CSS format. |
| `cssClass` | `String` |  | Custom CSS class to apply. The CSS class must be a [Bootstrap](https://getbootstrap.com/) CSS class or a custom class declared in `grails-app/assets/dueuno/custom/application.css`. |

## Methods

| Method | Description |
| --- | --- |
| `addComponent(Map)` | Adds a child component. |
| `addControl(Map)` | Adds a child control. See [controls](controls.md). |
| `on(Map)` | Configures an event. See [events](events.md). |

## Header

A `Header` is a bar at the top of the `Content` area. It can stick to the top or scroll with the content. Its main purpose is to hold navigation buttons.

A `Header` can have a `backButton` on the left, a `nextButton` on the right, and a `title` in the middle.

### Properties

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `sticky` | `Boolean` |  | When `true`, the header sticks to the top. Adding a `backButton` or `nextButton` automatically sets `sticky` to `true` so the buttons remain reachable after scrolling. To force the header to scroll with the content, explicitly set `sticky` to `false`. |
| `title` | `String` |  | The title to display. |
| `titleArgs` | `List` |  | Arguments used when resolving an i18n message. For example, if `messages.properties` contains `book.index.header.title=Books for {0} {1}` and `titleArgs = ['Mario', 'Rossi']`, the title becomes `Books for Mario Rossi`. |
| `icon` | `String` |  | An icon displayed before the `title`. Choose one from [Font Awesome](https://fontawesome.com/). |
| `hasBackButton` | `Boolean` |  | `true` if a `backButton` has been added. |
| `hasNextButton` | `Boolean` |  | `true` if a `nextButton` has been added. |
| `backButton` | `Button` |  | The back button object. See [button](#button). |
| `nextButton` | `Button` |  | The next button object. See [button](#button). |

### Methods

| Method | Description |
| --- | --- |
| `addBackButton(Map)` | Adds the `backButton`. Accepts [button](#button) arguments. |
| `removeBackButton()` | Removes the `backButton`. |
| `addNextButton(Map)` | Adds the `nextButton`. Accepts [button](#button) arguments. |
| `removeNextButton()` | Removes the `nextButton`. |

## Table

A `Table` displays a recordset.

Each table can define [table filters](#tablefilters), and each row can have its own action buttons. Available row actions can depend on the logged-in user, the record status, or other application logic.

### Properties

#### `columns`

- Type: `List<String>`
- Description: A list of column names to display. Each column name must match the recordset column name to automatically display its values.

```groovy
c.table.with {
    columns = [
        'title',
        'author',
    ]
}
```

#### `keys`

- Type: `List<String>`
- Description: List of key names. When specified, a new column is created for each key. Keys are automatically submitted when a row action is activated.

```groovy
c.table.with {
    keys = [
        'publisher_id',
    ]
}
```

#### `sortable`

- Type: `Map<String, String>`
- Description: Defines the sortable columns.

```groovy
c.table.with {
    sortable = [
        title: 'asc',
    ]
}
```

#### `sort`

- Type: `Map<String, String>`
- Description: Defines the sorting of the recordset. It takes precedence over the `sortable` property and forces the specified sorting.

```groovy
c.table.with {
    sort = [
        title: 'asc',
    ]
}
```

#### `submit`

- Type: `List<String>`
- Description: Column names whose values must be included when the table is submitted by a [button](#button) or [link](#link).

```groovy
c.table.with {
    submit = [
        'author',
    ]
}
```

#### `labels`

- Type: `Map<String, String>`
- Description: Programmatically change the label of the specified columns.

```groovy
c.table.with {
    labels = [
        author: '-',
    ]
}
```

#### `transformers`

- Type: `Map<String, String>`
- Description: Sets a transformer to a column. Each value of that column will be processed by the specified transformer. See `registerTransformer()`.

```groovy
c.table.with {
    transformers = [
        title: 'UPPERCASE_TITLE',
    ]
}
```

#### `prettyPrinters`

- Type: `Map<String, Object>`
- Description: Sets a pretty printer to a column. Each value of that column will be processed by the specified pretty printer. See `registerPrettyPrinter()`.

```groovy
c.table.with {
    prettyPrinter = [
        title: '${it.code}',
    ]
}
```

#### `prettyPrinterProperties`

- Type: `Map<String, Map>`
- Description: Sets pretty printer properties for a column. Each value in that column is processed with the specified properties. See [PrettyPrinterProperties](features.md#prettyprinterproperties).

```groovy
c.table.with {
    prettyPrinterProperties = [
        salary: [
            highlightNegative: false,
            renderZero: '-',
        ],
        name: [
            renderTextPrefix: true,
        ],
    ]
}
```

#### `stickyHeader`

- Type: `Boolean`
- Default: `true`
- Description: If `true`, the table header sticks to the top while scrolling. Not available in modals.

#### `filters`

- Type: `TableFilters`
- Description: Defines table filters.

```groovy
c.table.with {
    filters.with {
        addField(
            class: TextField,
            id: 'title',
            cols: 6,
        )
        addField(
            class: TextField,
            id: 'author',
            cols: 6,
        )
    }
}

Map filters = c.table.filters.values // <1>
```

`<1>` The submitted values of the filter fields. See [table filters](#tablefilters).

#### Other Properties

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `rowActions` | `Boolean` | `true` | Whether to display row action buttons. |
| `hasHeader` | `Boolean` | `true` | Whether to display the table header. |
| `hasFooter` | `Boolean` | `true` | Whether to display the table footer. |
| `hasPagination` | `Boolean` | `true` | Whether to display table pagination. |
| `hasComponents` | `Boolean` | `false` | Whether the table cells can host custom components. Enabling this feature slows down rendering. |
| `rowHighlight` | `Boolean` | `true` | Whether to highlight rows on pointer hover. |
| `rowStriped` | `Boolean` | `false` | Whether to use zebra striping. |
| `noResults` | `Boolean` | `true` | Whether to display a message box when the table has no results. |
| `noResultsIcon` | `String` |  | Icon to display when the table has no results. Choose one from [Font Awesome](https://fontawesome.com/). |
| `noResultsMessage` | `String` |  | Message to display when the table has no results. |

### Methods

#### `body`

Assigns a recordset to the table body. See [recordsets](#recordsets).

```groovy
c.table.body = bookService.list()
```

#### `footer`

Assigns a recordset to the table footer. See [recordsets](#recordsets).

```groovy
c.table.footer = bookService.listTotals()
```

#### `paginate`

When set, the table paginates the results. Set this value to the total number of records to display.

```groovy
c.table.paginate = bookService.count()
```

#### `eachRow`

This closure is called for each row displayed by the table. Avoid slow code here because it affects table rendering performance.

```groovy
c.table.body.eachRow { TableRow row, Map values -> // <1>
    row.cells['title'] // <2>
    row.actions // <3>
}
```

- `<1>` The record values.
- `<2>` See [label](#label).
- `<3>` See [row actions](#row-actions).

### Recordsets

Tables can be loaded from several data structures.

#### List of Lists

You can load a table with a list of lists. In this case, value order determines how each column maps to each value. There is no strict relationship between the displayed column name and the value.

For this reason, prefer [list of maps](#list-of-maps) when possible.

```groovy
c.table.columns = [
    'title',
    'author',
    'description',
]

c.table.body = [
    ['The Teachings of Don Juan', 'Carlos Castaneda', 'This is a nice fictional book'],
    ['The Antipodes of the Mind', 'Benny Shanon', 'This is a nice scientific book'],
]
```

#### List of Maps

You can load a table with a recordset-style data structure such as a list of maps. Each column displays the value associated with the map key that matches the column name.

```groovy
c.table.columns = [
    'title',
    'author',
    'id',
]

c.table.body = [
    [id: '1', title: 'The Teachings of Don Juan', author: 'Carlos Castaneda', description: 'This is a nice fictional book'],
    [id: '2', title: 'The Antipodes of the Mind', author: 'Benny Shanon', description: 'This is a nice scientific book'],
]
```

#### List of POGOs

A list of _Plain Old Groovy Objects_ can also be used to load a table.

Given this POGO:

```groovy
class Book {
    String id
    String title
    String author
    String description
}
```

Then load the table:

```groovy
c.table.columns = [
    'title',
    'author',
    'id',
]

c.table.body = [
    new Book(id: '1', title: 'The Teachings of Don Juan', author: 'Carlos Castaneda', description: 'This is a nice fictional book'),
    new Book(id: '2', title: 'The Antipodes of the Mind', author: 'Benny Shanon', description: 'This is a nice scientific book'),
]
```

#### GORM Recordsets

Using a [GORM Recordset](https://gorm.grails.org/latest/hibernate/manual/) is an easy way to load a table.

```groovy
c.table.columns = [
    'title',
    'author',
]

c.table.body = TBook.list()
c.table.paginate = TBook.count()
```

### Row Actions

There are two ways to configure row actions: for all rows at once, or row by row. To give all rows the same actions, configure them in the table namespace.

```groovy
c.table.with {
    columns = [
        'title',
        'author',
    ]
    actions.addAction(action: 'borrow') // <1>
    actions.addAction(action: 'return')
}
```

`<1>` See [button](#button) for all the `Button` properties.

To configure row actions based on record values or other logic, use the `eachRow` closure.

```groovy
c.table.with {
    columns = [
        'title',
        'author',
    ]

    body.eachRow {
        if (values.borrowed) {
            row.actions.addAction(action: 'return') // <1>
        } else {
            row.actions.addAction(action: 'borrow')
        }
    }
}
```

`<1>` See [button](#button) for all the `Button` properties.

### Group Actions

The table can allow users to select multiple rows and apply the same action to all of them.

```groovy
c.table.with {
    columns = [
        'title',
        'author',
    ]

    groupActions.addAction(action: 'return') // <1>
    groupActions.addAction(action: 'borrow')
}
```

`<1>` See [button](#button) for all the `Button` properties.

## TableFilters

Each table can have its own search `Form` for filtering results. When the filters are submitted, the action containing the table is reloaded and the filter values are available in the Grails `params` map.

```groovy
c.table.with {
    filters.with {
        addField(
            class: Select,
            optionsFromRecordset: bookService.list(),
            prettyPrinter: 'BOOK',
            id: 'book',
            cols: 4,
        )
        addField(
            class: TextField,
            id: 'search',
            cols: 8,
        )
    }

    Map filters = c.table.filters.values // <1>
}
```

`<1>` The submitted values of the filter fields.

### Properties

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `isFiltering` | `Boolean` |  | `true` if the filters form contains values. |
| `fold` | `Boolean` | `true` | Whether the filters form is folded the first time it appears. After that, the folded state is stored in the session. |
| `autoFold` | `Boolean` | `false` | If `true`, the filters form is folded each time a search is submitted. |

### Methods

| Method | Description |
| --- | --- |
| `addField()` | Adds a form field. See [form field](#formfield) and [controls](controls.md). |

## Form

A form is the component used to lay out components and controls. `Form` implements a grid system. Once enabled, the grid provides 12 columns for arranging form fields horizontally.

On mobile phones, all fields are displayed in a single column so the form remains usable when there is not enough space for a horizontal layout.

```groovy
c.form.with {
    grid = true
    addField(
        class: TextField,
        id: 'title',
        cols: 6,
    )
    addField(
        class: TextField,
        id: 'author',
        cols: 6,
    )
}
```

### Properties

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `validate` | `Class` |  | A `grails.validation.Validateable` class or GORM domain class used to automatically render fields as required. A red `*` is displayed next to the field label when appropriate. |
| `grid` | `Boolean` | `false` | Whether to activate the grid system. |
| `readonly` | `Boolean` | `false` | Sets all form fields to readonly. |

### Methods

| Method | Description |
| --- | --- |
| `addField()` | Adds a form field. See [form field](#formfield) and [controls](controls.md). |

## FormField

A form field wraps a `Control` with a label and places it in the grid system. A `FormField` is created automatically each time `addField()` is called on a `Form`.

### Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `component` | `Component` |  | The contained component. |
| `label` | `String` |  | The field label. |
| `labelArgs` | `List` |  | Objects to pass to the localized message, for example when using `{0}` in `message.properties`. |
| `help` | `String` |  | A help message. |
| `helpArgs` | `List` |  | Objects to pass to the localized help message, for example when using `{0}` in `message.properties`. |
| `nullable` | `Boolean` | `true` | Whether to display the field as nullable. If set, it overrides the form `validate` logic. See [form](#form). |
| `displayLabel` | `Boolean` |  | If `false`, the label is not displayed and its space is removed, changing the vertical position of the `Control`. |
| `cols` | `Integer` |  | Number of grid columns spanned by the `Control`. The value must be between `1` and `12`. |
| `rows` | `Integer` |  | For multiline controls, the number of lines to occupy. |

## Button

Buttons let users trigger actions. A `Button` can expose multiple actions.

A single button can display two directly accessible actions, `defaultAction` and `tailAction`, plus a menu of links called `actionMenu`.

| `defaultAction` | `tailAction` | `actionMenu` |
| --- | --- | --- |
|  |  |  |

A simple button only has `defaultAction`.

```groovy
c.form.with {
    def addBookField = addField( // <1>
        class: Button,
        id: 'addBook',
        action: 'addBook',
        submit: ['form'],
    )

    def button = addBookField.component
    button.addAction(controller: 'addAuthor')
}
```

`<1>` A `Button` can be initialized with event properties. See [events](events.md) and [link](#link).

### Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `defaultAction` | `Menu` |  | The default action. |
| `tailAction` | `Menu` |  | The tail action. |
| `actionMenu` | `Menu` |  | The action menu. |
| `primary` | `Boolean` | `false` | When `true`, the button uses the `PRIMARY_BACKGROUND_COLOR` and `PRIMARY_TEXT_COLOR` tenant properties to indicate that it has the primary role in the GUI. |
| `stretch` | `Boolean` | `false` | If `true`, the button fills all available horizontal space. |
| `group` | `Boolean` | `false` | If `true`, all button actions are displayed inline and are directly accessible. |
| `maxWidth` | `Integer` |  | Maximum button width in pixels. |

### Events

| Event | Description |
| --- | --- |
| `click` | Triggered by mouse click or finger tap on touch devices. |

## Menu

A menu organizes `Shell` and `Button` menus. It can hold a tree of items with a parent-child structure, but Dueuno uses only one level to group items. See [features](features.md).

This component is meant for internal use only.

## Link

Links appear in `Shell` menus, `Button` actions, `TextField` actions, and `Select` actions. They can also be used on their own. Links and buttons share the same properties.

```groovy
c.form.with {
    addField( // <1>
        class: Link,
        id: 'addBook',
        action: 'addBook',
        submit: ['form'],
        icon: 'fa-book',
    )
}
```

`<1>` A `Link` can be initialized with [label](#label) properties and event properties. See [events](events.md).

### Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `icon` | `String` |  | Icon that represents the link. Choose one from [Font Awesome](https://fontawesome.com/). |
| `image` | `String` |  | SVG image that represents the link. If specified, a corresponding file must exist in `grails-app/assets`. |
| `text` | `String` |  | Label that describes the link, usually a code from `messages.properties`. |
| `url` | `String` |  | Points to a specific URL. |
| `direct` | `Boolean` |  | Whether to render the whole HTML page or raw HTTP body instead of a `Transition`. |
| `target` | `String` |  | Target name used to open the page in a browser tab. Links with the same target display in the same tab. |
| `targetNew` | `Boolean` |  | If `true`, the link opens a new tab each time it is clicked. |
| `modal` | `Boolean` |  | Whether to display the content in a modal dialog. |
| `small`        | `Boolean` |  | When `modal` is enabled, displays a tighter dialog. |
| `large`        | `Boolean` |  | When `modal` is enabled, displays a wider dialog. |
| `fullscreen` | `Boolean` |  | When `modal` is enabled, makes the dialog fill the browser window. |
| `closeButton` | `Boolean` | `true` | When `modal` is enabled, displays a close button in the top-left corner so the user can close the dialog and cancel the operation. |
| `updateUrl` | `Boolean` | `false` | If `true`, updates the browser address bar with the link destination URL. On mobile phones, the address bar is never updated. |
| `animate` | `String` |  | Can be set to `fade`, `next`, or `back`. Currently, only `fade` is implemented as a graphical transition when changing content. |
| `infoMessage` | `String` |  | If specified, displays an info message and prevents the link from executing. |
| `confirmMessage` | `String` |  | If specified, displays a confirmation message so the user can cancel the action. |

### Events

| Event | Description |
| --- | --- |
| `click` | Triggered by mouse click or finger tap on touch devices. |

## Label

A `Label` is a canvas for text and custom HTML.

```groovy
c.form.with {
    addField(
        class: Label,
        id: 'label',
        html: '<b>This is a bold statement!</b>',
        textAlign: TextAlign.END,
        textWrap: TextWrap.LINE_WRAP,
        textStyle: TextStyle.BOLD,
    )
}
```

### Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `text` | `Object` |  | Text to display. If it is a `Boolean`, a check mark is displayed. |
| `html` | `String` |  | HTML string used to format text or insert links. |
| `url` | `String` |  | If specified, `text` becomes a link to this URL. |
| `icon` | `String` |  | Icon to display before the text. Choose one from [Font Awesome](https://fontawesome.com/). |
| `textAlign` | `TextAlign` |  | Determines the horizontal text alignment. It can be set to `DEFAULT`, `START`, `END`, or `CENTER`. Default: `DEFAULT`. |
| `textWrap` | `TextWrap` |  | Determines how the text is wrapped. |
| `textStyle` | `TextStyle` |  | Determines the text style. |
| `border` | `Boolean` |  | Draws a colored background. Useful when the label must be displayed with a different color. |
| `renderBoolean` | `Boolean` | `true` | If `true`, displays a check symbol. Otherwise, displays the text `true` or `false`. |

`textWrap` values:

- `NO_WRAP` The text is displayed on one line.
- `SOFT_WRAP` The text wraps when the container maximum width is reached. Line breaks are not considered.
- `LINE_WRAP` Each line is displayed on one line until the container maximum width is reached. Line breaks are considered.
- `LINE_BREAK` Each line is displayed on one line. Line breaks are considered.

`textStyle` values:

- `NORMAL`
- `BOLD`
- `ITALIC`
- `MONOSPACE`
- `UNDERLINE`
- `LINE_THROUGH`

## Separator

Use separators to add space between groups of fields in a form.

### Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `squeeze` | `Boolean` |  | Reduces the space introduced by the separator, leaving only the space for the label. |

## KeyPress

Use the `KeyPress` component to intercept keys pressed by the user in the GUI. Its main use is barcode reader integration, but it can be used for other scenarios.

```groovy
def c = createContent(ContentTable)
c.addComponent(
    class: KeyPress,
    id: 'keyPress',
    action: 'onKeyPress', // <1>
)
```

`<1>` See [events](events.md) to configure the event.

### Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `triggerKey` | `String` | `'Enter'` | Pressed keys are stored in a buffer until the trigger key is pressed. Then the configured event is called. The trigger key can be any character or `Enter`. If set to an empty string `''`, each key press is sent immediately. |
