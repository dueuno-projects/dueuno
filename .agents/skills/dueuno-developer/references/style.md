# Coding Style

Write code that is easy to read and understand for developers that are new to the project or have limited experience. Prefer clarity to cleverness, use descriptive names, and keep the implementation as simple as possible.


## Groovy

- Do not split the argument list across multiple lines in method definitions.
- Always use body brackets in `if` statements even if there is only one operation.
- Leave a blank line after an `if` statement, except when inlining multiple semantically similar statements.
- Inline `if` statements only when you have multiple semantically similar statements.
- Add a blank line before the `} else {` or `} else if () {` blocks when the body is made of multiple lines.
- Prefer the `for` construct whenever possible.
- Always use `return` in methods returning a value. Leave a blank line before `return` when the method body is longer than a couple of lines or when you think it's more visible this way. You can avoid blank lines if the previous statement is already isolated and resolves in the `return` statement.
- Prefer object types instead of primitive types whenever possible (eg: Boolean, Integer, Long, etc)
- Prefer multiple lines of code with variable assignments and meaningful variable names instead of inlining multiple operations in a single line. Do not split lines for trivial operations.
- Add a blank line when calling methods with many arguments formatted vertically (eg: each line one argument).


## Grails

- Leave a blank line before the `display` method call when the method body is longer than a couple of lines or when you think it's more visible this way. You can avoid blank lines if the previous statement is already isolated and resolves in the `display` statement or if the `display` statement is within an if/else body.
- Within a Table configuration, always write multiple lines when declaring table `columns`, `keys`, `sortable`, `labels` and any time you need to declare a List, Map or List<Map>.
- Within a Form configuration, always write multiple lines when calling methods with more than one argument. Do not add blank lines between properties assignments or method calls.

