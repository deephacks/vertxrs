package org.deephacks.vertxrs;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;

import java.util.*;

public class Services {
  private final Map<String, Handler<Message>> sockJsServices = new HashMap<>();
  private final ResteasyDeployment resteasy;

  private Services(Builder builder) {
    this.sockJsServices.putAll(builder.sockJsServices);
    this.resteasy = Objects.requireNonNull(builder.resteasy,
            "ResteasyDeployment must not be null");
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

  SynchronousDispatcher startResteasy() {
    resteasy.start();
    return (SynchronousDispatcher) resteasy.getDispatcher();
  }

  public static class Builder {
    private Map<String, Handler<Message>> sockJsServices = new HashMap<>();
    private ResteasyDeployment resteasy;

    public Builder withResteasy(ResteasyDeployment resteasy) {
      this.resteasy = resteasy;
      return this;
    }

    public Builder withSockJsServices(Map<String, Handler<Message>> services) {
      sockJsServices = Optional.ofNullable(services).orElse(new HashMap<>());
      return this;
    }

    public Builder withSockJsService(String address, Handler<Message> handler) {
      sockJsServices.put(address, handler);
      return this;
    }

    public Services build() {
      return new Services(this);
    }
  }

}
