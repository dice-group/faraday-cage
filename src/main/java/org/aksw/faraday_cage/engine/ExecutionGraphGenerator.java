package org.aksw.faraday_cage.engine;

import org.aksw.faraday_cage.vocabulary.FCAGE;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generate an ExecutionGraph from a specification in RDF.
 *
 * Nodes may be connected in three possible ways:
 * output-only port-explicit, output-input port-implicit, output-only single-implicit
 *
 */
class ExecutionGraphGenerator {

  private static final Logger logger = LoggerFactory.getLogger(ExecutionGraphGenerator.class);

  static <T> ExecutionGraph<T> generate(Model configGraph, PluginFactory<? extends ExecutionNode<T>> factory) {
    Map<Resource, ExecutionNode<T>> executionGraphNodeMap = new HashMap<>();
    // fill executionGraphNodeMap with mappings from plugin ids to implementations using the factory
    ModelFactory.createInfModel(ReasonerRegistry.getTransitiveReasoner(), configGraph)
      .listStatements(null, RDF.type, (RDFNode) null)
      .filterKeep(stmt -> stmt.getObject().asResource().hasProperty(RDFS.subClassOf, FCAGE.ExecutionNode))
      .mapWith(Statement::getSubject)
      .forEachRemaining(nodeResource -> {
        ExecutionNode<T> node = factory.create(nodeResource);
        if (node instanceof Parameterized) {
          ValidatableParameterMap parameterMap = ((Parameterized) node).createParameterMap();
          parameterMap.populate(nodeResource);
          parameterMap.init();
          ((Parameterized) node).initParameters(parameterMap);
        }
        executionGraphNodeMap.put(nodeResource, node);
      });
    ExecutionGraph<T> executionGraph = new ExecutionGraph<>();
    configGraph.listStatements(null, FCAGE.hasOutput,(RDFNode) null).forEachRemaining(stmt -> {
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

      generateEdgesForResource(executionGraph, s, targets, executionGraphNodeMap);
    });
    return executionGraph;
  }


  private static  <T> void generateEdgesForResource(ExecutionGraph<T> executionGraph, Resource node, List<Resource> targets, Map<Resource, ExecutionNode<T>> executionGraphNodeMap) {
    AtomicInteger i = new AtomicInteger(0);
    Map<Pair<Resource, Resource>, Integer> lastToPortMap = new HashMap<>();
    targets.forEach(r -> {
      if (r.isAnon() && r.hasProperty(FCAGE.toNode) && r.hasProperty(FCAGE.toPort)) {
        try {
          int toPort = r.getProperty(FCAGE.toPort).getInt();
          Resource toNode = r.getProperty(FCAGE.toNode).getResource();
          executionGraph.addEdge(executionGraphNodeMap.get(node), i.getAndIncrement(), executionGraphNodeMap.get(toNode), toPort);
        } catch (LiteralRequiredException | NumberFormatException e) {
          throw new RuntimeException("Error in definition of " + node + "! Invalid value \"" + r.getProperty(FCAGE.toPort).getObject() + "\" for " + FCAGE.toPort + ", allowed range is integer literals", e);
        } catch (ResourceRequiredException e) {
          throw new RuntimeException("Error in definition of " + node + "! Invalid value \"" + r.getProperty(FCAGE.toNode).getObject() + "\" for " + FCAGE.toNode + ", allowed range is resources", e);
        }
      } else if (r.hasProperty(FCAGE.hasInput)) {
        Resource inputs = r.getPropertyResourceValue(FCAGE.hasInput);
        int toPort = 0;
        if (inputs.canAs(RDFList.class)) {
          ImmutablePair<Resource, Resource> con = new ImmutablePair<>(node, r);
          Integer lastToPort = lastToPortMap.getOrDefault(con, 0);
          toPort = inputs.as(RDFList.class).indexOf(node, lastToPort);
          if (toPort == -1) {
            throw new RuntimeException("Could not find an occurrence of " + node + " in input declaration " + r);
          }
          lastToPortMap.put(con, toPort+1);
        }
        executionGraph.addEdge(executionGraphNodeMap.get(node), i.getAndIncrement(), executionGraphNodeMap.get(r), toPort);
      } else {
        executionGraph.addEdge(executionGraphNodeMap.get(node), i.getAndIncrement(), executionGraphNodeMap.get(r), 0);
      }
    });
  }

}
