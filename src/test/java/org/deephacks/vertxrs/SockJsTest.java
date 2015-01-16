package org.deephacks.vertxrs;

import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SockJsTest extends BaseTest {

  @Test
  public void testSockJs() {
    EventBus eventBus = stiletto.getVertx().eventBus();
    Thread thread = Thread.currentThread();
    AtomicReference<String> result = new AtomicReference<>();

    eventBus.send("test", "hello", new Handler<Message<String>>() {
      @Override
      public void handle(Message<String> event) {
        result.set(event.body());
        LockSupport.unpark(thread);
      }
    });
    LockSupport.park(thread);
    assertThat(result.get(), is("{\"value\":\"hello\"}"));
  }
}
