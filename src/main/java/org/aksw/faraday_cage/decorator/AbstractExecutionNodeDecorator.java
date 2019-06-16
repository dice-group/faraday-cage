package org.aksw.faraday_cage.decorator;

import org.aksw.faraday_cage.engine.ExecutionNode;
import org.apache.jena.rdf.model.Resource;

import java.util.List;

/**
 * Base abstract class for decorator pattern
 */
public abstract class AbstractExecutionNodeDecorator<T> implements ExecutionNode<T> {

  private ExecutionNode<T> wrapped;

  public AbstractExecutionNodeDecorator(ExecutionNode<T> other) {
    this.wrapped = other;
  }

  @Override
  public List<T> apply(List<T> data) {
    return wrapped.apply(data);
  }

  @Override
  public final boolean usesImplicitCloning() {
    return wrapped.usesImplicitCloning();
  }

  @Override
  public final DegreeBounds getDegreeBounds() {
    return wrapped.getDegreeBounds();
  }

  @Override
  public final int getInDegree() {
    return wrapped.getInDegree();
  }

  @Override
  public final int getOutDegree() {
    return wrapped.getOutDegree();
  }

  @Override
  public final void initDegrees(int inDegree, int outDegree) {
    wrapped.initDegrees(inDegree, outDegree);
  }

  @Override
  public void initPluginId(Resource id) {
    wrapped.initPluginId(id);
  }

  @Override
  public final Resource getId() {
    return wrapped.getId();
  }

  @Override
  public final Resource getType() {
    return wrapped.getType();
  }

  @Override
  public T deepCopy(T data) {
    return wrapped.deepCopy(data);
  }

  @Override
  public final boolean isInitialized() {
    return wrapped.isInitialized();
  }

  protected final ExecutionNode<T> getWrapped() {
    return wrapped;
  }

}
