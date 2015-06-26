package org.deephacks.vertxrs;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Path("/rest/test-resource")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TestResource implements Handler<Message> {
  static LinkedList<AsyncResponse> responses = new LinkedList<>();

  @GET
  @Path("simple/{path}")
  public String simple(@PathParam("path") String path, @QueryParam("param") String param) {
    return path + " " + param;
  }

  @POST @Path("json")
  public Data json(Data data) {
    return data;
  }


  @POST @Path("jaxrs")
  public Response jaxrsResponse(String body) {
    return Response.accepted(body).header("header", "value").build();
  }

  @GET @Path("inject-headers")
  public String injectHeaders(@Context HttpHeaders headers) {
    return headers.getHeaderString("test-header");
  }

  @GET
  @Path("resume")
  public String resume(@QueryParam("param") String test) {
    Optional.ofNullable(responses.pollFirst()).ifPresent(response -> response.resume(test));
    return test;
  }

  @GET
  @Path("async")
  public void async(@Suspended final AsyncResponse response) {
    response.setTimeout(1, TimeUnit.DAYS);
    responses.add(response);
  }

  @POST @Path("asyncJson")
  public void asyncJsonPost(Data data, @Suspended final AsyncResponse response) {
    response.resume(data);
  }

  @Override
  public void handle(Message event) {
    event.reply("{\"value\":\"" + event.body() +"\"}");
  }

  public static class Data {
    private String name;
    private String value;

    public Data() {

    }

    public Data(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Data data = (Data) o;

      if (name != null ? !name.equals(data.name) : data.name != null) return false;
      if (value != null ? !value.equals(data.value) : data.value != null) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + (value != null ? value.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return "Data{" +
              "name='" + name + '\'' +
              ", value='" + value + '\'' +
              '}';
    }
  }
}

