package org.aksw.faraday_cage;

import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public interface ExecutionGraphBuilder<T> {

  @NotNull ExecutionGraphBuilder addStart(@NotNull Execution<T> execution);

  @NotNull ExecutionGraphBuilder addStartHub(@NotNull Execution<T> hubExecution);

  @NotNull ExecutionGraphBuilder chain(@NotNull Execution<T> execution);

  ExecutionGraphBuilder chainIntoHub(@NotNull Execution<T> to, int toPort);

  ExecutionGraphBuilder chainFromHub(@NotNull Execution<T> from, int fromPort, @NotNull Execution<T> execution);

  @NotNull ExecutionGraphBuilder chainFromHubToHub(@NotNull Execution<T> from, int fromPort, @NotNull Execution<T> to, int toPort);

  @NotNull ExecutionGraph build();

}