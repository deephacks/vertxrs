# vertxrs
[![Build Status](https://travis-ci.org/deephacks/vertxrs.svg?branch=master)](https://travis-ci.org/deephacks/vertxrs)

Building JAX-RS applications with Vert.x comes with a few advantages. 

* Everybody knows JAX-RS.
* Fast and asynchronous by nature.
* Very few dependencies and only a few megabytes in size.
* Unnoticeable application startup time.
* SockJs and Websocket support.
* Java 8 and RxJava friendly.
* Static file serving support.
* Support for writing extremely fast binary TCP clients and servers.
* Clustered event bus.

### Example


```java

ResteasyDeployment resteasy = new ResteasyDeployment();
resteasy.setApplication(new Application() {
  @Override
  public Set<Object> getSingletons() {
    HashSet<Object> singletons = new HashSet<Object>();
    singletons.add(new Endpoint());
    return singletons;
  }
});

Services services = Services.newBuilder()
        .withResteasy(resteasy)
        .build();
VertxRsServer server = new VertxRsServer(Config.defaultConfig(), services);
server.start();

```
