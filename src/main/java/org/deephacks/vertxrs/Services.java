package org.deephacks.vertxrs;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;

import javax.ws.rs.core.Application;
import java.util.*;

public class Services {
  private final Map<String, Handler<Message>> sockJsServices = new HashMap<>();
  private ResteasyDeployment deployment;

  private Services(Builder builder) {
    this.sockJsServices.putAll(builder.sockJsServices);
    this.deployment = builder.resteasy;
    this.deployment.setProviderFactory(builder.factory);
    this.deployment.setApplication(new Application() {
      @Override
      public Set<Object> getSingletons() {
        return builder.resources;
      }
    });
  }

  public static Services empty() {
    return new Services(new Builder());
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public Map<String, Handler<Message>> getSockJsServices() {
    return sockJsServices;
  }

  SynchronousDispatcher startJaxrs() {
    deployment.start();
    return (SynchronousDispatcher) deployment.getDispatcher();
  }

  public static class Builder {
    private Map<String, Handler<Message>> sockJsServices = new HashMap<>();
    private ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
    private ResteasyDeployment resteasy = new ResteasyDeployment();
    private Set<Object> resources = new HashSet<>();

    public Builder withResource(Object resource) {
      resources.add(resource);
      return this;
    }

    public Builder withProvider(Object provider) {
      factory.registerProviderInstance(provider);
      return this;
    }

    public Builder withSockJs(Map<String, Handler<Message>> services) {
      sockJsServices = Optional.ofNullable(services).orElse(new HashMap<>());
      return this;
    }

    public Builder withSockJs(String address, Handler<Message> handler) {
      sockJsServices.put(address, handler);
      return this;
    }

    public Services build() {
      return new Services(this);
    }
  }

}
