# vertxrs
[![Build Status](https://travis-ci.org/deephacks/vertxrs.svg?branch=master)](https://travis-ci.org/deephacks/vertxrs)

JAX-RS with Vert.x


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
