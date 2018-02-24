package org.aksw.faraday_cage;

import com.google.common.collect.Sets;
import org.aksw.faraday_cage.execution.Execution;
import org.aksw.faraday_cage.execution.HubExecution;
import org.aksw.faraday_cage.execution.graph.ExecutionGraph;
import org.aksw.faraday_cage.execution.graph.ExecutionGraphBuilder;
import org.aksw.faraday_cage.plugin.Identifiable;
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
public abstract class AbstractExecutionGraphGenerator<U extends Execution<T> & Identifiable, V extends HubExecution<T> & Identifiable, T> {

  private Map<Resource, List<Resource>> executionInputs = new HashMap<>();
  private Map<Resource, List<Resource>> executionOutputs = new HashMap<>();
  private Model configGraph;
  private ExecutionGraphBuilder<U, V> builder;

  public AbstractExecutionGraphGenerator(Model configGraph, ExecutionGraphBuilder<U, V> builder) {
    this.configGraph = configGraph;
    this.builder = builder;
  }

  public final ExecutionGraph generate() {
    fillMap(executionInputs, Vocabulary.hasInput());
    fillMap(executionOutputs, Vocabulary.hasOutput());
    Set<Resource> startNodes = getStartNodes();
    Deque<List<Resource>> stack = new ArrayDeque<>();
    for (Resource startNode : startNodes) {
      if (isHub(startNode)) {
        builder.addStartHub(createAndInitHubExecution(startNode));
      } else {
        builder.addStart(createAndInitExecution(startNode));
      }
      getNodeOutputs(startNode).forEach(r -> stack.push(List.of(startNode, r)));
    }
    while (!stack.isEmpty()) {
      List<Resource> pair = stack.pop();
      Resource parent = pair.get(0);
      Resource node = pair.get(1);
      int outPort = executionOutputs.get(parent).indexOf(node);
      int inPort = executionInputs.get(node).indexOf(parent);
      if (isHub(parent) && isHub(node)) {
        V vParent = createAndInitHubExecution(parent);
        V vNode = createAndInitHubExecution(node);
        builder.chainFromHubToHub(vParent, outPort, vNode, inPort);
      } else if (isHub(parent)) {
        V vParent = createAndInitHubExecution(parent);
        U uNode = createAndInitExecution(node);
        builder.chainFromHub(vParent, outPort, uNode);
      } else if (isHub(node)) {
        V vNode = createAndInitHubExecution(node);
        builder.chainIntoHub(vNode, inPort);
      } else {
        U uNode = createAndInitExecution(node);
        builder.chain(uNode);
      }
      getNodeOutputs(node).forEach(r -> stack.push(List.of(node, r)));
    }
    return builder.build();
  }

  protected abstract U createAndInitExecution(Resource executionId);

  protected abstract V createAndInitHubExecution(Resource hubExecutionId);

  protected final boolean isHub(Resource execution) {
    return executionInputs.get(execution).size() > 1 || executionOutputs.get(execution).size() > 1;
  }

  //@todo: implement logic for multiple links between two hubs
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
