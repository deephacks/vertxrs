package org.deephacks.vertxrs;


import org.jboss.resteasy.core.SynchronousDispatcher;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

public class VertxResteasyHandler implements Handler<Buffer> {
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
      httpRequest.response().headers().clear();
      httpRequest.response().setStatusCode(500);
      httpRequest.response().end();
    }
  }
}
