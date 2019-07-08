package org.aksw.faraday_cage.engine;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * This custom future implementation enables inheritance of Threadlocal variables.
 */
public class ThreadlocalInheritingCompletableFuture<T> extends CompletableFuture<T> {

  private static final int PARALLELISM;
  private static final String DEER_PARALLELISM_LEVEL = "org.aksw.deer.parallelism";

  static {
    int parallelism = Runtime.getRuntime().availableProcessors();
    if (System.getProperty(DEER_PARALLELISM_LEVEL) != null) {
      parallelism = Integer.parseInt(System.getProperty(DEER_PARALLELISM_LEVEL));
    }
    PARALLELISM = parallelism;
  }

  private static final ThreadLocal<Executor> executors = ThreadLocal.withInitial(()->ThreadlocalInheritingThreadPoolExecutor.get(PARALLELISM));

  /**
   * Returns a new CompletableFuture that is already completed with
   * the given value.
   *
   * @param value the value
   * @param <U> the type of the value
   * @return the completed CompletableFuture
   */
  public static <U> CompletableFuture<U> completedFuture(U value) {
    ThreadlocalInheritingCompletableFuture<U> x = new ThreadlocalInheritingCompletableFuture<>();
    x.complete(value);
    return x;
  }

  @Override
  public Executor defaultExecutor() {
    return executors.get();
  }

  public <U> CompletableFuture<U> newIncompleteFuture() {
    return new ThreadlocalInheritingCompletableFuture<>();
  }

}