package org.deephacks.vertxrs;

import com.squareup.okhttp.*;

import java.io.IOException;

public class BaseTest {
  static VertxRsServer vertxrs;
  static MediaType APPLICATION_JSON = MediaType.parse("application/json");
  static OkHttpClient client;
  static Config config;
  static {
    if (vertxrs == null) {
      client = new OkHttpClient();
      Services services = Services.newBuilder()
              .withResteasy(new Resteasy().getDeployment())
              .withSockJsService("test", new TestResource())
              .build();
      config = Config.defaultConfig();
      vertxrs = new VertxRsServer(config, services);
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
