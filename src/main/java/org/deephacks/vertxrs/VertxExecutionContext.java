/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.deephacks.vertxrs;

import org.jboss.resteasy.core.AbstractAsynchronousResponse;
import org.jboss.resteasy.core.AbstractExecutionContext;
import org.jboss.resteasy.core.SynchronousDispatcher;

import org.jboss.resteasy.spi.*;

import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.core.*;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


class VertxExecutionContext extends AbstractExecutionContext {
  private static final ScheduledExecutorService scheduledExecutor =
          Executors.newSingleThreadScheduledExecutor();
  protected final VertxHttpResponse response;
  protected volatile boolean done;
  protected volatile boolean cancelled;
  protected volatile boolean wasSuspended;
  protected VertxHttpAsyncResponse asyncResponse;
  private volatile boolean flushed;

  public VertxExecutionContext(VertxHttpRequest request,
                               VertxHttpResponse response,
                               SynchronousDispatcher dispatcher) {
    super(dispatcher, request, response);
    this.request = request;
    this.response = response;
    this.asyncResponse = new VertxHttpAsyncResponse(dispatcher, request, response);
  }

  @Override
  public boolean isSuspended() {
    return wasSuspended;
  }

  @Override
  public ResteasyAsynchronousResponse getAsyncResponse() {
    return asyncResponse;
  }

  @Override
  public ResteasyAsynchronousResponse suspend() throws IllegalStateException {
    return suspend(-1);
  }

  @Override
  public ResteasyAsynchronousResponse suspend(long millis) throws IllegalStateException {
    return suspend(millis, TimeUnit.MILLISECONDS);
  }

  @Override
  public ResteasyAsynchronousResponse suspend(long time, TimeUnit unit) throws IllegalStateException {
    if (wasSuspended) {
      throw new IllegalStateException("Already suspended");
    }
    wasSuspended = true;
    return asyncResponse;
  }

  class VertxHttpAsyncResponse extends AbstractAsynchronousResponse {
    private final Object responseLock = new Object();
    protected ScheduledFuture timeoutFuture;
    private VertxHttpResponse vertxResponse;

    public VertxHttpAsyncResponse(SynchronousDispatcher dispatcher,
                                  VertxHttpRequest request,
                                  VertxHttpResponse response) {
      super(dispatcher, request, response);
      this.vertxResponse = response;
    }

    @Override
    public void initialRequestThreadFinished() {
      // done
    }

    @Override
    public boolean resume(Object entity) {
      synchronized (responseLock) {
        if (done) return false;
        if (cancelled) return false;
        try {
          return internalResume(entity);
        } finally {
          done = true;
          flush();
        }
      }
    }

    @Override
    public boolean resume(Throwable ex) {
      synchronized (responseLock) {
        if (done) return false;
        if (cancelled) return false;
        try {
          return internalResume(ex);
        } catch (UnhandledException unhandled) {
          return internalResume(Response.status(500).build());
        } finally {
          done = true;
          flush();
        }
      }
    }

    @Override
    public boolean cancel() {
      synchronized (responseLock) {
        if (cancelled) {
          return true;
        }
        if (done) {
          return false;
        }
        done = true;
        cancelled = true;
        try {
          return internalResume(Response.status(Response.Status.SERVICE_UNAVAILABLE).build());
        } finally {
          flush();
        }
      }
    }

    @Override
    public boolean cancel(int retryAfter) {
      synchronized (responseLock) {
        if (cancelled) return true;
        if (done) return false;
        done = true;
        cancelled = true;
        try {
          return internalResume(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                  .header(HttpHeaders.RETRY_AFTER, retryAfter).build());
        } finally {
          flush();
        }
      }
    }

    protected synchronized void flush() {
      flushed = true;
      try {
        vertxResponse.finish();
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        // TODO?
        // ctx.close();
      }
    }

    @Override
    public boolean cancel(Date retryAfter) {
      synchronized (responseLock) {
        if (cancelled) return true;
        if (done) return false;
        done = true;
        cancelled = true;
        try {
          return internalResume(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                  .header(HttpHeaders.RETRY_AFTER, retryAfter).build());
        } finally {
          flush();
        }
      }
    }

    @Override
    public boolean isSuspended() {
      return !done && !cancelled;
    }

    @Override
    public boolean isCancelled() {
      return cancelled;
    }

    @Override
    public boolean isDone() {
      return done;
    }

    @Override
    public boolean setTimeout(long time, TimeUnit unit) {
      synchronized (responseLock) {
        if (done || cancelled) return false;
        if (timeoutFuture != null && !timeoutFuture.cancel(false)) {
          return false;
        }
        timeoutFuture = scheduledExecutor.schedule(() -> handleTimeout(), time, unit);
      }
      return true;
    }

    protected void handleTimeout() {
      if (timeoutHandler != null) {
        timeoutHandler.handleTimeout(this);
      }
      if (done) return;
      resume(new ServiceUnavailableException());
    }
  }
}

