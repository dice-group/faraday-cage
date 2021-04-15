/*
 * FARADAY-CAGE - Framework for acyclic directed graphs yielding parallel computations of great efficiency
 * Copyright © 2018 Data Science Group (DICE) (kevin.dressler@uni-paderborn.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.faraday_cage.engine;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * A {@link ForkJoinPool} enabling Threadlocal inheritance.
 */
class ThreadlocalInheritingThreadPoolExecutor extends ForkJoinPool {

  static ThreadlocalInheritingThreadPoolExecutor get(int parallelism) {
    return new ThreadlocalInheritingThreadPoolExecutor(parallelism);
  }

  private ThreadlocalInheritingThreadPoolExecutor(int parallelism) {
    super(parallelism);
  }

  @Override
  public void execute(Runnable command) {
    super.execute(wrap(command));
  }

  public <T> ForkJoinTask<T> submit(Callable<T> task) {
    return super.submit(wrap(task));
  }

  public <T> ForkJoinTask<T> submit(Runnable task, T result) {
    return super.submit(wrap(task), result);
  }

  public ForkJoinTask<?> submit(Runnable task) {
    return super.submit(wrap(task));
  }

  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
    throw new UnsupportedOperationException("Operation not implemented.");
  }

  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                       long timeout, TimeUnit unit)
    throws InterruptedException {
    throw new UnsupportedOperationException("Operation not implemented.");
  }

  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) {
    throw new UnsupportedOperationException("Operation not implemented.");
  }

  public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                         long timeout, TimeUnit unit) {
    throw new UnsupportedOperationException("Operation not implemented.");
  }

  private static Runnable wrap(final Runnable runnable) {
    String newRunId = FaradayCageContext.getRunId();
    return () -> {
      String previousRunId = FaradayCageContext.getRunId();
      FaradayCageContext.setRunId(newRunId);
      FaradayCageContext.valueConsumer.forEach(runIdConsumer -> runIdConsumer.accept(newRunId));
      try {
        runnable.run();
      } finally {
        FaradayCageContext.setRunId(previousRunId);
      }
    };
  }

  private static <V> Callable<V> wrap(final Callable<V> callable) {
    String newRunId = FaradayCageContext.getRunId();
    return () -> {
      String previousRunId = FaradayCageContext.getRunId();
      FaradayCageContext.setRunId(newRunId);
      V call;
      try {
        call = callable.call();
      } finally {
        FaradayCageContext.setRunId(previousRunId);
      }
      return call;
    };
  }





}