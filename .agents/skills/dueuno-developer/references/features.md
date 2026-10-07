# Features

A _Dueuno_ application exposes a finite set of features to its users. Features are defined in the `init` closure. The main menu on the right side of the GUI lists the features available to the current user based on their privileges.

After registration, features are implemented by their controllers.

## registerFeature()

Registers a feature.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService

    def init = {

        applicationService.onInit {
            registerFeature(
                    controller: 'book', // <1>
                    action: 'index', // <2>
                    icon: 'fa-book', // <3>
                    authorities: ['ROLE_CAN_EDIT_BOOKS'] // <4>
            )
            registerFeature(
                    controller: 'read',
                    icon: 'fa-glasses',
            )

            registerFeature(
                    controller: 'configuration', // <5>
            )
            registerFeature(
                    parent: 'configuration', // <6>
                    controller: 'authors',
                    icon: 'fa-user',
            )
            registerFeature(
                    parent: 'configuration',
                    controller: 'publishers',
                    icon: 'fa-user-shield',
            )
        }

    }
}
```

- `<1>` The controller that implements the feature.
- `<2>` The action to execute when the feature is clicked. The default is `index`.
- `<3>` The menu item icon. Choose one from [Font Awesome](https://fontawesome.com/).
- `<4>` The feature is displayed only to users with one of the listed roles. The default is `ROLE_USER`.
- `<5>` A feature with only a controller can group other features and act as their parent.
- `<6>` Assigns the feature to its parent.

> **IMPORTANT:** To block users without the required authority, the controller class must also be annotated with `@Secured(['ROLE_CAN_EDIT_BOOKS'])`.

Available options:

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `controller` | `String` | `controllerName` | The controller that implements the feature. If omitted, it is set to the current controller name. |
| `action` | `String` | `'index'` | The action to execute. |
| `params` | `Map<String, Object>` |  | Parameters to pass when calling `action` or `url`. |
| `submit` | `List<String>` |  | Component names to process when collecting values to pass to `action` or `url`. |
| `icon` | `String` |  | The menu item icon. Choose one from [Font Awesome](https://fontawesome.com/). |
| `authorities` | `List<String>` | `['ROLE_USER']` | The feature is displayed only to users with one of the listed roles. |
| `favourite` | `Boolean` |  | If `true`, the feature is also displayed on the bookmark page, accessible from the home menu. |
| `url` | `String` |  | An absolute URL. When specified, it takes precedence over `controller` and `action`. |
| `direct` | `Boolean` |  | Menu items are URLs managed by _Dueuno_. When `true`, the browser handles the URL directly without additional processing. |
| `target` | `String` |  | Displays the feature in a new browser tab with the provided name. |
| `targetNew` | `String` |  | Displays the feature in a new browser tab using `_blank`. |
| `confirmMessage` | `String` |  | A confirmation message displayed before opening the feature. The user can cancel or confirm the operation. |
| `infoMessage` | `String` |  | If set, this message is displayed instead of the feature. |

## registerUserFeature()

Registers a feature in the _User Menu_. It accepts the same options as [registerFeature()](#registerfeature).

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService

    def init = {

        applicationService.onInit {
            registerUserFeature(
                    controller: 'manual',
                    icon: 'fa-book',
                    targetNew: true,
            )
        }

    }
}
```

## registerPrettyPrinter()

Registers a string template used to render an instance of a specific class. A pretty printer can also be registered by name only; in that case, it must be explicitly assigned to a control when the control is defined.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService

    def init = {

        applicationService.onInit {
            registerPrettyPrinter(TProject, '${it.name}') //<1>
            registerPrettyPrinter('PROJECT_ID', '${it.padLeft(4, "0")}') // <2>
        }

    }
}
```

- `<1>` Registers a pretty printer for the `TProject` domain class. The `it` variable refers to a `TProject` instance, and this example displays its `name` property.
- `<2>` Registers a pretty printer named `PROJECT_ID`. Because the project ID is a `String`, the template can call `padLeft()` on it.

### PrettyPrinterProperties

Every value in _Dueuno_ is displayed through the `PrettyPrinter` subsystem. [Components](components.md) and [controls](controls.md) can override user and system settings. See the documentation for each component to learn which settings are supported.

| Name | Type | Default | Description |
| --- | --- | --- | --- |
| `prettyPrinter` | `Object` |  | `Class` or `String` name of the pretty printer. |
| `transformer` | `String` |  | Transformer name. |
| `locale` | `Locale` |  | - |
| `renderTextPrefix` | `Boolean` |  | Default: `false`. Set to `true` to translate the value through `message.properties`. |
| `textPrefix` | `String` |  | Adds or changes the message prefix. |
| `textArgs` | `List` |  | Arguments for the i18n message. |
| `renderBoolean` | `Boolean` | `true` | If `false`, renders the text `true` or `false`. Otherwise, renders a check symbol when the value is `true` and nothing when it is `false`. |
| `highlightNegative` | `Boolean` | `false` | If the value is `< 0`, highlights the text in red. |
| `renderZero` | `String` |  | If the value is `0`, renders the specified string instead. |
| `renderDate` | `Boolean` |  | For `LocalDateTime` values, whether to render the date part. |
| `renderDatePattern` | `String` |  | Changes how the date is rendered. See [DateTimeFormatter](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html). |
| `renderTime` | `Boolean` |  | For `LocalDateTime` values, whether to render the time part. |
| `renderSeconds` | `Boolean` |  | For `LocalTime` values, whether to display seconds. |
| `renderDelimiter` | `String` | `', '` | For `Map` and `List` values, uses this delimiter to list the items. |
| `decimals` | `Integer` |  | For `Number` values, how many decimal digits to display. |
| `decimalFormat` | `String` | `ISO_COM` | For `Number` values, which decimal separator to use. It can be `ISO_COM` (,) or `ISO_DOT` (.). |
| `prefixedUnit` | `Boolean` | `false` | For `Quantity` and `Money` values, whether to display the unit of measure before the value. |
| `symbolicCurrency` | `Boolean` | `true` | For `Money` values, whether to display the currency as a symbol or an ISO code. |
| `symbolicQuantity` | `Boolean` | `true` | For `Quantity` values, whether to display the unit of measure as a symbol or an SI code. |
| `invertedMonth` | `Boolean` | `false` | For `Date` values, whether to display month/day/year (`true`) or day/month/year (`false`). |
| `twelveHours` | `Boolean` | `false` | For `Time` values, whether to display 12-hour time (`true`, with AM/PM) or 24-hour time (`false`). |
| `firstDaySunday` | `Boolean` | `false` | Whether to display Sunday as the first day of the week. |


## registerTransformer()

Registers a callback used to render an instance of a specific class. To use a transformer, explicitly assign it to a control when defining the control.

> **NOTE:** The closure receives the value being transformed and must return a `String`.

> **IMPORTANT:** Use transformers carefully. Slow closures can affect rendering performance.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService
    SecurityService securityService

    def init = {

        applicationService.onInit {
            registerTransformer('USER_FULLNAME') { Object value ->
                return securityService.getUserByUsername(value).fullname
            }
        }

    }
}
```

## registerCredits()

Registers a role and the people who held that role during project development. When credits are registered, a new menu item appears in the _User Menu_.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService

    def init = {

        applicationService.onInit {
            registerCredits('Application Development', 'Francesco Piceghello', 'Gianluca Sartori')
        }

    }
}
```
