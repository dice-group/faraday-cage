/*
 * FARADAY-CAGE - Framework for acyclic directed graphs yielding parallel computations of great efficiency
 * Copyright © 2018 Data Science Group (DICE) (kevin.dressler@uni-paderborn.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
