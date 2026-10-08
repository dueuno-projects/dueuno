# Application

`ApplicationService` is the main entry point for application setup. It is usually configured in `BootStrap.groovy`.

## onInstall()

Runs only the first time the application starts.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService

    def init = {

        applicationService.onInstall {
            // ...
        }

    }
}
```

## onTenantInstall()

Installs tenant-specific data. This closure runs once for the `DEFAULT` tenant during the first application startup, and again each time a new tenant is created from the super admin GUI.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService

    def init = {

        applicationService.onTenantInstall { String tenantId -> //<1>
            // ...
        }

    }
}
```

1. `tenantId` identifies the tenant being installed.

## onDevInstall()

Runs only once when the application starts from the IDE and the development environment is active. Use it to preload data for local testing.

This closure is not executed when the application runs as a JAR, as a WAR, or in the test environment.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService

    def init = {

        applicationService.onDevInstall { String tenantId ->
            // ...
        }

    }
}
```

## onUpdate()

Use `onUpdate` to run versioned upgrade logic during application releases, such as database updates or other one-time migration tasks.

Each closure runs only once at startup. Execution order is determined alphabetically by the version string passed as the first argument.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService

    def init = { // <1>

        applicationService.onUpdate('2021-01-03') { String tenantId ->
            println "${tenantId}: UPDATE N.2"
        }

        applicationService.onUpdate('2021-01-02') { String tenantId ->
            println "${tenantId}: UPDATE N.1"
        }

        applicationService.onUpdate('2021-01-05') { String tenantId ->
            println "${tenantId}: UPDATE N.4"
        }

        applicationService.onUpdate('2021-01-04') { String tenantId ->
            println "${tenantId}: UPDATE N.3"
        }
    }
}
```

1. The closures run in the following order, based on the specified version string: `2021-01-02`, `2021-01-03`, `2021-01-04`, `2021-01-05`.

## onInit()

Initializes the application. This closure runs every time the application starts.

IMPORTANT: Define the `onInit` closure after `onInstall`, `onTenantInstall`, and `onDevInstall`; otherwise those installation closures will not run.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService // <1>

    def init = {

        applicationService.onInit { // <2>
            // ...
        }

    }
}
```

1. Injects an instance of `ApplicationService`.
2. The `onInit { ... }` closure runs every time the application starts.

## afterLogin()

Runs after a user logs in. The session is active, so session variables can be set here.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    SecurityService securityService // <1>

    def init = {

        securityService.afterLogin {
            // ...
        }

    }
}
```

1. Injects an instance of `SecurityService`.

## afterLogout()

Runs after a user logs out. The session is not active, so session variables cannot be managed here.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    SecurityService securityService // <1>

    def init = {

        securityService.afterLogout {
            // ...
        }

    }
}
```

1. Injects an instance of `SecurityService`.

## Initialize the database with sample data

Use `onTenantInstall` when data must be created once during tenant installation. Use `onDevInstall` when data is only needed in the development environment.

Because `onDevInstall` runs only once in development, it is a good place to initialize lookup tables, sample records, and realistic data for manual testing.

Inject the service that owns the business logic and use its `create()` method instead of creating domain objects directly.

Set the `failOnError` argument to `true` to prevent the application from running if there is an error.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService
    CompanyService companyService

    def init = {

        applicationService.onDevInstall { String tenantId ->
            companyService.create(
                    failOnError: true,
                    name: 'Northwind Trading',
                    vat: 'IT02468135790',
                    address: 'Via Torino 15, Milano',
            )

            companyService.create(
                    failOnError: true,
                    name: 'Blue Harbor Logistics',
                    vat: 'IT13579246801',
                    address: 'Porto Industriale 8, Genova',
            )

            companyService.create(
                    failOnError: true,
                    name: 'Alpine Software Lab',
                    vat: 'IT99887766554',
                    address: 'Corso Trento 42, Bolzano',
            )

            companyService.create(
                    failOnError: true,
                    name: 'Green Valley Foods',
                    vat: 'IT11223344556',
                    address: 'Strada Agricola 3, Parma',
            )
        }

    }
}
```

The `tenantId` argument identifies the tenant being initialized. In multi-tenant applications, use it only when the sample data must change depending on the tenant.

# URL Path

You can make the application available under a specific URL path, for example `http://localhost:8080/admin`.

`grails-app/init/BootStrap.groovy`

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService
    TenantPropertyService tenantPropertyService

    def init = {

        applicationService.onTenantInstall { String tenantId ->
            tenantPropertyService.setString('SHELL_URL_MAPPING', '/admin')
            tenantPropertyService.setString('LOGIN_LANDING_URL', '/') // <1>
            tenantPropertyService.setString('LOGOUT_LANDING_URL', '/') // <2>
        }

    }
}
```

1. After login, the shell redirects to this path.
2. After logout, the shell redirects to this path.

The URL mappings must also match the new configuration. For example, if the main website controller is `WebsiteController.groovy`, create the following file:

`grails-app/controllers/WebsiteUrlMappings.groovy`

```groovy
class WebsiteUrlMappings {

    static mappings = {
        "/"(controller: 'website')
        "/admin"(controller: 'shell') // <1>
    }

}
```

1. The configured path must match the value of the `SHELL_URL_MAPPING` tenant property.
