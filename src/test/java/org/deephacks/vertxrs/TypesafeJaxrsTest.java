package org.deephacks.vertxrs;

import org.deephacks.vertxrs.TestResource.Data;
import org.junit.Test;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TypesafeJaxrsTest extends BaseTest {

  @Test
  public void testPost() {
    TypesafeService service = JaxrsClient.newClient(TypesafeService.class, config);
    Data in = new Data("name", "value");
    Data out = service.post(in, "p", "q");
    assertThat(out, is(new Data("namep", "valueq")));
  }

  @Path("/rest/typesafe")
  public static interface TypesafeService {

    @Path("post/{path}") @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Data post(Data data, @PathParam("path") String path, @QueryParam("query") String query);
  }

  public static class TypesafeResource implements TypesafeService {

    @Override
    public Data post(Data data, String path, String query) {
      return new Data(data.getName() + path, data.getValue() + query);
    }
  }
}
