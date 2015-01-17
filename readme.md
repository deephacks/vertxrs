# vertxrs
[![Build Status](https://travis-ci.org/deephacks/vertxrs.svg?branch=master)](https://travis-ci.org/deephacks/vertxrs)

Building JAX-RS applications with Vert.x comes with a few advantages as opposed to conventional Servlet containers.

* Everybody knows JAX-RS.
* Fast and asynchronous by nature.
* Very few dependencies and only a few megabytes in size.
* Unnoticeable application startup time.
* Static file serving support for web resources.
* SockJs and Websocket support.
* Java 8 and reactive programming friendly.
* Simple API for writing extremely fast zero-copy TCP clients and servers.
* Clustered event bus.

### Example


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
