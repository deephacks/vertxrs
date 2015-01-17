package org.deephacks.vertxrs;

import com.squareup.okhttp.*;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.*;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Inspiration taken from RestEasy
 */
public class AsyncJaxrsResourceTest extends BaseTest {

  static final int REQUEST_TIMEOUT = 1000000;

  @Test(timeout = REQUEST_TIMEOUT)
  public void testMethodFailure() throws Exception {
    Response response =  GET("/jaxrs/method-failure");
    assertEquals(Status.FORBIDDEN.getStatusCode(), response.code());
  }

  @Test(timeout = REQUEST_TIMEOUT)
  public void testAsync() throws Exception {
    Response response = GET("/jaxrs");
    assertEquals(200, response.code());
    assertEquals("hello", response.body().string());
  }
    @Test(timeout = 3 * REQUEST_TIMEOUT)
    public void testEmpty() throws Exception {
      callEmpty();
      callEmpty();
      callEmpty();
    }

    private void callEmpty() throws IOException {
      long start = System.currentTimeMillis();
      Response response = GET("/jaxrs/empty");
      long end = System.currentTimeMillis() - start;
      assertEquals(204, response.code());
      assertTrue(end < REQUEST_TIMEOUT);  // should take less than 1 second
    }

    @Test(timeout = REQUEST_TIMEOUT)
    public void testTimeout() throws Exception {
      Response response = GET("/jaxrs/timeout");
      assertEquals(503, response.code());
    }


    @Test(timeout = REQUEST_TIMEOUT)
    public void testCancelled() throws Exception {
      Response response = PUT("/jaxrs/cancelled", "");
      assertEquals(204, response.code());
      response = GET("/jaxrs/cancelled");
      assertEquals(500, response.code());
    }

    @Test
    public void testCancel() throws Exception {
      Response response = PUT("/jaxrs/cancelled", "");
      assertEquals(204, response.code());

      response = GET("/jaxrs/cancelled");
      assertEquals(500, response.code());

      response = GET("/jaxrs/cancel");
      assertEquals(503, response.code());

      response = GET("/jaxrs/cancelled");
      assertEquals(204, response.code());
    }

    @Test(timeout = REQUEST_TIMEOUT)
    public void testResumeObject() throws Exception {
      Response response = GET("/jaxrs/resume/object");
      assertEquals(200, response.code());
      String body = response.body().string();
      assertEquals(response.header("Content-Type"), "application/xml");
      assertTrue(body.contains("bill"));
      assertTrue(body.contains("xml"));
    }

    @Test(timeout = REQUEST_TIMEOUT)
    public void testResumeObjectThread() throws Exception {
      Response response = GET("/jaxrs/resume/object/thread");
      assertEquals(200, response.code());
      String body = response.body().string();
      assertTrue(body.contains("bill"));
      assertTrue(body.contains("xml"));
    }

    @Test(timeout = REQUEST_TIMEOUT)
    public void testConnectionCloseHeader() throws Exception {
      Request req = new Request.Builder().get()
              .url(config.getRestHttpHostPortUrl("/jaxrs/empty"))
              .header("Connection", "close")
              .build();
      Response response = client.newCall(req).execute();
      assertEquals(HttpHeaders.Values.CLOSE, response.header("Connection"));
    }
}
