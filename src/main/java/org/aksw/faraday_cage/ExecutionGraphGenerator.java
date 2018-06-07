package org.aksw.faraday_cage;

import org.aksw.faraday_cage.nodes.Node;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.aksw.faraday_cage.util.QueryHelper.forEachResultOf;

/**
 *
 *
 *
 */
public class ExecutionGraphGenerator<T> {

  private static class Connection {
    private int fromPort;
    private int toPort;
    private Resource toNode;

    private Connection(int fromPort, int toPort, Resource toNode) {
      this.fromPort = fromPort;
      this.toPort = toPort;
      this.toNode = toNode;
    }

  }

  private Model configGraph;
  private ExecutionGraphBuilder<T> builder;
  private ExecutionFactory<T> factory;
  private Map<Resource, List<Connection>> connections;
  private Map<Resource, int[]> degrees;
  private HashMap<Resource, Execution<T>> visitedHubs = new HashMap<>();

  public ExecutionGraphGenerator(Model configGraph, ExecutionGraphBuilder<T> builder,
                                 ExecutionFactory<T> factory) {
    this.configGraph = configGraph;
    this.builder = builder;
    this.factory = factory;
    this.connections = buildConnections(Vocabulary.hasOutput());
    this.degrees = getDegrees();
  }

  public final ExecutionGraph generate() {
    final Set<Resource> startNodes = getStartNodes();
    startNodes.forEach(node -> dfs(new Connection(0, 0, node), null, new ArrayDeque<>()));
    return builder.build();
  }

  private void dfs(Connection connection, Resource parent, final Deque<Resource> recStack) {
    final Resource node = connection.toNode;
    recStack.push(node);
    getConnections(node).forEach(next -> {
      if (recStack.contains(next.toNode)) {
        throw new RuntimeException("Cyclic Graph detected! Cycle in [" +
          StreamSupport.stream(((Iterable<Resource>) recStack::descendingIterator).spliterator(), false)
            .map(Resource::getLocalName)
            .reduce(":", (a, b) -> a + b + ", :") + next.toNode.getLocalName() + "]");
      }
    });
    boolean recur = !isHub(node);
    if (isHub(node) && !visitedHubs.containsKey(node)) {
      visitedHubs.put(node, createAndInitExecution(node));
      recur = true;
    }
    if (parent == null) {
      if (isHub(node)) {
        builder.addStartHub(visitedHubs.get(node));
      } else {
        builder.addStart(createAndInitExecution(node));
      }
    } else {
      final int fromPort = connection.fromPort;
      final int toPort = connection.toPort;
      final boolean parentIsHub = isHub(parent);
      final boolean nodeIsHub = isHub(node);
      if (parentIsHub && nodeIsHub) {
        Execution<T> uParent = visitedHubs.get(parent);
        Execution<T> uNode = visitedHubs.get(node);
        builder.chainFromHubToHub(uParent, fromPort, uNode, toPort);
      } else if (parentIsHub) {
        Execution<T> uParent = visitedHubs.get(parent);
        Execution<T> uNode = createAndInitExecution(node);
        builder.chainFromHub(uParent, fromPort, uNode);
      } else if (nodeIsHub) {
        Execution<T> uNode = visitedHubs.get(node);
        builder.chainIntoHub(uNode, toPort);
      } else {
        Execution<T> uNode = createAndInitExecution(node);
        builder.chain(uNode);
      }
    }
    if (recur) {
      getConnections(node).forEach(r -> dfs(r, node, recStack));
    }
    recStack.pop();
  }

  //todo: probably belongs somewhere else...
  private Execution<T> createAndInitExecution(Resource executionId) {
    Execution<T> execution = factory.create(executionId);
    if (execution instanceof Node) {
      int[] d = degrees.get(executionId);
      ((Node) execution).init(executionId, d[0], d[1]);
    } else if (execution instanceof Plugin) {
      ((Plugin) execution).init(executionId);
    }
    if (execution instanceof Parametrized) {
      ParameterMap parameterMap = ((Parametrized) execution).createParameterMap();
      parameterMap.init(executionId);
      ((Parametrized) execution).init(parameterMap);
    }
    return execution;
  }

  private boolean isHub(Resource execution) {
    int[] d = degrees.get(execution);
    return d[0] > 1 || d[1] > 1;
  }

  private List<Connection> getConnections(Resource execution) {
    return connections.getOrDefault(execution, Collections.emptyList());
  }

  private Set<Resource> getStartNodes() {
    return degrees.entrySet().stream()
      .filter(e -> e.getValue()[0] == 0)
      .map(Map.Entry::getKey)
      .collect(Collectors.toSet());
  }

  private Map<Resource, int[]> getDegrees() {
    Map<Resource, int[]> degrees = new HashMap<>();
    connections.forEach((key, value) -> degrees.put(key, new int[]{0, value.size()}));
    new ArrayList<>(connections.entrySet()).stream()
      .flatMap(e -> e.getValue().stream())
      .map(c -> c.toNode)
      .forEach(p -> {
        if (!degrees.containsKey(p)) {
          degrees.put(p, new int[]{1, 0});
        } else {
          degrees.get(p)[0] = degrees.get(p)[0]+1;
        }
      });
    return degrees;
  }

  private Map<Resource, List<Connection>> buildConnections(Resource searchProperty) {
    Map<Resource, List<Connection>> connectionsMap = new HashMap<>();
    Query q = new SelectBuilder()
      .setDistinct(true)
      .addVar("?op").addVar("?list")
      .addWhere("?op", searchProperty, "?list")
      .build();
    forEachResultOf(q, configGraph, (qs) -> {
      List<Resource> base;
      Resource op = qs.getResource("?op");
      if (qs.getResource("?list").canAs(RDFList.class)) {
        base = qs.getResource("?list").as(RDFList.class).iterator()
          .filterKeep(RDFNode::isResource)
          .mapWith(RDFNode::asResource)
          .toList();
      } else {
        base = List.of(qs.getResource("?list"));
      }
      AtomicInteger i = new AtomicInteger(0);
      Map<Pair<Resource, Resource>, Integer> lastToPortMap = new HashMap<>();
      List<Connection> connections = base.stream().map(r -> {
        if (r.isAnon() && r.hasProperty(Vocabulary.toNode()) && r.hasProperty(Vocabulary.toPort())) {
          try {
            int toPort = r.getProperty(Vocabulary.toPort()).getInt();
            Resource toNode = r.getProperty(Vocabulary.toNode()).getResource();
            return new Connection(i.getAndIncrement(), toPort, toNode);
          } catch (LiteralRequiredException | NumberFormatException e) {
            throw new RuntimeException("Error in definition of " + op + "! Invalid value \"" + r.getProperty(Vocabulary.toPort()).getObject() + "\" for " + Vocabulary.toPort() + ", allowed range is integer literals", e);
          } catch (ResourceRequiredException e) {
            throw new RuntimeException("Error in definition of " + op + "! Invalid value \"" + r.getProperty(Vocabulary.toNode()).getObject() + "\" for " + Vocabulary.toNode() + ", allowed range is resources", e);
          }
        } else if (r.hasProperty(Vocabulary.hasInput())) {
          Resource inputs = r.getPropertyResourceValue(Vocabulary.hasInput());
          int toPort = 0;
          if (inputs.canAs(RDFList.class)) {
            ImmutablePair<Resource, Resource> con = new ImmutablePair<>(op, r);
            Integer lastToPort = lastToPortMap.getOrDefault(con, 0);
            toPort = inputs.as(RDFList.class).indexOf(op, lastToPort);
            lastToPortMap.put(con, toPort+1);
            if (toPort == -1) {
              throw new RuntimeException("Could not find " + op + " in input declaration " + r);
            }
          }
          return new Connection(i.getAndIncrement(), toPort, r);
        } else {
          return new Connection(i.getAndIncrement(), 0, r);
        }
      }).collect(Collectors.toList());
      connectionsMap.put(op, connections);
    });
    validateConnections(connectionsMap);
    return connectionsMap;
  }

  private void validateConnections(Map<Resource, List<Connection>> connectionsMap) {
    HashMap<Resource, SortedSet<Integer>> inPorts = new HashMap<>();
    connectionsMap.values().stream()
      .flatMap(Collection::stream)
      .forEach(c -> {
        SortedSet<Integer> set = (inPorts.containsKey(c.toNode)) ? inPorts.get(c.toNode) : new TreeSet<>();
        set.add(c.toPort);
        inPorts.put(c.toNode, set);
      });
    inPorts.forEach((k, v) -> {
      int j = -1;
      for (Integer i : v) {
        if (i != ++j) {
          throw new RuntimeException("Error in " + k + ": missing port #" + j + "! There were " + v.size() + " ports declared.");
        }
      }
    });
  }


}
