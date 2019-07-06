package org.aksw.faraday_cage.engine;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
class AdjacencyMatrix {

  private static final Logger logger = LoggerFactory.getLogger(AdjacencyMatrix.class);

  private final int[][][] rows;

  AdjacencyMatrix(int n) {
    rows = new int[n][n][0];
  }

  AdjacencyMatrix(AdjacencyMatrix other) {
    rows = other.rows;
  }

  void addEdge(int from, int fromPort, int to, int toPort) {
    int k = rows[from][to].length;
    rows[from][to] = Arrays.copyOf(rows[from][to], k+2);
    rows[from][to][k] = fromPort;
    rows[from][to][k+1] = toPort;
  }

  /**
   * Try to generate canonical indexing, abort if a cycle gets detected
   */
  <T> ExecutionGraph<T> compileCanonicalForm(List<ExecutionNode<T>> ops) {
    int[] canonicalIndexing = getCanonicalIndexing();
    List<ExecutionNode<T>> canonicalOps = new ArrayList<>(ops.size());
    for (int i = 0; i < rows.length; i++) canonicalOps.add(null);
    AdjacencyMatrix canonicalForm = new AdjacencyMatrix(rows.length);

    for (int i = 0; i < rows.length; i++) {
      int k = rows.length-1-canonicalIndexing[i];
      canonicalOps.set(k, ops.get(i));
      for (int j = 0; j < rows.length; j++) {
        int l = rows.length-1-canonicalIndexing[j];
        canonicalForm.rows[k][l] = rows[i][j];
      }
    }

    int[] inDegrees = new int[rows.length];
    int[] outDegrees = new int[rows.length];
    for (int i = 0; i < rows.length; i++) {
      for (int j = 0; j < rows.length; j++) {
        inDegrees[i] += canonicalForm.rows[j][i].length/2;
        for (int k = 0; k < canonicalForm.rows[i][j].length; k+=2) {
          outDegrees[i] = Math.max(outDegrees[i], canonicalForm.rows[i][j][k]+1);
        }
      }
    }

    ExecutionGraph<T> executionGraph = new ExecutionGraph<>(canonicalOps.size());
    for (int i = rows.length - 1; i >= 0; i--) {
      int[] columnRow = new int[2+inDegrees[i]*2];
      columnRow[0] = inDegrees[i];
      columnRow[1] = outDegrees[i];
      for (int j = 0; j < rows.length; j++) {
        int n = canonicalForm.rows[j][i].length;
        for (int k = 0; k < n; k+=2) {
          columnRow[2+canonicalForm.rows[j][i][k+1]*2] = rows.length - j -1;
          columnRow[2+canonicalForm.rows[j][i][k+1]*2+1] = canonicalForm.rows[j][i][k];
        }
      }
      executionGraph.addRow(rows.length - i -1, canonicalOps.get(i), columnRow);
    }
    logger.info("\n{}", executionGraph);
    return executionGraph;
  }

  private int[] getCanonicalIndexing() {
    AdjacencyMatrix a = this;
    int[] maxPathLength = new int[rows.length];
    int[] canonicalIndexing = new int[rows.length];
    List<Integer> leafs = getLeafs();
    logger.trace(leafs.toString());
    for (int n = 1; n <= rows.length; n++) {
      logger.trace("\n{}",a);
      int[][][] x = a.rows;
      for (int i = 0; i < rows.length; i++) {
        if (x[i][i].length != 0) {
          throw new InvalidExecutionGraphException("The given graph contains a cycle!");
        }
      }
      boolean stop = true;
      for (int leaf : leafs) {
        for (int j = 0; j < rows.length; j++) {
          if (x[leaf][j].length != 0) {
            maxPathLength[j] = n;
            stop = false;
          }
        }
      }
      if (stop) {
        break;
      }
      a = this.matrixProduct(a);
    }
    int k = 0; int l = 0;
    while (k < rows.length && l < rows.length) {
      for (int i = 0; i < rows.length; i++) {
        if (maxPathLength[i] == k) {
          canonicalIndexing[i] = l;
          l++;
        }
      }
      k++;
    }
    logger.trace("\n{}",Arrays.toString(maxPathLength));
    logger.trace("\n{}",Arrays.toString(canonicalIndexing));
    return canonicalIndexing;
  }

  private AdjacencyMatrix matrixProduct(AdjacencyMatrix other) {
    AdjacencyMatrix result = new AdjacencyMatrix(rows.length);
    int[][][] a = rows;
    int[][][] b = other.rows;
    int[][][] c = result.rows;
    for (int i = 0; i < c.length; i++) {
      for (int j = 0; j < c.length; j++) {
        for (int k = 0; k < c.length; k++) {
          if ((a[i][k].length & b[k][j].length) != 0) {
            c[i][j] = new int[2];
            break;
          }
        }
      }
    }
    return result;
  }

  private List<Integer> getLeafs() {
    List<Integer> leafs = new ArrayList<>();
    for (int i = 0; i < rows.length; i++) {
      boolean leaf = true;
      for (int j = 0; j < rows.length; j++) {
        if (rows[j][i].length != 0) {
          leaf = false;
          break;
        }
      }
      if (leaf) {
        leafs.add(i);
      }
    }
    return leafs;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < rows.length; i++) {
      for (int j = 0; j < rows.length; j++) {
        sb.append(rows[i][j].length/2);
        sb.append(" ");
      }
      sb.append("\n");
    }
    return sb.toString();
  }


}
