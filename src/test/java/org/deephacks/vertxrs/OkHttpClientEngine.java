package org.deephacks.vertxrs;/*
 * Copyright 2015 Thomas Broyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MultivaluedMap;

import okio.Buffer;
import okio.BufferedSink;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;
import org.jboss.resteasy.client.jaxrs.internal.ClientResponse;
import org.jboss.resteasy.util.CaseInsensitiveMap;

/**
 * @author Thomas Broyer <t.broyer@ltgt.net>
 */
public class OkHttpClientEngine implements ClientHttpEngine {

  private final OkHttpClient client;

  private SSLContext sslContext;

  public OkHttpClientEngine(OkHttpClient client) {
    this.client = client;
  }

  @Override
  public SSLContext getSslContext() {
    return sslContext;
  }

  public void setSslContext(SSLContext sslContext) {
    this.sslContext = sslContext;
  }

  @Override
  public HostnameVerifier getHostnameVerifier() {
    return client.getHostnameVerifier();
  }

  @Override
  public ClientResponse invoke(ClientInvocation request) {
    Request req = createRequest(request);
    Response response;
    try {
      response = client.newCall(req).execute();
    } catch (IOException e) {
      throw new ProcessingException("Unable to invoke request", e);
    }
    return createResponse(request, response);
  }

  private Request createRequest(ClientInvocation request) {
    Request.Builder builder = new Request.Builder()
      .method(request.getMethod(), createRequestBody(request))
      .url(request.getUri().toString());
    for (Map.Entry<String, List<String>> header : request.getHeaders().asMap().entrySet()) {
      String headerName = header.getKey();
      for (String headerValue : header.getValue()) {
        builder.addHeader(headerName, headerValue);
      }
    }
    return builder.build();
  }

  private RequestBody createRequestBody(final ClientInvocation request) {
    if (request.getEntity() == null) {
      return null;
    }

    // NOTE: this will invoke WriterInterceptors which can possibly change the request,
    // so it must be done first, before reading any header.
    final Buffer buffer = new Buffer();
    try {
      request.writeRequestBody(buffer.outputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    javax.ws.rs.core.MediaType mediaType = request.getHeaders().getMediaType();
    final MediaType contentType = (mediaType == null) ? null : MediaType.parse(mediaType.toString());

    return new RequestBody() {
      @Override
      public long contentLength() throws IOException {
        return buffer.size();
      }

      @Override
      public MediaType contentType() {
        return contentType;
      }

      @Override
      public void writeTo(BufferedSink sink) throws IOException {
        sink.write(buffer, buffer.size());
      }
    };
  }

  private ClientResponse createResponse(ClientInvocation request, final Response response) {
    ClientResponse clientResponse = new ClientResponse(request.getClientConfiguration()) {
      private InputStream stream;

      @Override
      protected InputStream getInputStream() {
        if (stream == null) {
          stream = response.body().byteStream();
        }
        return stream;
      }

      @Override
      protected void setInputStream(InputStream is) {
        stream = is;
      }

      @Override
      protected void releaseConnection() throws IOException {
        // Stream might have been entirely replaced, so we need to close it independently from response.body()
        Throwable primaryExc = null;
        try {
          if (stream != null) {
            stream.close();
          }
        } catch (Throwable t) {
          primaryExc = t;
          throw t;
        } finally {
          if (primaryExc != null) {
            try {
              response.body().close();
            } catch (Throwable suppressedExc) {
              primaryExc.addSuppressed(suppressedExc);
            }
          } else {
            response.body().close();
          }
        }
      }
    };

    clientResponse.setStatus(response.code());
    clientResponse.setHeaders(transformHeaders(response.headers()));

    return clientResponse;
  }

  private MultivaluedMap<String, String> transformHeaders(Headers headers) {
    MultivaluedMap<String, String> ret = new CaseInsensitiveMap<>();
    for (int i = 0, l = headers.size(); i < l; i++) {
      ret.add(headers.name(i), headers.value(i));
    }
    return ret;
  }

  @Override
  public void close() {
    // no-op
  }
}