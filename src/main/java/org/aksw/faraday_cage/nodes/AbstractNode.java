package org.aksw.faraday_cage.nodes;

import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 *
 *
 */
public abstract class AbstractNode<T> implements Node<T> {

  private int inDegree = -1;
  private int outDegree = -1;
  private Resource id = null;
  private boolean useImplicitCloning = false;

  public static abstract class WithImplicitCloning<T> extends AbstractNode<T> {

    public WithImplicitCloning() {
      this.useImplicitCloning(true);
    }

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
  public final void init(@NotNull Resource id, int inDegree, int outDegree) {
    init(id);
    if (getDegreeBounds().notSatisfiedBy(inDegree, outDegree, useImplicitCloning)) {
      throw new RuntimeException("Arity not valid.");
    } else {
      this.inDegree = inDegree;
      this.outDegree = outDegree;
    }
  }

  @Override
  public final void init(@NotNull Resource id) {
    this.id = id;
  }

  @NotNull
  @Override
  public final Resource getId() {
    return id;
  }

  @Override
  public final T apply(T data) {
    List<T> dates = apply(List.of(data));
    if (!dates.isEmpty()) {
      return dates.get(0);
    } else if (outDegree == 0) {
      return null;
    } else {
      throw new RuntimeException("Got null reference in non-leaf node.");
    }
  }

  @Override
  public final List<T> apply(List<T> data) {
    if (!isInitialized()) {
      throw new RuntimeException(this.getClass().getCanonicalName() + " must be initialized before calling apply()!");
    }
    List<T> result = safeApply(data);
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
