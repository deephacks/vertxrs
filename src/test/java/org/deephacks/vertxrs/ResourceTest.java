package org.deephacks.vertxrs;

import com.squareup.okhttp.Response;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Inspiration taken from RestEasy
 */
public class ResourceTest extends BaseTest {

  @Test
  public void testBasic() throws Exception {
    assertEquals("hello world", GET("/test").body().string());
  }

  @Test
  public void testQuery() throws Exception {
    assertEquals("val", GET("/query?param=val").body().string());
  }

  @Test
  public void testEmpty() throws Exception {
    assertEquals(204, GET("/empty").code());
  }

  @Test
  public void testLarge() throws Exception {
    Response response = GET("/large");
    assertEquals(200, response.code());
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < 1000; i++) {
      buf.append(i);
    }
    String expected = buf.toString();
    String have = response.body().string();
    assertEquals(expected, have);
  }

  @Test
  public void testUnhandledException() throws Exception {
     assertEquals(500, GET("/exception").code());
  }

  @Test
  public void testPost() throws IOException {
    String body = POST("/post", "hello world").body().string();
    assertEquals("hello world", body);
  }
}
