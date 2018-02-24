package org.aksw.faraday_cage;

import java.util.concurrent.CompletableFuture;

/**
 *
 *
 *
 */
public interface CompletableFutureFactory {

  CompletableFutureFactory DEFAULT = new CompletableFutureFactory() {
    @Override
    public <T> CompletableFuture<T> getInstance() {
      return new CompletableFuture<>();
    }

    @Override
    public <T> CompletableFuture<T> getCompletedInstance(T value) {
      return CompletableFuture.completedFuture(value);
    }
  };

  <T> CompletableFuture<T> getInstance();

  <T> CompletableFuture<T> getCompletedInstance(T value);

}
