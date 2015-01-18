package org.deephacks.vertxrs;

import com.squareup.okhttp.*;
import org.jboss.resteasy.plugins.providers.jackson.Jackson2JsonpInterceptor;

import java.io.IOException;

public class BaseTest {
  static VertxRsServer vertxrs;
  static MediaType APPLICATION_JSON = MediaType.parse("application/json");
  static OkHttpClient client;
  static Config config = Config.defaultConfig();
  static {
    if (vertxrs == null) {
      client = new OkHttpClient();
      vertxrs = VertxRsServer.newBuilder()
              .withResource(new TestResource())
              .withResource(new AsyncJaxrsResource())
              .withResource(new Resource())
              .withResource(new ExceptionMapperTest.ExceptionMapperResource())
              .withResource(new JsonpTest.JsonpResource())
              .withResource(new JaxrsStressTest.AsyncJaxrsStressResource())
              .withResource(new TypesafeJaxrsTest.TypesafeResource())
              .withProvider(new ExceptionMapperTest.ExceptionMapper())
              .withProvider(new Jackson2JsonpInterceptor())
              .withSockJs("test", new TestResource())
              .build();
      vertxrs.start();
    }
  }

  static Response PUT(String path, String body) throws IOException {
    Request req = new Request.Builder()
            .put(RequestBody.create(APPLICATION_JSON, body))
            .url(config.getRestHttpHostPortUrl(path)).build();
    return client.newCall(req).execute();
  }

  static Response GET(String path) throws IOException {
    Request req = new Request.Builder().get().url(config.getRestHttpHostPortUrl(path)).build();
    return client.newCall(req).execute();
  }

  static Response GET(String path, String header, String value) throws IOException {
    Request req = new Request.Builder().get()
            .url(config.getRestHttpHostPortUrl(path))
            .header(header, value)
            .build();
    return client.newCall(req).execute();
  }


  static Response POST(String path, String body) throws IOException {
    Request req = new Request.Builder()
            .post(RequestBody.create(APPLICATION_JSON, body))
            .url(config.getRestHttpHostPortUrl(path))
            .build();
    return client.newCall(req).execute();
  }
}
