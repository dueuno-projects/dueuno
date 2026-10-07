# Events

Each `Component` can trigger one or more events. See [components](components.md) and [controls](controls.md) for the events supported by each component and control.

Event names are lowercase. You can configure an event directly when creating a component by using the `on<EventName>` property, where `<EventName>` is the capitalized event name.

```groovy
c.form.with {
    addField(
            class: Select,
            id: 'book',
            onChange: 'onChangeBook', // <1>
            submit: ['form'],
    )
}
```

`<1>` The `onChange` property binds the `change` event to the `onChangeBook` action.

To configure multiple events on the same component, get a reference to the component and call `on(...)` once for each event.

```groovy
c.form.with {
    def books = addField(
            class: Select,
            id: 'book',
    ).component // <1>

    books.with {
        on( // <2>
                event: 'load',
                action: 'onLoadBooks',
        )
        on( // <3>
                event: 'change',
                action: 'onChangeBook',
                submit: ['form'],
        )
    }
}
```

- `<1>` Reference the component held by the `FormField`, not the form field itself.
- `<2>` Configure the `load` event.
- `<3>` Configure the `change` event.

The following properties are available when configuring an event.

## Properties

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `controller` | `String` |  | The controller to redirect to. If `action` is not specified, the `index` action is displayed. |
| `action` | `String` |  | The action to redirect to. If `controller` is not specified and the event runs in the context of a web request, such as a user-triggered event, the current controller is used. If the event is configured outside a web request, such as from a job, `controller` is required. |
| `params` | `Map<String, Object>` |  | The parameters to pass when redirecting to a `controller` or `action`. |
| `submit` | `List<String>` |  | The list of components whose values must be submitted with the event. Each component defines the data structure for the values it contains. By default, Dueuno submits the values of all controls contained in the component. |


# Transitions

A `Transition` is a set of instructions sent from the server to the browser to update the content currently displayed on the page.

For example, when the user selects a book from a list, the application can populate a text field with the selected book description. This kind of UI update is implemented with a transition.

> **NOTE:** See [controls](controls.md) and [components](components.md) for the events available on each component.

> **NOTE:** Events can also be triggered programmatically, for example from background jobs or server-side processes.

`grails-app/controllers/ReadController.groovy`

```groovy
class ReadController implements ElementsController {

    BookService bookService

    def index() {
        def c = createContent(ContentForm)

        c.header.removeNextButton()

        c.form.with {
            addField(
                    class: Select,
                    id: 'book',
                    optionsFromRecordset: bookService.list(),
                    onChange: 'onChangeBook', // <1>
            )
            addField(
                    class: Textarea,
                    id: 'description',
            )
        }

        display content: c
    }

    def onChangeBook() {
        def t = createTransition() // <2>
        def book = bookService.get(params.book)

        if (book) {
            t.set('description', book.description) // <3>
            t.set('description', 'readonly', true) // <4>
        } else {
            t.set('description', null)
            t.set('description', 'readonly', false)
        }

        display transition: t
    }
}
```

- `<1>` Configure the `Select` field to execute `onChangeBook` when the `change` event occurs.
- `<2>` Create a new `Transition`.
- `<3>` Set the value of the `description` field.
- `<4>` Set the `Textarea` to `readonly`.


To complete the example, register a pretty printer for book records and configure the `Select` control to use it when displaying the available items.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService

    def init = {

        applicationService.onInit {
            registerPrettyPrinter('BOOK', '${it.title} - ${it.author}') // <1>
        }

    }
}
```

`<1>` Register a `BOOK` pretty printer that displays each book by title and author. The `it` variable refers to the current book record, which is a `Map` in this example.

`grails-app/controllers/ReadController.groovy`

```groovy
class ReadController implements ElementsController {
    ...

        addField(
                class: Select,
                id: 'book',
                optionsFromRecordset: bookService.list(),
                prettyPrinter: 'BOOK', // <1>
                onChange: 'onChangeBook',
        )

    ...
}
```

`<1>` Configure the `Select` control to use the `BOOK` pretty printer.


## display()

The `display` method is one of the main features of _Dueuno_. It renders the GUI on the server and sends it to the browser.

You can call `display` with one or more of the following parameters.

| Name           | Type | Default | Description |
|----------------| --- | --- |-------------|
| `controller`   | `String` |  | The controller to redirect to. If `action` is not specified, the `index` action is displayed. |
| `action`       | `String` |  | The action to redirect to. If `controller` is not specified, the current controller is used. |
| `params`       | `Map<String, Object>` |  | The parameters to pass when redirecting to a `controller` or `action`. |
| `content`      | `PageContent` |  | The content to display. See [contents](contents.md). |
| `transition`   | `Transition` |  | The transition to display. See [transitions](#transitions). |
| `modal`        | `Boolean` |  | Whether to display the content in a modal dialog. |
| `small`        | `Boolean` |  | When `modal` is enabled, displays a tighter dialog. |
| `large`        | `Boolean` |  | When `modal` is enabled, displays a wider dialog. |
| `fullscreen`   | `Boolean` |  | When `modal` is enabled, makes the dialog fill the browser window. |
| `closeButton`  | `Boolean` | `true` | When `modal` is enabled, displays a close button in the top-left corner so the user can close the dialog and cancel the operation. |
| `errors`       | `org.springframework.validation.Errors` |  | Validation errors to display. |
| `errorMessage` | `String` |  | An error message to display to the user. |
| `exception`    | `Exception` |  | An exception to display to the user. |
| `message`      | `String` |  | A message to display to the user. |
