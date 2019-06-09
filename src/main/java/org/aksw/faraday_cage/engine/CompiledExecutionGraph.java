package org.aksw.faraday_cage.engine;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *
 */
@SuppressWarnings("unchecked")
public class CompiledExecutionGraph implements Runnable {

  private String runId;
  private CompletableFuture trigger;
  private CompletableFuture joiner;

  CompiledExecutionGraph(String runId, CompletableFuture trigger, CompletableFuture joiner) {
    this.runId = runId;
    this.trigger = trigger;
    this.joiner = joiner;
  }

  public String getRunId() {
    return runId;
  }

  @Override
  public void run() {
    FaradayCageContext.setRunId(runId);
    trigger.complete(null);
    joiner.join();
    if (joiner.isCompletedExceptionally()) {
      try {
        joiner.get();
      } catch (@NotNull InterruptedException | ExecutionException e) {
        throw new RuntimeException(e.getCause());
      }
    }
  }

  public void andThen(@NotNull Runnable r) {
    joiner.thenRun(r);
  }
}