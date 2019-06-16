package org.aksw.faraday_cage.engine;

import org.pf4j.ExtensionPoint;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 *
 */
public interface ExecutionNode<T> extends ExtensionPoint, Plugin {

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

  boolean usesImplicitCloning();

  default DegreeBounds getDegreeBounds() {
    return new DegreeBounds(0,1,0,1);
  }

  int getInDegree();

  int getOutDegree();

  void initDegrees(int inDegree, int outDegree);

  T deepCopy(T data);

  static <T> UnaryOperator<List<T>> toMultiExecution(UnaryOperator<T> singleExecution) {
    return (dates -> List.of(singleExecution.apply(dates.isEmpty() ? null : dates.get(0))));
  }

  List<T> apply(List<T> data);

}