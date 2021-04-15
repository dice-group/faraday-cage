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

//  private static final ThreadLocal<Executor> executors = ThreadLocal.withInitial(()->ThreadlocalInheritingThreadPoolExecutor.get(PARALLELISM));
  private static final Executor executor = ThreadlocalInheritingThreadPoolExecutor.get(PARALLELISM);
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
    return executor;
  }

  public <U> CompletableFuture<U> newIncompleteFuture() {
    return new ThreadlocalInheritingCompletableFuture<>();
  }

}