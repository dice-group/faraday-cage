package org.aksw.faraday_cage;

import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public interface ExecutionGraphBuilder<T> {

  @NotNull ExecutionGraphBuilder addStart(@NotNull IdentifiableExecution<T> execution);

  @NotNull ExecutionGraphBuilder addStartHub(@NotNull IdentifiableExecution<T> hubExecution);

  @NotNull ExecutionGraphBuilder chain(@NotNull IdentifiableExecution<T> execution);

  ExecutionGraphBuilder chainIntoHub(@NotNull IdentifiableExecution<T> to, int toPort);

  ExecutionGraphBuilder chainFromHub(@NotNull IdentifiableExecution<T> from, int fromPort, @NotNull IdentifiableExecution<T> execution);

  @NotNull ExecutionGraphBuilder chainFromHubToHub(@NotNull IdentifiableExecution<T> from, int fromPort, @NotNull IdentifiableExecution<T> to, int toPort);

  @NotNull ExecutionGraph build();

}