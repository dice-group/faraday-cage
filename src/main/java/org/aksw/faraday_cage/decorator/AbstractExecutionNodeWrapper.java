package org.aksw.faraday_cage.decorator;

import org.aksw.faraday_cage.engine.ExecutionNode;
import org.apache.jena.rdf.model.Resource;

/**
 *
 */
public abstract class AbstractExecutionNodeWrapper<V extends ExecutionNode<T>, T> implements ExecutionNodeWrapper<V, T> {

  private Resource id = null;

  @Override
  public final Resource getId() {
    return id;
  }

  @Override
  public final void initPluginId(Resource id) {
    this.id = id;
  }

  @Override
  public boolean isInitialized() {
    return this.id != null;
  }

}