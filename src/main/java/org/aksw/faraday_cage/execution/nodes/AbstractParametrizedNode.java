package org.aksw.faraday_cage.execution.nodes;

import org.aksw.faraday_cage.parametrized.ParameterMap;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public abstract class AbstractParametrizedNode<T> extends AbstractNode<T> implements ParametrizedNode<T> {

  private ParameterMap parameterMap = null;

  @Override
  public final void init(@NotNull Resource id, int inDegree, int outDegree, @NotNull ParameterMap parameterMap) {
    init(id, inDegree, outDegree);
    init(parameterMap);
  }

  @Override
  public void init(@NotNull ParameterMap parameterMap) {
    this.parameterMap = parameterMap;
    this.validateAndAccept(parameterMap);
  }

  protected abstract void validateAndAccept(@NotNull ParameterMap parameterMap);

  @Override
  public final @NotNull ParameterMap getParameterMap() {
    return parameterMap;
  }

  @Override
  public final boolean isInitialized() {
    return super.isInitialized() && parameterMap != null;
  }
}
