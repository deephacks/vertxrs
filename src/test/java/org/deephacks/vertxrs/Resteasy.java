package org.deephacks.vertxrs;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class Resteasy extends Application {
  private final Set<Object> services = new HashSet<>();
  private Dispatcher dispatcher;
  private ResteasyDeployment deployment = new ResteasyDeployment();

  public Resteasy(Object... services) {
    this.services.add(new TestResource());
    this.services.add(new AsyncJaxrsResource());
    this.services.add(new Resource());
    for (Object service : services) {
      this.services.add(service);
    }
  }

  @Override
  public Set<Object> getSingletons() {
    return services;
  }

  public ResteasyDeployment getDeployment() {
    deployment.setApplication(this);
    return deployment;
  }
}
