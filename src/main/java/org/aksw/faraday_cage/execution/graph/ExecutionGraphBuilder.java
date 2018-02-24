package org.aksw.faraday_cage.execution.graph;

import org.aksw.faraday_cage.execution.Execution;
import org.aksw.faraday_cage.execution.HubExecution;
import org.aksw.faraday_cage.plugin.Identifiable;
import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public interface ExecutionGraphBuilder<U extends Execution & Identifiable, V extends HubExecution & Identifiable> {

  @NotNull ExecutionGraphBuilder addStart(@NotNull U execution);

  @NotNull ExecutionGraphBuilder addStartHub(@NotNull V hubExecution);

  @NotNull ExecutionGraphBuilder chain(@NotNull U execution);

  ExecutionGraphBuilder chainIntoHub(@NotNull V to, int toPort);

  ExecutionGraphBuilder chainFromHub(@NotNull V from, int fromPort, @NotNull U execution);

  @NotNull ExecutionGraphBuilder chainFromHubToHub(@NotNull V from, int fromPort, @NotNull V to, int toPort);

  @NotNull ExecutionGraph build();

}