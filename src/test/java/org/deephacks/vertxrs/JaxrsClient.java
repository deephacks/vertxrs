package org.deephacks.vertxrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;

import javax.ws.rs.ext.ContextResolver;

public class JaxrsClient {

  private static final ApacheHttpClient4Engine httpClient;
  private static final JacksonContextResolver jacksonContextResolver;

  static {
    PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(connectionManager.getMaxTotal());
    httpClient = new ApacheHttpClient4Engine(new DefaultHttpClient(connectionManager));
    ObjectMapper objectMapper = new ObjectMapper();
    jacksonContextResolver = new JacksonContextResolver(objectMapper);
  }

  public static <T> T newClient(Class<T> cls, Config config) {
    ResteasyClient client = new ResteasyClientBuilder()
            .httpEngine(httpClient)
            .register(jacksonContextResolver)
            .build();
    ResteasyWebTarget target = (ResteasyWebTarget) client.target(config.getHttpHostPortUrl(""));
    return target.proxy(cls);
  }

  public static class JacksonContextResolver implements ContextResolver<ObjectMapper> {
    private final ObjectMapper objectMapper;

    public JacksonContextResolver(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
      return objectMapper;
    }
  }
}