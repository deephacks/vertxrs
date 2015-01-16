package org.deephacks.vertxrs;

import com.squareup.okhttp.*;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestResourceTest extends BaseTest {

  @Test
  public void testPostJson() throws Exception {
    String body = "{\"name\":\"test\",\"value\":\"value\"}";
    Response response = POST("/test-resource/json", body);
    assertThat(response.body().string(), is(body));
    assertThat(response.header("Content-Type"), is("application/json"));
  }

  @Test
  public void testGetPathAndQuery() throws Exception {
    Response response = GET("/test-resource/simple/1?param=2");
    assertThat(response.body().string(), is("1 2"));
  }

  @Test
  public void testClose() throws Exception {
    Request req = new Request.Builder().get()
            .url(config.getHttpHostPortUrl("/test-resource/simple/1?param=2"))
            .header("Connection", "close")
            .build();
    Response response = client.newCall(req).execute();
    assertThat(response.header("Connection"), is("close"));
  }

  @Test
  public void testJaxrsResponse() throws Exception {
    String body = "{\"test\":\"test\"}";
    RequestBody requestBody = RequestBody.create(APPLICATION_JSON, body);
    Request req = new Request.Builder().post(requestBody)
            .url(config.getHttpHostPortUrl("/test-resource/jaxrs"))
            .header("Connection", "close")
            .header("Content-Type", "application/json")
            .build();
    Response response = client.newCall(req).execute();
    assertThat(response.body().string(), is(body));
    assertThat(response.header("header"), is("value"));
    assertThat(response.header("Connection"), is("close"));
  }

  @Test
  public void testJaxrsInjectHeaders() throws Exception {
    Request req = new Request.Builder().get()
            .url(config.getHttpHostPortUrl("/test-resource/inject-headers"))
            .header("test-header", "value")
            .build();
    Response response = client.newCall(req).execute();
    assertThat(response.body().string(), is("value"));
  }

  @Test
  public void testAsync() throws Exception {
    Thread thread = Thread.currentThread();
    AtomicReference<String> result = new AtomicReference<>();
    new Thread(() -> {
      try {
        Response response = GET("/test-resource/async");
        result.set(response.body().string());
        LockSupport.unpark(thread);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();
    Thread.sleep(1000);
    Response response = GET("/test-resource/resume?param=123");
    LockSupport.park(thread);
    assertThat(result.get(), is("123"));
    assertThat(response.body().string(), is("123"));
  }
}
