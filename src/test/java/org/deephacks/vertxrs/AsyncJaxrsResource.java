package org.deephacks.vertxrs;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Inspiration taken from RestEasy
 */
@Path("/rest/jaxrs")
public class AsyncJaxrsResource {
  protected boolean cancelled;

  @GET
  @Path("resume/object")
  @Produces("application/xml")
  public void resumeObject(@Suspended final AsyncResponse response) {
    response.resume(new XmlData("bill"));
  }

  @GET
  @Path("resume/object/thread")
  @Produces("application/xml")
  public void resumeObjectThread(@Suspended final AsyncResponse response) throws Exception {
    Thread t = new Thread() {
      @Override
      public void run() {
        response.resume(new XmlData("bill"));
      }
    };
    t.start();
  }

  @GET
  @Path("injection-failure/{param}")
  public void injectionFailure(@Suspended final AsyncResponse response, @PathParam("param") int id) {
    System.out.println("injectionFailure: " + id);
    throw new ForbiddenException("Should be unreachable");
  }

  @GET
  @Path("method-failure")
  public void injectionFailure(@Suspended final AsyncResponse response) {
    throw new ForbiddenException("Should be unreachable");
  }

  @GET
  @Produces("text/plain")
  public void get(@Suspended final AsyncResponse response) throws Exception {
    response.setTimeout(200000, TimeUnit.MILLISECONDS);
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(100);
          Response jaxrs = Response.ok("hello").type(MediaType.TEXT_PLAIN).build();
          response.resume(jaxrs);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();
  }

  @GET
  @Path("empty")
  @Produces("text/plain")
  public void getEmpty(@Suspended final AsyncResponse response) throws Exception {
    response.setTimeout(200000, TimeUnit.MILLISECONDS);
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(100);
          response.resume(Response.noContent().build());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();
  }

  @GET
  @Path("timeout")
  @Produces("text/plain")
  public void timeout(@Suspended final AsyncResponse response) {
    response.setTimeout(10, TimeUnit.MILLISECONDS);
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(100000);
          Response jaxrs = Response.ok("goodbye").type(MediaType.TEXT_PLAIN).build();
          response.resume(jaxrs);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();
  }

  @GET
  @Path("cancelled")
  public Response getCancelled() {
    if (cancelled) return Response.noContent().build();
    else return Response.status(500).build();
  }

  @PUT
  @Path("cancelled")
  public void resetCancelled() {
    cancelled = false;
  }

  @GET
  @Path("cancel")
  @Produces("text/plain")
  public void cancel(@Suspended final AsyncResponse response) throws Exception {
    response.setTimeout(10000, TimeUnit.MILLISECONDS);
    final CountDownLatch sync = new CountDownLatch(1);
    final CountDownLatch ready = new CountDownLatch(1);
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          sync.countDown();
          ready.await();
          Response jaxrs = Response.ok("hello").type(MediaType.TEXT_PLAIN).build();
          cancelled = !response.resume(jaxrs);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    t.start();

    sync.await();
    response.cancel();
    ready.countDown();
  }

  @XmlRootElement(name = "data")
  public static class XmlData {
    protected String name;

    public XmlData(String data) {
      this.name = data;
    }

    public XmlData() {
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}