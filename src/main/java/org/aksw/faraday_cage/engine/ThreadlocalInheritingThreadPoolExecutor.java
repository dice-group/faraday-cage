package org.aksw.faraday_cage.engine;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * A {@link ForkJoinPool} enabling Threadlocal inheritance.
 */
class ThreadlocalInheritingThreadPoolExecutor extends ForkJoinPool {

  @NotNull
  static ThreadlocalInheritingThreadPoolExecutor get(int parallelism) {
    return new ThreadlocalInheritingThreadPoolExecutor(parallelism);
  }

  private ThreadlocalInheritingThreadPoolExecutor(int parallelism) {
    super(parallelism);
  }

  @Override
  public void execute(@NotNull Runnable command) {
    super.execute(wrap(command));
  }

  @NotNull
  public <T> ForkJoinTask<T> submit(@NotNull Callable<T> task) {
    return super.submit(wrap(task));
  }

  @NotNull
  public <T> ForkJoinTask<T> submit(@NotNull Runnable task, T result) {
    return super.submit(wrap(task), result);
  }

  @NotNull
  public ForkJoinTask<?> submit(@NotNull Runnable task) {
    return super.submit(wrap(task));
  }

  @NotNull
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
    throw new UnsupportedOperationException("Operation not implemented.");
  }

  @NotNull
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                       long timeout, TimeUnit unit)
    throws InterruptedException {
    throw new UnsupportedOperationException("Operation not implemented.");
  }

  @NotNull
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks) {
    throw new UnsupportedOperationException("Operation not implemented.");
  }

  @NotNull
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                         long timeout, TimeUnit unit) {
    throw new UnsupportedOperationException("Operation not implemented.");
  }

  @NotNull
  private static Runnable wrap(@NotNull final Runnable runnable) {
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

  @NotNull
  private static <V> Callable<V> wrap(@NotNull final Callable<V> callable) {
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