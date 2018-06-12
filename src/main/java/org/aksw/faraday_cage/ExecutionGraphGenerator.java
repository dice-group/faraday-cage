package org.aksw.faraday_cage;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 *
 *
 */
public class ExecutionGraphGenerator {

  private Model configGraph;
  private ExecutionGraph executionGraph;
  private static final Logger logger = LoggerFactory.getLogger(ExecutionGraphGenerator.class);

  public ExecutionGraphGenerator(Model configGraph) {
    this.configGraph = configGraph;
  }

  public ExecutionGraph generate() {
    executionGraph = new ExecutionGraph();
    configGraph.listStatements(null, Vocabulary.hasOutput(),(RDFNode) null).forEachRemaining(stmt -> {
      List<Resource> targets;
      Resource s = stmt.getSubject();
      Resource o = stmt.getObject().asResource();
      if (o.canAs(RDFList.class)) {
        targets = o.as(RDFList.class).iterator()
          .filterKeep(RDFNode::isResource)
          .mapWith(RDFNode::asResource)
          .toList();
      } else {
        targets = List.of(o);
      }
      generateEdgesForResource(s, targets);
    });
    return executionGraph;
  }

  private void generateEdgesForResource(Resource node, List<Resource> targets) {
    AtomicInteger i = new AtomicInteger(0);
    Map<Pair<Resource, Resource>, Integer> lastToPortMap = new HashMap<>();
    targets.forEach(r -> {
      if (r.isAnon() && r.hasProperty(Vocabulary.toNode()) && r.hasProperty(Vocabulary.toPort())) {
        try {
          int toPort = r.getProperty(Vocabulary.toPort()).getInt();
          Resource toNode = r.getProperty(Vocabulary.toNode()).getResource();
          executionGraph.addEdge(node, i.getAndIncrement(), toNode, toPort);
        } catch (LiteralRequiredException | NumberFormatException e) {
          throw new RuntimeException("Error in definition of " + node + "! Invalid value \"" + r.getProperty(Vocabulary.toPort()).getObject() + "\" for " + Vocabulary.toPort() + ", allowed range is integer literals", e);
        } catch (ResourceRequiredException e) {
          throw new RuntimeException("Error in definition of " + node + "! Invalid value \"" + r.getProperty(Vocabulary.toNode()).getObject() + "\" for " + Vocabulary.toNode() + ", allowed range is resources", e);
        }
      } else if (r.hasProperty(Vocabulary.hasInput())) {
        Resource inputs = r.getPropertyResourceValue(Vocabulary.hasInput());
        int toPort = 0;
        if (inputs.canAs(RDFList.class)) {
          ImmutablePair<Resource, Resource> con = new ImmutablePair<>(node, r);
          Integer lastToPort = lastToPortMap.getOrDefault(con, 0);
          toPort = inputs.as(RDFList.class).indexOf(node, lastToPort);
          lastToPortMap.put(con, toPort+1);
          if (toPort == -1) {
            throw new RuntimeException("Could not find " + node + " in input declaration " + r);
          }
        }
        executionGraph.addEdge(node, i.getAndIncrement(), r, toPort);
      } else {
        executionGraph.addEdge(node, i.getAndIncrement(), r, 0);
      }
    });
  }



}
