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
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.sockjs.SockJSServer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VertxRsServer {
  private final Vertx vertx;
  private final List<HttpServer> httpServers = new ArrayList<>();
  private final Config config;
  private final Services services;
  private SockJSServer sockJSServer;

  public VertxRsServer(Config config, Services services) {
    this.config = config;
    this.vertx = VertxFactory.newVertx();
    this.services = services;
  }

  public void start() {
    for (int i = 0; i < config.getEventLoops(); i++) {
      HttpServer httpServer = vertx.createHttpServer();
      httpServers.add(httpServer);
      httpServer.requestHandler(handleBody());
      if (!services.getSockJsServices().isEmpty()) {
        JsonObject path = new JsonObject().putString("prefix", config.getSockJsPath());
        JsonArray permitted = new JsonArray();
        // Let everything through
        permitted.add(new JsonObject());
        sockJSServer = vertx.createSockJSServer(httpServer).bridge(path, permitted, permitted);
        services.getSockJsServices()
                .forEach((address, handler) -> vertx.eventBus()
                        .registerHandler(address, handler));
      }
      httpServer.listen(config.getHttpPort(), config.getHttpHost());
    }
    ShutdownHook.install(new Thread(() -> VertxRsServer.this.stop()));
  }

  public void stop() {
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
    SynchronousDispatcher dispatcher = services.startJaxrs();
    return httpRequest -> {
      if (httpRequest.path().startsWith(config.getJaxrsPath())) {
        httpRequest.bodyHandler(new VertxResteasyHandler(httpRequest, dispatcher));
      } else {
        File file = new File(config.getStaticRootPath(), httpRequest.path());
        httpRequest.response().sendFile(file.getAbsolutePath());
      }
    };
  }
}
