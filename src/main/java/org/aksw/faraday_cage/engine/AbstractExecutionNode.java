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
