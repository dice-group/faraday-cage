package org.aksw.faraday_cage.engine;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 *
 */
public class ExecutionGraph<T> {

  private static final Logger logger = LoggerFactory.getLogger(ExecutionGraph.class);

  @NotNull
  private List<ExecutionNode<T>> vertices = new ArrayList<>();

  @NotNull
  private List<List<List<Integer>>> adjacencyMatrix = new ArrayList<>();

  @NotNull
  private Map<ExecutionNode<T>, List<Edge<T>>> edges = new HashMap<>();

  static class Edge<T> {

    private int fromPort;
    private int toPort;
    private ExecutionNode<T> toNode;

    Edge(int fromPort, int toPort, ExecutionNode<T> toNode) {
      this.fromPort = fromPort;
      this.toPort = toPort;
      this.toNode = toNode;
    }

    int getFromPort() {
      return fromPort;
    }

    int getToPort() {
      return toPort;
    }

    ExecutionNode<T> getToNode() {
      return toNode;
    }
  }

  public ExecutionGraph() {

  }

  public void computeEdges() {
    int l = vertices.size();
    for (int i = 0; i < l; i++) {
      ExecutionNode<T> from = vertices.get(i);
      if (!edges.containsKey(from)) {
        edges.put(from, new ArrayList<>());
      }
      for (int j = 0; j < l; j++) {
        ExecutionNode<T> to = vertices.get(j);
        List<Integer> ports = adjacencyMatrix.get(i).get(j);
        for (int k = 0; k < ports.size(); k += 2) {
          int fromPort = ports.get(k);
          int toPort = ports.get(k+1);
          edges.get(from).add(new Edge<>(fromPort, toPort, to));
        }
      }
    }
  }

  @NotNull
  public ExecutionGraph addEdge(ExecutionNode<T> from, int fromPort, ExecutionNode<T> to, int toPort) {
    if (!vertices.contains(from)) {
      createVertex(from);
    }
    int i = vertices.indexOf(from);
    if (!vertices.contains(to)) {
      createVertex(to);
    }
    int j = vertices.indexOf(to);
    adjacencyMatrix.get(i).get(j).add(fromPort);
    adjacencyMatrix.get(i).get(j).add(toPort);
    return this;
  }

  public CompiledExecutionGraph compile() {
    computeEdges();
    return new ExecutionGraphCompiler<>(edges).compile(FaradayCageContext.newRunId());
  }

  CompiledExecutionGraph compile(String runId) {
    computeEdges();
    FaradayCageContext.setRunId(runId);
    return new ExecutionGraphCompiler<>(edges).compile(runId);
  }

  @NotNull
  public ExecutionGraph createVertex(ExecutionNode<T> node) {
    int index = vertices.size();
    vertices.add(node);
    adjacencyMatrix.add(new ArrayList<>());
    for (int i = 0; i <= index; i++) {
      adjacencyMatrix.get(index).add(new ArrayList<>());
    }
    return this;
  }

  /**
   * was brauch ich hier?
   *  getRandomVertex
   *  getRandomSubsequence
   *  merge / branch
   *  io.jenetics:prngine:1.0.1
   */

}
