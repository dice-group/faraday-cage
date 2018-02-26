package org.aksw.faraday_cage;

import com.google.common.collect.Sets;
import org.aksw.faraday_cage.nodes.Node;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.util.*;

import static org.aksw.faraday_cage.util.QueryHelper.forEachResultOf;

/**
 *
 *
 *
 */
public class ExecutionGraphGenerator<T> {

  private Map<Resource, List<Resource>> executionInputs = new HashMap<>();
  private Map<Resource, List<Resource>> executionOutputs = new HashMap<>();
  private HashMap<Resource, IdentifiableExecution<T>> visitedHubs = new HashMap<>();
  private Model configGraph;
  private ExecutionGraphBuilder<T> builder;
  private IdentifiableExecutionFactory<T> factory;
  private Deque<List<Resource>> stack;

  public ExecutionGraphGenerator(Model configGraph, ExecutionGraphBuilder<T> builder,
                                 IdentifiableExecutionFactory<T> factory) {
    this.configGraph = configGraph;
    this.builder = builder;
    this.factory = factory;
    this.stack = new ArrayDeque<>();
  }

  public final ExecutionGraph generate() {
    fillMap(executionInputs, Vocabulary.hasInput());
    fillMap(executionOutputs, Vocabulary.hasOutput());
    Set<Resource> startNodes = getStartNodes();
    startNodes.forEach(node -> stack.push(List.of(node)));
    while (!stack.isEmpty()) {
      List<Resource> pair = stack.pop();
      if (pair.size() == 1) {
        Resource startNode = pair.get(0);
        populateStackAndMarkVisited(startNode);
        if (isHub(startNode)) {
          builder.addStartHub(visitedHubs.get(startNode));
        } else {
          builder.addStart(createAndInitExecution(startNode));
        }
      } else {
        Resource parent = pair.get(0);
        Resource node = pair.get(1);
        populateStackAndMarkVisited(node);
        int outPort = getNodeOutputs(parent).indexOf(node);
        int inPort = getNodeInputs(node).indexOf(parent);
        boolean parentIsHub = isHub(parent);
        boolean nodeIsHub = isHub(node);
        if (parentIsHub && nodeIsHub) {
          IdentifiableExecution<T> uParent = visitedHubs.get(parent);
          IdentifiableExecution<T> uNode = visitedHubs.get(node);
          builder.chainFromHubToHub(uParent, outPort, uNode, inPort);
        } else if (parentIsHub) {
          IdentifiableExecution<T> uParent = visitedHubs.get(parent);
          IdentifiableExecution<T> uNode = createAndInitExecution(node);
          builder.chainFromHub(uParent, outPort, uNode);
        } else if (nodeIsHub) {
          IdentifiableExecution<T> uNode = visitedHubs.get(node);
          builder.chainIntoHub(uNode, inPort);
        } else {
          IdentifiableExecution<T> uNode = createAndInitExecution(node);
          builder.chain(uNode);
        }
      }
    }
    return builder.build();
  }

  private void populateStackAndMarkVisited(Resource node) {
    if (!isHub(node) || !visitedHubs.containsKey(node)) {
      getNodeOutputs(node).forEach(r -> stack.push(List.of(node, r)));
      if (isHub(node)) {
        visitedHubs.put(node, createAndInitExecution(node));
      }
    }
  }

  protected IdentifiableExecution<T> createAndInitExecution(Resource executionId) {
    IdentifiableExecution<T> execution = factory.create(executionId);
    if (execution instanceof Node) {
      ((Node) execution).init(executionId, getNodeInputs(executionId).size(), getNodeOutputs(executionId).size());
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

  protected final boolean isHub(Resource execution) {
    return getNodeInputs(execution).size() > 1 || getNodeOutputs(execution).size() > 1;
  }

  private void fillMap(Map<Resource, List<Resource>> map, Resource searchProperty) {
    Query q = new SelectBuilder()
      .setDistinct(true)
      .addVar("?op").addVar("?list")
      .addWhere("?op", searchProperty, "?list")
      .build();
    forEachResultOf(q, configGraph, (qs) -> map.put(qs.getResource("?op"),
      qs.getResource("?list").as(RDFList.class).mapWith(RDFNode::asResource).toList()));
  }

  protected final List<Resource> getNodeInputs(Resource execution) {
    return executionInputs.getOrDefault(execution, Collections.emptyList());
  }

  protected final List<Resource> getNodeOutputs(Resource execution) {
    return executionOutputs.getOrDefault(execution, Collections.emptyList());
  }

  protected final Set<Resource> getStartNodes() {
    return Sets.difference(executionOutputs.keySet(), executionInputs.keySet());
  }


}
