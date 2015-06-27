package org.deephacks.vertxrs;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import org.deephacks.vertxrs.TestResource.Data;
import org.junit.Test;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

public class JaxrsStressTest {
  static VertxRsServer vertxrs;
  static int multiplier = 5;
  static int connections = Runtime.getRuntime().availableProcessors() * multiplier;

  static {
    vertxrs = VertxRsServer.newBuilder()
            .withConfig(Config.newBuilder().withHttpPort(8081).build())
            .withResource(new AsyncJaxrsStressResource()).build();
    vertxrs.start();
  }

  @Test
  public void stressTestAsync() throws Exception {
    execute("async");
  }

  @Test
  public void stressTestSync() throws Exception {
    execute("sync");
  }

  private void execute(String path) throws Exception {
    Vertx vertx = vertxrs.getVertx();
    CountDownLatch latch = new CountDownLatch(multiplier * connections);
    for (int j = 0; j < connections; j++) {
      HttpClient client = vertx.createHttpClient(new HttpClientOptions().setDefaultPort(8081).setDefaultHost("localhost"));
      for (int i = 0; i < multiplier; i++) {
        String json = "{\"name\":\"name\", \"value\":\"value" + String.format("%07d", i) + "\"}";
        client.post("/rest/stress/" + path, event -> {
          if (event.statusCode() != 200) {
            throw new RuntimeException(String.valueOf(event.statusCode()));
          }
          latch.countDown();
        }).putHeader(CONTENT_TYPE, "application/json")
                .putHeader(CONNECTION, "Keep-Alive")
                .putHeader(CONTENT_LENGTH, String.valueOf(json.length()))
                .write(json).end();
      }
    }
    latch.await(10, TimeUnit.SECONDS);
    assertThat(latch.getCount(), is(0L));
  }
  @Path("/rest/stress")
  public static class AsyncJaxrsStressResource {

    @POST @Path("async")
    @Consumes(MediaType.APPLICATION_JSON)
    public void send(Data data, @Suspended AsyncResponse response) {
      response.resume(data);
    }

    @POST @Path("sync")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Data sync(Data data) {
      return data;
    }
  }
}
