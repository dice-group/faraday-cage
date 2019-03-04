package org.aksw.faraday_cage.decorator;

import org.aksw.faraday_cage.engine.ExecutionGraphNode;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public abstract class AbstractExecutionGraphNodeWrapper<V extends ExecutionGraphNode<T>, T> implements ExecutionNodeWrapper<V, T> {

  private Resource id = null;

  @NotNull
  @Override
  public final Resource getId() {
    return id;
  }

  @Override
  public final void initPluginId(@NotNull Resource id) {
    this.id = id;
  }

  @Override
  public boolean isInitialized() {
    return this.id != null;
  }

}