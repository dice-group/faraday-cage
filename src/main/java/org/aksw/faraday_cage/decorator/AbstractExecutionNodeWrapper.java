package org.aksw.faraday_cage.decorator;

import org.aksw.faraday_cage.engine.ExecutionNode;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public abstract class AbstractExecutionNodeWrapper<V extends ExecutionNode<T>, T> implements ExecutionNodeWrapper<V, T> {

  @Nullable
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