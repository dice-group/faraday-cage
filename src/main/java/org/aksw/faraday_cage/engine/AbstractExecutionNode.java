package org.aksw.faraday_cage.engine;

import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
  private boolean useImplicitCloning = false;

  public static abstract class WithImplicitCloning<T> extends AbstractExecutionNode<T> {

    public WithImplicitCloning() {
      this.useImplicitCloning(true);
    }

  }

  @Override
  public final boolean usesImplicitCloning() {
    return useImplicitCloning;
  }

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
    initPluginId(id);
    DegreeBounds degreeBounds = getDegreeBounds();
    if (inDegree < degreeBounds.minIn() || inDegree > degreeBounds.maxIn()) {
      throw new InvalidExecutionGraphException("Number of inputs for node " + id +
        " is " + inDegree + ", but must be in [" + degreeBounds.minIn() + ", " + degreeBounds.maxIn() + "]!");
    }
    if (outDegree < degreeBounds.minOut() ||
      (!useImplicitCloning || (degreeBounds.maxOut() != 1)) && (outDegree > degreeBounds.maxOut())) {
      throw new InvalidExecutionGraphException("Number of outputs for node " + id +
        " is " + outDegree + ", but must be in [" + degreeBounds.minOut() + ", " + degreeBounds.maxOut() + "]!");
    }
      this.inDegree = inDegree;
      this.outDegree = outDegree;
  }

  public final void initPluginId(@NotNull Resource id) {
    this.id = id;
  }

  @NotNull
  @Override
  public final Resource getId() {
    return id;
  }

  @Override
  public final List<T> apply(List<T> data) {
    if (!isInitialized()) {
      throw new RuntimeException(this.getClass().getCanonicalName() + " must be initialized before calling apply()!");
    }
    List<T> result = new ArrayList<>(safeApply(data));
    // implicit cloning implemented here
    if (useImplicitCloning
      && outDegree > result.size() && result.size() == 1 && getDegreeBounds().maxOut() == 1) {
      for (int i = 0; i < outDegree - 1; i++) {
        result.add(deepCopy(result.get(0)));
      }
    }
    return result;
  }

  @Override
  public boolean isInitialized() {
    return getInDegree() >= 0 && getOutDegree() >= 0 && id != null;
  }

  protected abstract List<T> safeApply(List<T> data);

  protected abstract T deepCopy(T data);

  final void useImplicitCloning(boolean useImplicitCloning) {
    this.useImplicitCloning = useImplicitCloning;
  }

}
