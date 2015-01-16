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

import java.util.Optional;

public class Config {
  private String staticRootPath;
  private String sockJsPath;
  private String jaxrsPath;
  private int httpPort;
  private String httpHost;
  private int eventLoops;

  private Config(Builder builder) {
    this.staticRootPath = Optional.ofNullable(builder.staticRootPath).orElse("src/main/web");
    this.jaxrsPath = Optional.ofNullable(builder.staticRootPath).orElse("rest");
    this.sockJsPath = Optional.ofNullable(builder.sockJsPath).orElse("/sockjs");
    this.httpPort = Optional.ofNullable(builder.httpPort).orElse(8080);
    this.httpHost = Optional.ofNullable(builder.httpHost).orElse("localhost");
    this.eventLoops = Optional.ofNullable(builder.eventLoops)
            .orElse(Runtime.getRuntime().availableProcessors());
  }

  public static Config defaultConfig() {
    return new Config(new Builder());
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getStaticRootPath() {
    return staticRootPath;
  }

  public String getSockJsPath() {
    return sockJsPath;
  }

  public String getJaxrsPath() {
    return jaxrsPath;
  }

  public String getHttpHost() {
    return httpHost;
  }

  public int getHttpPort() {
    return httpPort;
  }

  public int getEventLoops() {
    return eventLoops;
  }

  public String getHttpHostPortUrl(String path) {
    return "http://" + getHttpHost() + ":" + getHttpPort() + path;
  }

  public static class Builder {
    private String staticRootPath;
    private String sockJsPath;
    private String jaxrsPath;
    private String httpHost;
    private Integer httpPort;
    private Integer eventLoops;

    public Builder getStaticRootPath(String path) {
      staticRootPath = path;
      return this;
    }

    public Builder withSockJsPath(String path) {
      this.sockJsPath = path;
      return this;
    }

    public Builder withJaxrsPath(String path) {
      this.jaxrsPath = path;
      return this;
    }

    public Builder withHttpHost(String host) {
      this.httpHost = host;
      return this;
    }

    public Builder withHttpPort(int httpPort) {
      this.httpPort = httpPort;
      return this;
    }

    public Builder withEventLoops(int number) {
      eventLoops = number;
      return this;
    }

    public Config build() {
      return new Config(this);
    }
  }
}

