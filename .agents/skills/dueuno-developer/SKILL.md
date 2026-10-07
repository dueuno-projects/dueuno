---
name: dueuno-developer
description: |
  Expert knowledge for developing Dueuno full-stack applications using Groovy and Grails.
  Covers architecture, UI (Dueuno), business logic, security, and advanced features.
  Use this skill when building or modifying Dueuno applications, UI components, services, or system configuration.
version: 1.0.0
tags:
  - groovy
  - grails
  - dueuno
  - fullstack
  - enterprise
  - backend
  - low-code
---

# Dueuno Developer Skill

## When to use this skill
Use this skill when the task involves:
- Building or modifying a Dueuno application
- Designing UI with Dueuno Elements (ContentTable, ContentForm, etc.)
- Implementing business logic with GORM / Grails services
- Managing multi-tenant architecture
- Configuring security (Spring Security / roles / permissions)
- Customizing application UI or behavior

---

## Core Knowledge

### Create or Initialize a Dueuno Application

- [ ] Verify that the following dependency is present in `build.gradle`: `implementation "org.apache.grails:grails-core"`. If it is missing, tell the user this is not a Grails project and stop here.
- [ ] Add the following dependency to `build.gradle`: `implementation "org.dueuno:dueuno-core:3.2.0"`.
- [ ] Add the following code to initialize the application:

```groovy
class BootStrap {

    ServletContext servletContext
    ApplicationService applicationService

    def init = {

        applicationService.onInit {
            // no-op // <1>
        }

    }
}
```

- [ ] Delete the file `grails-app/controllers/**/UrlMappings.groovy`.

- [ ] Delete all files in:
  - `grails-app/assets/javascripts/`
  - `grails-app/assets/stylesheets/`
  - `grails-app/assets/images/`
  - `grails-app/views/`

- [ ] Create the following files:
  - `grails-app/assets/javascripts/application.js`
  - `grails-app/assets/stylesheets/application.css`

- [ ] Replace `grails-app/init/BootStrap.groovy` with `BootStrap.groovy` from assets.

- [ ] Replace `grails-app/conf/logback-spring.xml` with `logback-spring.xml` from assets.

- [ ] Add the following configuration to `application.yml`, replacing `${project-name}` with the project name:
```yml
---
server:
  tomcat:
    basedir: ${project-name}/tomcat
  servlet:
    session:
      persistent: true
      store-dir: ${project-name}
  compression:
    enabled: true
    mime-types: text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json

---
javamelody:
  init-parameters:
    log: true
    storage-directory: ${user.dir}/${project-name}/java-melody

---
grails:
  plugin:
    springsecurity:
      debug:
        useFilter: true
  controllers:
    upload:
      maxFileSize: 20000000
      maxRequestSize: 20000000
```

- [ ] Set the H2 database URL to `jdbc:h2:./project-name/project-name;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE`.

- [ ] Set the `dbCreate` property to `update` for the development environment.

- [ ] Add `/project-name/` to the root `.gitignore`.

- [ ] Copy the `messages.properties` file from the assets to `grails-app/i18n/messages.properties`.

## Create a CRUD

- [ ] If the project is not a Grails project, stop here.
- [ ] If the project is not a Dueuno project because it does not depend on `org.dueuno:dueuno-core`, create a Dueuno project first, then proceed.

To create a CRUD, create the following:

- [ ] Create a domain class.
- [ ] Create a service.
- [ ] Create a controller.

IMPORTANT: Verify with the Compliance Checklist and correct if needed

## Create a Domain Class

Dueuno domain classes have the following characteristics.

- [ ] Are located in `grails-app/domain/`
- [ ] Are prefixed with the letter `T` (for example, `TCompany`).
- [ ] Use the template found in `assets` to create a new domain class.
- [ ] Have a corresponding service class in `grails-app/services/`. If it does not exist, create one for the domain class.
- [ ] Implement the following classes and fields:

```groovy
import java.time.LocalDateTime

@GrailsCompileStatic
class TCompany implements GormEntity, MultiTenant<TCompany> {

  Long id
  LocalDateTime dateCreated
  LocalDateTime lastUpdated

}
```

- [ ] Register a pretty printer for the class in the `applicationService.onInit` method.

IMPORTANT: Verify with the Compliance Checklist and correct if needed


## Create a Service

Write services using a strongly typed approach to improve code clarity and self-documentation. A service is, in every respect, part of the application’s internal API.

- [ ] Ask the user for the domain class name if it is not specified in the prompt.
- [ ] Use the template found in `assets` to create a new service.
- [ ] The service name is not prefixed with `T`.
- [ ] Implement the filters for each field in the specified domain class.
- [ ] Define `fetch` properties for each relationship in the domain class.

IMPORTANT: Verify with the Compliance Checklist and correct if needed


## Create a Controller

Controllers are written using a scripting style. Being the highest layer of the application, they focus on readability and expressive, concise logic. Use `def` to declare variables and methods.

- [ ] To create a controller, a domain class and a service must exist in the project. If they do not exist, ask the user to create them.
- [ ] Use the template found in `assets` to create a new controller.
- [ ] The controller name is not prefixed with `T`.
- [ ] Implement the related feature in `BootStrap.groovy`. For the first controller of the application, set the `favourite` parameter to `true`.
- [ ] Dueuno provides the following component types: `Content`, `Component`, `Controller`.
- [ ] Implement the controller using the available Dueuno components.
- [ ] Update the `messages.properties` file according to the new controller.

IMPORTANT: Verify with the Compliance Checklist and correct if needed

### Contents

See the reference documentation for the available contents.

IMPORTANT: Verify with the Compliance Checklist and correct if needed


### Components

See the reference documentation for the available components.

IMPORTANT: Verify with the Compliance Checklist and correct if needed


### Controls

See the reference documentation for the available controls.

IMPORTANT: Verify with the Compliance Checklist and correct if needed


### Actions

See the reference documentation for the available actions.

IMPORTANT: Verify with the Compliance Checklist and correct if needed


### Features

See the reference documentation for the available features.

IMPORTANT: Verify with the Compliance Checklist and correct if needed

---

## Code Style

- Prefer the `for` construct whenever possible.
- Always use `return` in methods, except in controllers.

## Compliance Checklist

Verify each of the following steps before proceeding.

- [ ] Implements `ElementsController`
- [ ] Uses `@Slf4j` and `@Secured`
- [ ] Services are injected with explicit types.
- [ ] Uses `createContent(...)`.
- [ ] Defines all required actions.
- [ ] Uses `display` for responses.
- [ ] Defines forms in `buildForm`.
- [ ] Keeps business logic out of controllers.
- [ ] Builds UI only with Dueuno APIs.

IMPORTANT: The `applicationService.onInit()` method MUST be called after the `onInstall`, `onTenantInstall`, and `onDevInstall` closures; otherwise installation will not run.

### Forbidden Deviations

Generated code MUST NOT:

- Rename `c`, `obj`, or `params`.
- Inline form definitions.
- Skip `with {}` blocks.
- Introduce business logic.
- Bypass `display`.

## Forbidden Practices

- Business logic inside controllers.
- Direct persistence access.
- Bypassing `createContent`.
- Building UI outside Dueuno APIs.
- Defining forms outside `buildForm`.
- Heavy logic inside `eachRow`.
