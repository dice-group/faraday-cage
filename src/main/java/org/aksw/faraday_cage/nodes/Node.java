package org.aksw.faraday_cage.nodes;

import org.aksw.faraday_cage.Plugin;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public interface Node<T> extends Plugin<T> {

  class DegreeBounds {

    private final int minIn;
    private final int maxIn;
    private final int minOut;
    private final int maxOut;

    public DegreeBounds(int minIn, int maxIn, int minOut, int maxOut) {
      this.minIn = minIn;
      this.maxIn = maxIn;
      this.minOut = minOut;
      this.maxOut = maxOut;
    }

    public boolean notSatisfiedBy(int in, int out, boolean withImplicitCloning) {
      boolean inBounds;
      inBounds  = in >= minIn();
      inBounds &= in <= maxIn();
      inBounds &= out >= minOut();
      // only check maxOut if it is not 1 when using implicit cloning
      if (maxOut() != 1 || !withImplicitCloning) {
        inBounds &= out <= maxOut();
      }
      return !inBounds;
    }

    public boolean notSatisfiedBy(int in, int out) {
      return notSatisfiedBy(in, out, false);
    }

    public int minIn() {
      return minIn;
    }

    public int maxIn() {
      return maxIn;
    }

    public int minOut() {
      return minOut;
    }

    public int maxOut() {
      return maxOut;
    }

    public String toString() {
      return minIn + " <= in <= " + maxIn + "; " + minOut + " <= out <= " + maxOut;
    }

  }

  default DegreeBounds getDegreeBounds() {
    return new DegreeBounds(0,1,0,1);
  }

  int getInDegree();

  int getOutDegree();

  void init(@NotNull Resource id, int inDegree, int outDegree);

}
