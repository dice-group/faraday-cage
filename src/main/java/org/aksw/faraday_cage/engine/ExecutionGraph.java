package org.aksw.faraday_cage.engine;

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

  public ExecutionGraph addEdge(ExecutionNode<T> from, int fromPort, ExecutionNode<T> to, int toPort) {
    if (!edges.containsKey(from)) {
      edges.put(from, new ArrayList<>());
    }
    edges.get(from).add(new Edge<>(fromPort, toPort, to));
    return this;
  }

  public CompiledExecutionGraph compile() {
    return new ExecutionGraphCompiler<>(edges).compile(FaradayCageContext.newRunId());
  }

  CompiledExecutionGraph compile(String runId) {
    FaradayCageContext.setRunId(runId);
    return new ExecutionGraphCompiler<>(edges).compile(runId);
  }

}
