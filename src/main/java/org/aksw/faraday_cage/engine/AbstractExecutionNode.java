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
package org.aksw.faraday_cage.engine;

import org.apache.jena.rdf.model.Resource;

import java.util.List;

/**
 *
 *
 *
 */
public abstract class AbstractExecutionNode<T> implements ExecutionNode<T> {

  private int inDegree = -1;
  private int outDegree = -1;
  private Resource id = null;

  @Override
  public final int getInDegree() {
    return inDegree;
  }

  @Override
  public final int getOutDegree() {
    return outDegree;
  }

  @Override
  public final void initDegrees(int inDegree, int outDegree) throws InvalidExecutionGraphException {
    DegreeBounds degreeBounds = getDegreeBounds();
    if (inDegree < degreeBounds.minIn() || inDegree > degreeBounds.maxIn()) {
      throw new InvalidExecutionGraphException("Number of inputs for node " + id +
        " is " + inDegree + ", but must be in [" + degreeBounds.minIn() + ", " + degreeBounds.maxIn() + "]!");
    }
    if (outDegree < degreeBounds.minOut() || outDegree > degreeBounds.maxOut()) {
      throw new InvalidExecutionGraphException("Number of outputs for node " + id +
        " is " + outDegree + ", but must be in [" + degreeBounds.minOut() + ", " + degreeBounds.maxOut() + "]!");
    }
      this.inDegree = inDegree;
      this.outDegree = outDegree;
  }

  public final void initPluginId(Resource id) {
    this.id = id;
  }

  @Override
  public final Resource getId() {
    return id;
  }

  @Override
  public final List<T> apply(List<T> data) {
    if (!isInitialized()) {
      throw new RuntimeException(this.getClass().getCanonicalName() + " must be initialized before calling apply()!");
    }
    return safeApply(data);
  }

  @Override
  public boolean isInitialized() {
    return getInDegree() >= 0 && getOutDegree() >= 0 && id != null;
  }

  protected abstract List<T> safeApply(List<T> data);

}
