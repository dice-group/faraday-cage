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

  private final short[][][] rows;

  AdjacencyMatrix(int n) {
    rows = new short[n][n][0];
  }

  AdjacencyMatrix(AdjacencyMatrix other) {
    rows = other.rows;
  }

  void addEdge(int from, int fromPort, int to, int toPort) {
    short k = (short) rows[from][to].length;
    rows[from][to] = Arrays.copyOf(rows[from][to], k+2);
    rows[from][to][k] = (short) fromPort;
    rows[from][to][k+1] = (short) toPort;
  }

  /**
   * Try to generate canonical indexing, abort if a cycle gets detected
   */
  <T> ExecutionGraph<T> compileCanonicalForm(List<ExecutionNode<T>> ops) {
    short[] canonicalIndexing = getCanonicalIndexing();
    List<ExecutionNode<T>> canonicalOps = new ArrayList<>(ops.size());
    for (int i = 0; i < rows.length; i++) canonicalOps.add(null);
    AdjacencyMatrix canonicalForm = new AdjacencyMatrix(rows.length);

    for (short i = 0; i < rows.length; i++) {
      short k = (short) (rows.length-1-canonicalIndexing[i]);
      canonicalOps.set(k, ops.get(i));
      for (short j = 0; j < rows.length; j++) {
        short l = (short) (rows.length-1-canonicalIndexing[j]);
        canonicalForm.rows[k][l] = rows[i][j];
      }
    }

    short[] inDegrees = new short[rows.length];
    short[] outDegrees = new short[rows.length];
    for (short i = 0; i < rows.length; i++) {
      for (short j = 0; j < rows.length; j++) {
        inDegrees[i] += canonicalForm.rows[j][i].length/2;
        for (int k = 0; k < canonicalForm.rows[i][j].length; k+=2) {
          outDegrees[i] = (short) Math.max(outDegrees[i], canonicalForm.rows[i][j][k]+1);
        }
      }
    }

    ExecutionGraph<T> executionGraph = new ExecutionGraph<>(canonicalOps);
    for (short i = 0; i < rows.length; i++) {
      short[] columnRow = new short[2+inDegrees[i]*2];
      columnRow[0] = inDegrees[i];
      columnRow[1] = outDegrees[i];
      for (short j = 0; j < rows.length; j++) {
        short n = (short) canonicalForm.rows[j][i].length;
        for (short k = 0; k < n; k+=2) {
          columnRow[2+canonicalForm.rows[j][i][k+1]*2] = j;
          columnRow[2+canonicalForm.rows[j][i][k+1]*2+1] = canonicalForm.rows[j][i][k];
        }
      }
      executionGraph.addRow(i, columnRow);
    }
    logger.info("\n{}", executionGraph);
    return executionGraph;
  }

  private short[] getCanonicalIndexing() {
    AdjacencyMatrix a = this;
    short[] maxPathLength = new short[rows.length];
    short[] canonicalIndexing = new short[rows.length];
    List<Short> leafs = getLeafs();
    logger.trace(leafs.toString());
    for (short n = 1; n <= rows.length; n++) {
      logger.trace("\n{}",a);
      short[][][] x = a.rows;
      for (short i = 0; i < rows.length; i++) {
        if (x[i][i].length != 0) {
          throw new InvalidExecutionGraphException("The given graph contains a cycle!");
        }
      }
      boolean stop = true;
      for (short leaf : leafs) {
        for (short j = 0; j < rows.length; j++) {
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
    short k = 0; short l = 0;
    while (k < rows.length && l < rows.length) {
      for (short i = 0; i < rows.length; i++) {
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
    short[][][] a = rows;
    short[][][] b = other.rows;
    short[][][] c = result.rows;
    for (short i = 0; i < c.length; i++) {
      for (short j = 0; j < c.length; j++) {
        for (short k = 0; k < c.length; k++) {
          if ((a[i][k].length & b[k][j].length) != 0) {
            c[i][j] = new short[2];
            break;
          }
        }
      }
    }
    return result;
  }

  private List<Short> getLeafs() {
    List<Short> leafs = new ArrayList<>();
    for (short i = 0; i < rows.length; i++) {
      boolean leaf = true;
      for (short j = 0; j < rows.length; j++) {
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
    for (short i = 0; i < rows.length; i++) {
      for (short j = 0; j < rows.length; j++) {
        sb.append(rows[i][j].length/2);
        sb.append(" ");
      }
      sb.append("\n");
    }
    return sb.toString();
  }


}