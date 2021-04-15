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

  public ExecutionNode<T> getWrapped() {
    if (wrapped instanceof AbstractExecutionNodeDecorator) {
      return ((AbstractExecutionNodeDecorator<T>) wrapped).getWrapped();
    } else {
      return wrapped;
    }
  }

}
