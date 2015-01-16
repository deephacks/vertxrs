package org.deephacks.vertxrs;

import org.junit.Test;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

import com.squareup.okhttp.Response;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ExceptionMapperTest extends BaseTest {

  @Test
  public void testExceptionMapper() throws IOException {
    Response response = GET("/exception-mapper/exception");
    assertThat(response.code(), is(400));
  }

  @Provider
  public static class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Throwable> {

    @Context
    private org.jboss.resteasy.spi.HttpRequest request;

    @Context
    private org.jboss.resteasy.spi.HttpResponse response;

    @Override
    public javax.ws.rs.core.Response toResponse(Throwable exception) {
      if (request.getUri() == null) {
        throw new RuntimeException("Could not inject request");
      }
      if (response == null) {
        throw new RuntimeException("Could not inject response");
      }
      if (exception instanceof IllegalArgumentException) {
        return javax.ws.rs.core.Response.status(400).build();
      } else if (ServiceUnavailableException.class.isAssignableFrom(exception.getClass())) {
        return javax.ws.rs.core.Response.status(503).build();
      } else if (ForbiddenException.class.isAssignableFrom(exception.getClass())) {
        return javax.ws.rs.core.Response.status(403).build();
      } else if (RuntimeException.class.isAssignableFrom(exception.getClass())) {
        throw (RuntimeException) exception;
      }
      throw new RuntimeException(exception);
    }
  }


  @Path("/exception-mapper")
  public static class ExceptionMapperResource {

    @GET
    @Path("exception")
    public void exception() {
      throw new IllegalArgumentException();
    }
  }
}
