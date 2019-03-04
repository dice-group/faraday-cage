package org.aksw.faraday_cage.decorator;

import org.aksw.faraday_cage.engine.ExecutionGraphNode;
import org.aksw.faraday_cage.engine.Parameterized;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;

/**
 * Base abstract class for decorator pattern
 */
public abstract class AbstractParameterizedExecutionGraphNodeDecorator<V extends Parameterized & ExecutionGraphNode<T>, T> extends AbstractExecutionGraphNodeDecorator<T> implements Parameterized {

  private V wrapped;

  public AbstractParameterizedExecutionGraphNodeDecorator(V other) {
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

}
