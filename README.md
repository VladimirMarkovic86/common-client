# Common client

Common client project makes easier to manipulate with essential three entities (User, Role and Language). Using framework-lib it renderes insert/update forms listing entities table and making deletion of entity available.

## Getting Started

This project is ment to be used only with common-middle (where are defined URLs and available functionalities), common-server where are implemented CRUD operations for generic entities, server-lib (which fetches clients requests and passes them to routing function).

### Installing

You can use this project as dependencie in clojure projects by listing it in project.clj

```
[org.clojars.vladimirmarkovic86/common-client "0.2.0"]
```

## Authors

* **Vladimir Markovic** - [VladimirMarkovic86](https://github.com/VladimirMarkovic86)

## License

This project is licensed under the Eclipse Public License 1.0 - see the [LICENSE](LICENSE) file for details

