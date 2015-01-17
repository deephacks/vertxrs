package org.deephacks.vertxrs;

import javax.ws.rs.*;

/**
 * Inspiration taken from RestEasy
 */
@Path("/rest")
public class Resource {
  @GET
  @Path("/test")
  @Produces("text/plain")
  public String hello() {
    return "hello world";
  }

  @GET
  @Path("empty")
  public void empty() {
  }

  @GET
  @Path("query")
  public String query(@QueryParam("param") String value) {
    return value;
  }


  @GET
  @Path("/exception")
  @Produces("text/plain")
  public String exception() {
    throw new RuntimeException("THIS IS OK!");
  }

  @GET
  @Path("large")
  @Produces("text/plain")
  public String large() {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < 1000; i++) {
      buf.append(i);
    }
    return buf.toString();
  }

  @POST
  @Path("/post")
  @Produces("text/plain")
  public String post(String postBody) {
    return postBody;
  }
}