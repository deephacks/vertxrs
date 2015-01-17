package org.deephacks.vertxrs;

import com.squareup.okhttp.Response;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.Test;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JsonpTest extends BaseTest {

  @Test
  public void testJson() throws IOException {
    Response response = GET("/json?callback=json&value=json123",
            HttpHeaders.Names.CONTENT_TYPE,
            APPLICATION_JSON.toString());
    assertThat(response.code(), is(200));
    assertThat(response.body().string(), is("json(json123)"));
  }

  @Test
  public void testJavascript() throws IOException {
    Response response = GET("/javascript?callback=javascript&value=js123",
            HttpHeaders.Names.CONTENT_TYPE,
            "application/javascript");
    assertThat(response.code(), is(200));
    assertThat(response.body().string(), is("javascript(js123)"));
  }

  @Path("/rest")
  public static class JsonpResource {

    @GET @Path("json") @Produces(MediaType.APPLICATION_JSON)
    public String json(@QueryParam("value") String value) {
      return value;
    }

    @GET @Path("javascript") @Produces("application/javascript")
    public String javascript(@QueryParam("value") String value) {
      return value;
    }
  }
}