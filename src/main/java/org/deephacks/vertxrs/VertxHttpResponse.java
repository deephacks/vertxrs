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

import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.jboss.resteasy.spi.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.*;


class VertxHttpResponse implements HttpResponse {
  private Buffer buffer;
  private OutputStream outputStream;
  private ByteBufOutputStream underlyingOutputStream;
  private HttpServerRequest request;
  private boolean committed;
  private MultivaluedMap<String, Object> outputHeaders;
  private boolean keepAlive;
  private HttpServerResponse response;
  private ResteasyProviderFactory providerFactory;

  public VertxHttpResponse(HttpServerRequest request, ResteasyProviderFactory providerFactory) {
    this.response = request.response();
    this.providerFactory = providerFactory;
    this.underlyingOutputStream = new ByteBufOutputStream(Unpooled.buffer());
    this.outputStream = underlyingOutputStream;
    this.buffer = Buffer.buffer(underlyingOutputStream.buffer());
    this.request = request;
    this.outputHeaders = new MultivaluedMapImpl<>();
    this.keepAlive = isKeepAlive(request);
  }

  HttpServerResponse getResponse() {
    return response;
  }

  private boolean isKeepAlive(HttpServerRequest request) {
    String connection = request.headers().get("Connection");
    if ("close".equalsIgnoreCase(connection)) {
      return false;
    } else if ("keep-alive".equalsIgnoreCase(connection)) {
      return true;
    }
    return false;
  }

  public boolean isKeepAlive() {
    return keepAlive;
  }

  public Buffer getBuffer() {
    return buffer;
  }

  @Override
  public int getStatus() {
    return response.getStatusCode();
  }

  @Override
  public void setStatus(int status) {
    response.setStatusCode(status);
  }

  @Override
  public MultivaluedMap<String, Object> getOutputHeaders() {
    return outputHeaders;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return outputStream;
  }

  @Override
  public void setOutputStream(OutputStream os) {
    this.outputStream = os;
  }

  @Override
  public void addNewCookie(NewCookie cookie) {
    outputHeaders.add(HttpHeaders.SET_COOKIE, cookie);
  }

  @Override
  public void sendError(int status) throws IOException {
    sendError(status, null);
  }

  @Override
  public void sendError(int status, String message) throws IOException {
    if (committed) {
      throw new IllegalStateException();
    }
    HttpResponseStatus responseStatus;
    if (message != null) {
      responseStatus = new HttpResponseStatus(status, message);
    } else {
      responseStatus = HttpResponseStatus.valueOf(status);
    }
    response.setStatusCode(responseStatus.code());
    if (keepAlive) {
      // Add keep alive and content length if needed
      response.putHeader(Names.CONNECTION, Values.KEEP_ALIVE);
      response.putHeader(Names.CONTENT_LENGTH, String.valueOf(0));
    }
    response.end();
    committed = true;
  }

  @Override
  public boolean isCommitted() {
    return committed;
  }

  @Override
  public void reset() {
    if (committed) {
      throw new IllegalStateException("Already committed");
    }
    outputHeaders.clear();
    underlyingOutputStream.buffer().clear();
    outputHeaders.clear();
  }

  public void finish() throws IOException {
    transformHeaders();
    request.response().end(buffer);
  }

  void transformHeaders() {
    HttpServerResponse response = this.getResponse();
    if (this.isKeepAlive()) {
      response.headers().set(Names.CONNECTION, Values.KEEP_ALIVE);
    } else {
      response.headers().set(Names.CONNECTION, Values.CLOSE);
    }

    for (Map.Entry<String, List<Object>> entry : this.getOutputHeaders().entrySet()) {
      String key = entry.getKey();
      for (Object value : entry.getValue()) {
        RuntimeDelegate.HeaderDelegate delegate = providerFactory.getHeaderDelegate(value.getClass());
        if (delegate != null) {
          response.headers().add(key, delegate.toString(value));
        } else {
          response.headers().set(key, value.toString());
        }
      }
    }
  }
}
