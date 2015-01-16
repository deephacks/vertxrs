package org.deephacks.vertxrs;

import org.jboss.resteasy.plugins.providers.jackson.Jackson2JsonpInterceptor;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class Resteasy extends Application {
  private final Set<Object> services = new HashSet<>();
  private ResteasyDeployment deployment = new ResteasyDeployment();

  public Resteasy(Object... services) {
    this.services.add(new TestResource());
    this.services.add(new AsyncJaxrsResource());
    this.services.add(new Resource());
    this.services.add(new ExceptionMapperTest.ExceptionMapperResource());
    this.services.add(new JsonpTest.JsonpResource());
    this.services.add(new JaxrsStressTest.AsyncJaxrsStressResource());
    for (Object service : services) {
      this.services.add(service);
    }
    ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();

    factory.registerProviderInstance(new ExceptionMapperTest.ExceptionMapper());
    factory.registerProviderInstance(new Jackson2JsonpInterceptor());
    deployment.setProviderFactory(factory);
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
