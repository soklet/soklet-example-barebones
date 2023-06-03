## Soklet Example (Barebones)

Here we demonstrate building and running a single-file Soklet application with nothing but the Soklet JAR and the JDK (or Docker).

While a real production system will have more moving parts, it's important to show that you _can_ build server software without ceremony or dependencies - this is the Soklet ethos.

[A more fully-featured example is also available](https://github.com/soklet/soklet-example-full).

Two ways to build and run are shown: 

* [Directly from the command-line](#building-and-running-without-docker)
* [Docker container](#building-and-running-with-docker)

### Source Code

The entire application is contained in [src/com/soklet/example/App.java](src/com/soklet/example/App.java), which is reproduced below.

```java
public class App {
  @Resource
  public static class ExampleResource {
    @GET("/")
    public String index() {
      return "Hello, world!";
    }

    @GET("/test-input")
    public Response testInput(@QueryParameter Integer input) {
      return new Response.Builder()
        .headers(Map.of("Content-Type", Set.of("application/json; charset=UTF-8")))
        // A real application would not construct JSON in this manner
        .body(String.format("{\"input\": %d}", input))
        .build();
    }
  }

  public static void main(String[] args) throws Exception {
    int port = 8080;
    Server server = new MicrohttpServer.Builder(port).build();
    SokletConfiguration sokletConfiguration = new SokletConfiguration.Builder(server).build();

    // In an interactive console environment, stop on `Return` keypress.
    // In a Docker container, join on the current thread (normally no stdin)
    boolean stopOnKeypress = !"true".equals(System.getenv("RUNNING_IN_DOCKER"));

    try (Soklet soklet = new Soklet(sokletConfiguration)) {
      soklet.start();

      System.out.printf("Soklet Example App started on port %d (%s virtual threads).\n",
        port, Utilities.virtualThreadsAvailable() ? "with" : "without");

      if (stopOnKeypress) {
        System.out.println("Press [enter] to exit");
        System.in.read();
      } else {
        Thread.currentThread().join();
      }
    }
  }
}
```

### Building and Running Without Docker

Requires JDK 16+ to be installed on your machine.  If you need one, Amazon provides [Corretto](https://aws.amazon.com/corretto/) - a free-to-use, production-ready distribution of [OpenJDK](https://openjdk.org/) that includes LTS.

#### Build

```console
javac -parameters -cp soklet-2.0.0-SNAPSHOT.jar -d build src/com/soklet/example/App.java 
```

#### Run

```console
java --enable-preview -cp soklet-2.0.0-SNAPSHOT.jar:build com/soklet/example/App
```

### Building and Running With Docker

Requires [Docker](https://www.docker.com/products/docker-desktop/) to be installed on your machine.

#### Build

```console
docker build . --file Dockerfile --tag soklet/example-barebones
```

#### Run

```console
docker run -p 8080:8080 soklet/example-barebones
```

### Testing

#### Happy Path

##### Request

```console
curl  "http://localhost:8080/"
```

##### Response

```text
Hello, world
```

#### Query Parameters

##### Request

```console
curl --verbose "http://localhost:8080/test-input?input=123"
```

##### Response

```text
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /test-input?input=123 HTTP/1.1
...
< HTTP/1.1 200 OK
< Content-Length: 14
< Content-Type: application/json; charset=UTF-8
< 
* Connection #0 to host localhost left intact
{"input": 123}
```

#### Bad Input

##### Request

```console
curl --verbose "http://localhost:8080/test-input?input=abc"
```

##### Response

```text
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /test-input?input=abc HTTP/1.1
...
< HTTP/1.1 400 Bad Request
< Content-Length: 21
< Content-Type: text/plain; charset=UTF-8
< 
* Connection #0 to host localhost left intact
HTTP 400: Bad Request
```