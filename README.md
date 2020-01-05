# Ressor

![GitHub](https://img.shields.io/github/license/dmart28/ressor) [![Build Status](https://travis-ci.org/dmart28/ressor.svg?branch=develop)](https://travis-ci.org/dmart28/ressor) [![Join the chat at https://gitter.im/dmart28/ressor](https://badges.gitter.im/dmart28/ressor.svg)](https://gitter.im/dmart28/ressor?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Maven Central](https://img.shields.io/maven-central/v/xyz.ressor/ressor-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22xyz.ressor%22%20AND%20a:%22ressor-core%22)

Ressor is a framework which ease the development of resource-based Java services. It translates your static or dynamic resources (files, http endpoints, git repositories, etc) into a complete Java service instance, implicitly reloading it when the source data is changing.

See the <a href="https://docs.ressor.xyz/basic-concepts" target="_blank">Basic Concepts</a> page for the more details.

## How it works?

Ressor generates a special proxy class at runtime, which inherits the class/interface of your service. It delegates the calls to an actual instance of your service, which is created and stored inside this proxy, filled with the resource data.

As a result, you generate a single instance once and use it everywhere for the whole application lifetime. When the source data changes, it reloads the service and just swap to the new instance under the hood.

## Project Status & Contributing

Ressor is currently at its early stages, so the feature set can be not complete and some bugs occur.

Feel free to participate in Ressor development in any form via contributions, issues, feature requests, etc.

## Quick Example

Let's suppose you have a service which provides book titles by the ISBN:

```java
public class BookRepository {
  private final Map<String, String> data = new HashMap<>();

  public BookRepository(List<Book> node) {
      node.forEach(b -> data.put(b.getIsbn(), b.getTitle()));
  }

  public String getTitle(String isbn) {
    return data.get(isbn);
  }
}
```

Also, there is a `/etc/books.json` file with all the data. Now we can simply tell Ressor to create a service instance, based on that file and use JSON format:

```java
var ressor = Ressor.create();
var bookService = ressor.service(BookRepository.class)
        .fileSource("/etc/books.json")
        .translator(jsonList(Book.class))
        .build();
```

Now we can just use it:

```java
var title = bookService.getTitle("0679760806"); // The Master and Margarita
```

What will happen if the `books.json` file will be changed? In case of file on local File System you can just subscribe for the changes:

```java
ressor.listen(bookService);
```

That's all, you can continue using `bookService` instance, which will be always up-to-date with the `books.json` file contents.

## Documentation

See our <a href="https://ressor.xyz" target="_blank">Project Website</a>.

Javadoc is <a href="https://javadoc.ressor.xyz" target="_blank">here</a>.

## Import

Releases are available via Maven Central.

### Java 11+

```
  implementation 'xyz.ressor:ressor-core:1.3.0'
  implementation 'xyz.ressor:ressor-git-source:1.3.0'
  implementation 'xyz.ressor:ressor-http-source:1.3.0'
  implementation 'xyz.ressor:ressor-s3-source:1.3.0'
```

### Java 8

```
  implementation 'xyz.ressor:ressor-core-jdk8:1.3.0'
  implementation 'xyz.ressor:ressor-git-source-jdk8:1.3.0'
  implementation 'xyz.ressor:ressor-http-source-jdk8:1.3.0'
  implementation 'xyz.ressor:ressor-s3-source-jdk8:1.3.0'
```

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [releases on this repository](https://github.com/dmart28/ressor/releases).

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](https://github.com/dmart28/ressor/blob/master/LICENSE) file for details.
