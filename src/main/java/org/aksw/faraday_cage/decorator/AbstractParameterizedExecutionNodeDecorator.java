package org.aksw.faraday_cage.decorator;

import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.Parameterized;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;

/**
 * Base abstract class for decorator pattern
 */
public abstract class AbstractParameterizedExecutionNodeDecorator<V extends Parameterized & ExecutionNode<T>, T> extends AbstractExecutionNodeDecorator<T> implements Parameterized {

  private V wrapped;

  public AbstractParameterizedExecutionNodeDecorator(V other) {
    super(other);
    wrapped = other;
  }

  public ValidatableParameterMap createParameterMap() {
    return wrapped.createParameterMap();
  }

  public final void initParameters(ValidatableParameterMap parameterMap) {
    wrapped.initParameters(parameterMap);
  }

  public final ValidatableParameterMap getParameterMap() {
    return wrapped.getParameterMap();
  }

  public V getWrapped() {
    if (wrapped instanceof AbstractParameterizedExecutionNodeDecorator) {
      return ((AbstractParameterizedExecutionNodeDecorator<V,T>) wrapped).getWrapped();
    } else {
      return wrapped;
    }
  }

}
