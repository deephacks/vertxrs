package org.deephacks.vertxrs;

public class SockJsTest extends BaseTest {
/*
  @Test
  public void testSockJs() {
    EventBus eventBus = vertxrs.getVertx().eventBus();
    Thread thread = Thread.currentThread();
    AtomicReference<String> result = new AtomicReference<>();

    eventBus.send("test", "hello", (Message<String> event) -> {
          result.set(event.body());
          LockSupport.unpark(thread);
    });
    LockSupport.park(thread);
    assertThat(result.get(), is("{\"value\":\"hello\"}"));
  }
  */
}
