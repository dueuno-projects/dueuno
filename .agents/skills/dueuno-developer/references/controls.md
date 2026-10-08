# Controls

Controls are components that can hold a value. They are the main way users interact with the application, and they are usually placed in forms so their values can be submitted.

## TextField

A text field.

```groovy
c.form.addField(
    class: TextField,
    id: 'username',
    icon: 'fa-user',
    textStyle: TextStyle.LINE_THROUGH,
)
```

### Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `icon` | `String` |  | An icon to display inside the control. Choose one from [Font Awesome](https://fontawesome.com/). |
| `prefix` | `String` |  | Text to display before the editable area of the control. |
| `maxSize` | `Integer` |  | Maximum number of characters the user can enter. |
| `placeholder` | `String` |  | Text to display when the control is empty. |
| `textTransform` | `TextTransform` |  | Transforms the input while typing. |
| `textStyle` | `TextStyle` |  | Determines the text style. |
| `pattern` | `String` |  | A regular expression used to accept only specific input. For example, `'^[0-9\\.\\,]*$'` accepts only numbers, dots, and commas. |

`textTransform` values:

- `UPPERCASE`
- `LOWERCASE`
- `CAPITALIZE` each word

`textStyle` values:

- `NORMAL`
- `BOLD`
- `ITALIC`
- `MONOSPACE`
- `UNDERLINE`
- `LINE_THROUGH`

### Methods

| Method | Description |
| --- | --- |
| `addAction()` | Adds an action button at the end of the control. See [link](components.md#link). |

### Events

| Event | Description |
| --- | --- |
| `load` | Triggered once the content is loaded. |
| `change` | Triggered when the value changes. |

## Select

Displays a list of options to choose from.

### Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `optionsFromRecordset` | `List<Map>` or `List<Object>` or `GORM Recordset` |  | Sets options from a recordset. |
| `optionsFromList` | `List` |  | Sets options from list items. The key of each item matches the item value. |
| `optionsFromEnum` | `Enum` |  | Sets options from an enum. The key of each item matches the item value. |
| `options` | `Map` |  | Sets options from map entries using key/value pairs. |
| `keys` | `List<String>` | `['id']` | Column names to submit as the key for the selected option. |
| `prettyPrinter` | `Class` or `String` |  | Use the specified pretty printer to display the options. See `registerPrettyPrinter()`. If the registered pretty printer `Class` matches the item class, the pretty printer will be automatically applied. |
| `transformer` | `String` |  | Name of the transformer used to display the options. See `registerTransformer()`. |
| `textStyle` | `TextStyle` |  | Determines the text style. |
| `textPrefix` | `String` |  | Prefix added to each item so it can be localized through `message.properties`. |
| `renderTextPrefix` | `Boolean` | `true` | Whether to display `textPrefix`. |
| `placeholder` | `String` |  | Text to display when no option is selected. |
| `allowClear` | `Boolean` |  | If `true`, the selection can be cleared. |
| `autoSelect` | `Boolean` | `true` | When only one option is available, it is automatically selected. |
| `multiple` | `Boolean` | `false` | Enables multiple selection. |
| `search` | `Boolean` |  | Displays a search box to filter the available options. Client-side search is used by default. Use the `search` event for server-side search. |
| `searchMinInputLength` | `Integer` | `0` | Minimum number of characters required before server-side search can start. Works with the `search` event. |

`textStyle` values:

- `NORMAL`
- `BOLD`
- `ITALIC`
- `MONOSPACE`
- `UNDERLINE`
- `LINE_THROUGH`

### Methods

| Method | Description |
| --- | --- |
| `Select.optionsFromRecordset(recordset: ...)` | Returns a `Map` of options to use in a transition. See [search on server](#search-on-server). Accepts a `Map` with the following arguments: `keys`, `keysSeparator`, `prettyPrinter`, `transformer`, `textPrefix`, `renderTextPrefix`, `locale`. |
| `Select.optionsFromList(list: ...)` | Returns a `Map` of options to use in a transition. See [search on server](#search-on-server). Accepts a `Map` with the arguments listed above. |
| `Select.optionsFromEnum(enum: ...)` | Returns a `Map` of options to use in a transition. See [search on server](#search-on-server). Accepts a `Map` with the arguments listed above. |
| `Select.options(options: ...)` | Returns a `Map` of options to use in a transition. See [search on server](#search-on-server). Accepts a `Map` with the arguments listed above. |

### Events

| Event | Description |
| --- | --- |
| `load` | Triggered once the content is loaded. |
| `change` | Triggered when the value changes. |
| `search` | Triggered when `searchMinInputLength` is reached. |

### Search on Server

The following example configures server-side search.

```groovy
c.form.with {
    addField(
        class: Select,
        id: 'activity',
        onLoad: 'onActivityLoad', // <1>
        onChange: 'onActivityChange',
        onSearch: 'onActivitySearch', // <2>
        searchMinInputLength: 0, // <3>
        submit: ['form'],
        allowClear: true,
    )
}
```

- `<1>` The `load` event must return the selected option to display.
- `<2>` The `search` event returns the list of matching options.
- `<3>` If set to `0`, the `search` event is triggered as soon as the user opens the options list.

Create the following actions.

```groovy
ActivityService activityService

def onActivityLoad() {
    def t = createTransition()
    def activities = activityService.list(id: params.activity) // <1>
    def options = Select.optionsFromRecordset(recordset: activities)
    t.set('activity', 'options', options)
    display transition: t
}

def onActivityChange() {
    def t = createTransition()
    // Do something...
    display transition: t
}

def onActivitySearch() {
    def t = createTransition()
    def activities = activityService.list(find: params.activity) // <2>
    def options = Select.optionsFromRecordset(recordset: activities)
    t.set('activity', 'options', options)
    display transition: t
}
```

- `<1>` `params.activity` contains the selected ID.
- `<2>` `params.activity` contains the search string.

## Checkbox

A checkbox is a way to interact with `Boolean` values.

```groovy
c.form.with {
    addField(
        class: Checkbox,
        id: 'fullscreen',
        displayLabel: false,
        cols: 3,
    )
}
```

### Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `text` | `String` |  | The text to display. |

### Events

| Event | Description |
| --- | --- |
| `click` | Not implemented yet. |

## MultipleCheckbox

Manages multiple checkboxes as if they were a `Select` control with many options. See [Select](#select).

## Textarea

A text area that can span multiple lines in a form.

```groovy
c.form.with {
    addField(
        class: Textarea,
        id: 'textarea',
        maxSize: 100,
        cols: 12,
        rows: 5,
    )
}
```

### Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `maxSize` | `Integer` |  | Maximum number of characters the user can enter. |

### Events

| Event | Description |
| --- | --- |
| `change` | Triggered when the value changes. |

## QuantityField

A text field to input quantities.

```groovy
c.form.with {
    addField(
        class: QuantityField,
        id: 'quantity',
        defaultUnit: QuantityUnit.KM,
        availableUnits: quantityService.listAllUnits(),
    )
}
```

### Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `decimals` | `Integer` | `2` | Allowed decimal digits. |
| `negative` | `Boolean` | `false` | Whether negative values are allowed. |
| `unitOptions` | `List` |  | A list of units to select from. |
| `defaultUnit` | `QuantityUnit` |  | The default unit to display. |

### Events

| Event | Description |
| --- | --- |
| `load` | Triggered once the content is loaded. |
| `change` | Triggered when the value changes. |

## MoneyField

A text field to input currency values.

```groovy
c.form.with {
    addField(
        class: MoneyField,
        id: 'salary',
        decimals: 0,
    )
}
```

### Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `decimals` | `Integer` | `2` | Allowed decimal digits. |
| `negative` | `Boolean` | `false` | Whether negative values are allowed. |

### Events

| Event | Description |
| --- | --- |
| `load` | Triggered once the content is loaded. |
| `change` | Triggered when the value changes. |

## NumberField

A text field used to enter numeric values.

```groovy
c.form.with {
    addField(
        class: NumberField,
        id: 'number',
        min: -2,
        max: 10,
    )
}
```

### Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `decimals` | `Integer` | `2` | Allowed decimal digits. |
| `negative` | `Boolean` | `false` | Whether negative values are allowed. |
| `min` | `Integer` |  | Minimum number the user can enter. |
| `max` | `Integer` |  | Maximum number the user can enter. |
