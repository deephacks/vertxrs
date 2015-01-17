# vertxrs
[![Build Status](https://travis-ci.org/deephacks/vertxrs.svg?branch=master)](https://travis-ci.org/deephacks/vertxrs)

The combination of JAX-RS and Vert.x comes with a few advantages over conventional Servlet containers.

* Everybody knows JAX-RS.
* Fast and asynchronous by nature.
* Very few dependencies and only a few megabytes in size.
* Unnoticeable application startup time.
* Static file serving support for web resources.
* SockJs and Websocket support.
* Java 8 and reactive programming friendly.
* Simple API for writing extremely fast zero-copy TCP clients and servers.
* Clustered, language neutral event bus for IPC and server communication.

### Example

Maven dependency.

```xml
<dependency>
  <groupId>org.deephacks.vertxrs</groupId>
  <artifactId>vertxrs</artifactId>
  <version>${version}</version>
</dependency>
```

Register a JAX-RS resource and start the server.

```java

ResteasyDeployment resteasy = new ResteasyDeployment();
resteasy.setApplication(new Application() {
  @Override
  public Set<Object> getSingletons() {
    HashSet<Object> singletons = new HashSet<>();
    singletons.add(new Endpoint());
    return singletons;
  }
});

Services services = Services.newBuilder()
        .withResteasy(resteasy)
        .build();
new VertxRsServer(Config.defaultConfig(), services).start();
```
