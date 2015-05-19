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
import io.netty.buffer.ByteBufInputStream;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.BaseHttpRequest;
import org.jboss.resteasy.specimpl.ResteasyHttpHeaders;
import org.jboss.resteasy.spi.*;
import org.jboss.resteasy.util.CookieParser;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.util.*;

class VertxHttpRequest extends BaseHttpRequest {
    private HttpServerRequest request;
    protected Map<String, Object> attributes = new HashMap<>();
    private VertxExecutionContext executionContext;
    private InputStream body;

    public VertxHttpRequest(Buffer body,
                            HttpServerRequest request,
                            VertxHttpResponse response,
                            SynchronousDispatcher dispatcher) {
      super(new ResteasyUriInfo(request.absoluteURI()));
      this.request = request;
      this.executionContext = new VertxExecutionContext(this, response, dispatcher);
      this.body = new ByteBufInputStream(body.getByteBuf());
    }

    public VertxExecutionContext getExecutionContext() {
      return executionContext;
    }

    @Override
    public HttpHeaders getHttpHeaders() {
      MultivaluedMap map = new MultivaluedHashMap<>();
      request.headers().forEach(e -> map.add(e.getKey(), e.getValue()));
      HashMap<String, Cookie> cookieHashMap = new HashMap<>();
      Object cookie = map.getFirst("Cookie");
      if (cookie != null) {
        CookieParser.parseCookies(String.valueOf(cookie))
          .forEach(c -> cookieHashMap.put(c.getName(), c));
      }
      return new ResteasyHttpHeaders(map, cookieHashMap);
    }

    @Override
    public MultivaluedMap<String, String> getMutableHeaders() {
      return null;
    }

    @Override
    public InputStream getInputStream() {
      return body;
    }

    @Override
    public void setInputStream(InputStream stream) {

    }

    @Override
    public String getHttpMethod() {
      return request.method();
    }

    @Override
    public void setHttpMethod(String method) {
    }

    @Override
    public Object getAttribute(String attribute) {
      return attributes.get(attribute);
    }

    @Override
    public void setAttribute(String name, Object value) {
      attributes.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {
      attributes.remove(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
      Enumeration<String> en = new Enumeration<String>() {
        private Iterator<String> it = attributes.keySet().iterator();

        @Override
        public boolean hasMoreElements() {
          return it.hasNext();
        }

        @Override
        public String nextElement() {
          return it.next();
        }
      };
      return en;
    }

    @Override
    public ResteasyAsynchronousContext getAsyncContext() {
      return executionContext;
    }

    @Override
    public void forward(String path) {
      throw new NotImplementedYetException();
    }

    @Override
    public boolean wasForwarded() {
      return false;
    }
  }
