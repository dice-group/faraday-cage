package org.aksw.faraday_cage.decorator;

import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.Parameterized;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;

/**
 *
 */
public abstract class AbstractParameterizedExecutionNodeWrapper<V extends ExecutionNode<T>, T> extends AbstractExecutionNodeWrapper<V, T> implements Parameterized {

  private ValidatableParameterMap parameterMap = null;

  @Override
  public final void initParameters(ValidatableParameterMap parameterMap) {
    this.parameterMap = parameterMap;
  }

  @Override
  public final ValidatableParameterMap getParameterMap() {
    return this.parameterMap;
  }

  @Override
  public final boolean isInitialized() {
    return super.isInitialized() && this.parameterMap != null;
  }

}