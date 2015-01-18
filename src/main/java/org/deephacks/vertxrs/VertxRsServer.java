/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.vertxrs;

import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;

import javax.ws.rs.core.Application;
import java.io.File;
import java.util.*;

public class VertxRsServer {
  private final Vertx vertx;
  private final List<HttpServer> httpServers = new ArrayList<>();
  private final Config config;
  private final Map<String, Handler<Message>> sockJsServices;
  private final ResteasyDeployment resteasy;
  private SockJSServer sockJSServer;

  private VertxRsServer(Builder builder) {
    this.config = Optional.ofNullable(builder.config).orElse(Config.defaultConfig());
    this.resteasy = builder.resteasy;
    this.resteasy.setProviderFactory(builder.factory);
    this.resteasy.setApplication(new Application() {
      @Override
      public Set<Object> getSingletons() {
        return builder.resources;
      }
    });
    this.vertx = VertxFactory.newVertx();
    this.sockJsServices = builder.sockJsServices;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public void start() {
    for (int i = 0; i < config.getEventLoops(); i++) {
      HttpServer httpServer = vertx.createHttpServer();
      httpServers.add(httpServer);
      httpServer.requestHandler(handleBody());
      if (!sockJsServices.isEmpty()) {
        JsonObject path = new JsonObject().putString("prefix", config.getSockJsPath());
        JsonArray permitted = new JsonArray();
        // Let everything through
        permitted.add(new JsonObject());
        sockJSServer = vertx.createSockJSServer(httpServer).bridge(path, permitted, permitted);
        sockJsServices.forEach((address, handler) -> vertx.eventBus()
                        .registerHandler(address, handler));
      }
      httpServer.listen(config.getHttpPort(), config.getHttpHost());
    }
    ShutdownHook.install(new Thread(() -> VertxRsServer.this.stop()));
  }

  public void stop() {
    resteasy.stop();
    if (sockJSServer != null) {
      sockJSServer.close();
    }
    httpServers.forEach(HttpServer::close);
    vertx.stop();
  }

  public Vertx getVertx() {
    return vertx;
  }

  public Config getConfig() {
    return config;
  }

  private Handler<HttpServerRequest> handleBody() {
    resteasy.start();
    SynchronousDispatcher dispatcher = (SynchronousDispatcher) resteasy.getDispatcher();
    return httpRequest -> {
      if (httpRequest.path().startsWith(config.getJaxrsPath())) {
        httpRequest.bodyHandler(new VertxResteasyHandler(httpRequest, dispatcher));
      } else {
        File file = new File(config.getStaticRootPath(), httpRequest.path());
        httpRequest.response().sendFile(file.getAbsolutePath());
      }
    };
  }

  public static class Builder {
    private Map<String, Handler<Message>> sockJsServices = new HashMap<>();
    private ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
    private ResteasyDeployment resteasy = new ResteasyDeployment();
    private Set<Object> resources = new HashSet<>();
    private Config config;

    private Builder() {}

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

    public Builder withConfig(Config config) {
      this.config = config;
      return this;
    }

    public VertxRsServer build() {
      return new VertxRsServer(this);
    }
  }

}
