# Contents

Contents are the canvas for each feature. You can create a `ContentBlank`, which is an empty canvas, and add components to it manually. In most cases this is not necessary because Dueuno provides ready-to-use content classes.

Components are added to content vertically, one after another. Content classes do not provide layout controls directly. To create a layout, use the `Form` component or implement a custom component.

`ContentBase`
Embeds a `Header` and a confirm button that submits a component named `form` to the `onConfirm` action. The `form` component is not provided by `ContentBase`.

`ContentForm`
Extends `ContentHeader` and embeds a `Form` named `form`.

`ContentCreate`
Extends `ContentForm` and provides a create button that submits the `form` component to the `onCreate` action.

`ContentEdit`
Extends `ContentForm` and provides a save button that submits the `form` component to the `onEdit` action.

`ContentTable`
Extends `ContentHeader` and embeds a `Table` component. It also provides a new button that redirects to the `create` action.

The `Table` component displays edit and delete buttons for each row. The edit button submits the row `id` to the `edit` action. The delete button asks for confirmation before redirecting to the `onDelete` action.
