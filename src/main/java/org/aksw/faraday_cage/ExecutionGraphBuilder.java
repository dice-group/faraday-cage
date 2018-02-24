package org.aksw.faraday_cage;

import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public interface ExecutionGraphBuilder<T extends IdentifiableExecution> {

  @NotNull ExecutionGraphBuilder addStart(@NotNull T execution);

  @NotNull ExecutionGraphBuilder addStartHub(@NotNull T hubExecution);

  @NotNull ExecutionGraphBuilder chain(@NotNull T execution);

  ExecutionGraphBuilder chainIntoHub(@NotNull T to, int toPort);

  ExecutionGraphBuilder chainFromHub(@NotNull T from, int fromPort, @NotNull T execution);

  @NotNull ExecutionGraphBuilder chainFromHubToHub(@NotNull T from, int fromPort, @NotNull T to, int toPort);

  @NotNull ExecutionGraph build();

}