package org.aksw.faraday_cage.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 *
 */
@SuppressWarnings("unchecked")
public class CompiledExecutionGraph implements Runnable {

  private String runId;
  private CompletableFuture trigger;
  private CompletableFuture joiner;

  private static final Logger logger = LoggerFactory.getLogger(CompiledExecutionGraph.class);

  public static <T> CompiledExecutionGraph of(ExecutionGraph<T> executionGraph) {
    return CompiledExecutionGraph.of(executionGraph, FaradayCageContext.newRunId());
  }

  public static <T> CompiledExecutionGraph of(ExecutionGraph<T> executionGraph, String runId) {
    final CompletableFuture<List<T>> trigger = new ThreadlocalInheritingCompletableFuture<>();
    CompletableFuture<List<T>> joiner= ThreadlocalInheritingCompletableFuture.completedFuture(null);
    final List<CompletableFuture<List<T>>> futures = new ArrayList<>(executionGraph.getSize());
    for (int i = 0; i < executionGraph.getSize(); i++) futures.add(null);
    for (int i = executionGraph.getSize()-1; i >= 0; i--) {
      ExecutionNode<T> currentNode = executionGraph.getNode(i);
      short[] currentRow = executionGraph.getRow(i);
      int inDegree = currentRow[0];
      int outDegree = currentRow[1];
      currentNode.initDegrees(currentRow[0], currentRow[1]);
      CompletableFuture<List<T>> handle;
      if (inDegree == 0) {
        handle = trigger;
      } else {
        handle = futures.get(currentRow[2]);
        for (int k = 1; k < inDegree; k++) {
          final int l = k;
          handle = handle.thenCombine(futures.get(currentRow[2 + l * 2]), (a, b) -> {
            a.add(b.get(currentRow[2 + l * 2 + 1]));
            return a;
          });
        }
      }
      CompletableFuture<List<T>> currentFuture = handle.thenApplyAsync(l -> {
          logger.info("Executing {}...", currentNode.getId());
          return currentNode.apply(l).stream().map(currentNode::deepCopy).collect(Collectors.toList());
        }
      );
      futures.set(i, currentFuture);
      if (outDegree == 0) {
        joiner = joiner.thenCombine(currentFuture, (a, b) -> b);
      }
    }
    //    if (startPipelines.isEmpty() && startHubs.isEmpty()) {
    //      throw new InvalidExecutionGraphException("No root nodes have been detected. Please supply a non-empty acyclic configuration graph!");
    //    }
    return new CompiledExecutionGraph(runId, trigger, joiner);
  }

  private CompiledExecutionGraph(String runId, CompletableFuture trigger, CompletableFuture joiner) {
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
    trigger.complete(new ArrayList<>());
    joiner.join();
    if (joiner.isCompletedExceptionally()) {
      try {
        joiner.get();
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e.getCause());
      }
    }
  }

  public void andThen(Runnable r) {
    joiner.thenRun(r);
  }


}