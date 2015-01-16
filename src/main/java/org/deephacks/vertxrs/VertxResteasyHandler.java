package org.deephacks.vertxrs;


import org.jboss.resteasy.core.SynchronousDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

public class VertxResteasyHandler implements Handler<Buffer> {
  private Logger logger = LoggerFactory.getLogger(VertxResteasyHandler.class);
  private HttpServerRequest httpRequest;
  private SynchronousDispatcher dispatcher;

  public VertxResteasyHandler(HttpServerRequest httpRequest, SynchronousDispatcher dispatcher) {
    this.httpRequest = httpRequest;
    this.dispatcher = dispatcher;
  }

  @Override
  public void handle(Buffer body) {
    try {
      VertxHttpResponse response = new VertxHttpResponse(httpRequest, dispatcher.getProviderFactory());
      VertxHttpRequest request = new VertxHttpRequest(body, httpRequest, response, dispatcher);
      dispatcher.invoke(request, response);
      if (!request.getExecutionContext().isSuspended()) {
        response.transformHeaders();
        httpRequest.response().end(response.getBuffer());
      }
    } catch (Exception e) {
      logger.warn("Unhandled exception {} {}", e.getClass().getName(), e.getMessage());
      logger.debug("{}", e);
      httpRequest.response().headers().clear();
      httpRequest.response().setStatusCode(500);
      httpRequest.response().end();
    }
  }
}
