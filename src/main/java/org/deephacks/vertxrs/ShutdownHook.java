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

class ShutdownHook {

  static void install(final Thread threadToJoin) {
    Thread thread = new ShutdownHookThread(threadToJoin);
    Runtime.getRuntime().addShutdownHook(thread);
  }

  private static class ShutdownHookThread extends Thread {
    private final Thread threadToJoin;

    private ShutdownHookThread(final Thread threadToJoin) {
      super("ShutdownHook: " + threadToJoin.getName());
      this.threadToJoin = threadToJoin;
    }

    @Override
    public void run() {
      shutdown(threadToJoin, 30000);
    }
  }

  public static void shutdown(final Thread t, final long joinwait) {
    if (t == null)
      return;
    t.start();
    while (t.isAlive()) {
      try {
        t.join(joinwait);
      } catch (InterruptedException e) {
      }
    }
  }
}

