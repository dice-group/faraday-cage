package org.aksw.faraday_cage.engine;

import org.aksw.faraday_cage.vocabulary.FCAGE;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generate an ExecutionGraph from a specification in RDF.
 *
 */
class ExecutionGraphGenerator {

  private static final Logger logger = LoggerFactory.getLogger(ExecutionGraphGenerator.class);

  static <T> ExecutionGraph<T> generate(Model configGraph, PluginFactory<? extends ExecutionNode<T>> factory) {
    Map<Resource, Integer> executionNodeMap = new HashMap<>();
    List<ExecutionNode<T>> executionNodes = new ArrayList<>();
    // fill ExecutionNodeMap with mappings from plugin ids to implementations using the factory
    Set<Resource> resources = ModelFactory.createInfModel(ReasonerRegistry.getTransitiveReasoner(), configGraph)
      .listStatements(null, RDF.type, (RDFNode) null)
      .filterKeep(stmt -> stmt.getObject().asResource().hasProperty(RDFS.subClassOf, FCAGE.ExecutionNode))
      .mapWith(Statement::getSubject)
      .toSet();
    resources
      .forEach(nodeResource -> {
        ExecutionNode<T> node = factory.create(nodeResource);
        if (node instanceof Parameterized) {
          ValidatableParameterMap parameterMap = ((Parameterized) node).createParameterMap();
          parameterMap.populate(nodeResource);
          parameterMap.init();
          ((Parameterized) node).initParameters(parameterMap);
        }
        int i = executionNodes.size();
        executionNodes.add(node);
        executionNodeMap.put(nodeResource, i);
      });
    AdjacencyMatrix adj = new AdjacencyMatrix(executionNodes.size());
    resources.stream()
      .filter(s -> s.hasProperty(FCAGE.hasInput))
      .forEach(s -> {
        int toNode = executionNodeMap.get(s);
        Resource o = s.getProperty(FCAGE.hasInput).getObject().asResource();
        if (o.canAs(RDFList.class)) {
          AtomicInteger i = new AtomicInteger(0);
          o.as(RDFList.class).iterator()
            .filterKeep(RDFNode::isResource)
            .mapWith(RDFNode::asResource)
            .forEachRemaining(r -> {
              if (r.isAnon()) {
                try {
                  int fromPort = r.getProperty(FCAGE.fromPort).getInt();
                  int fromNode = executionNodeMap.get(r.getProperty(FCAGE.fromNode).getResource());
                  adj.addEdge(fromNode, fromPort, toNode, i.getAndIncrement());
                } catch (LiteralRequiredException | NumberFormatException e) {
                  throw new RuntimeException("Error in definition of " + s + "! Invalid value \"" + r.getProperty(FCAGE.fromPort).getObject() + "\" for " + FCAGE.fromPort + ", allowed range is integer literals", e);
                } catch (ResourceRequiredException e) {
                  throw new RuntimeException("Error in definition of " + s + "! Invalid value \"" + r.getProperty(FCAGE.fromNode).getObject() + "\" for " + FCAGE.fromPort + ", allowed range is resources", e);
                }
              } else {
                adj.addEdge(executionNodeMap.get(r), 0, executionNodeMap.get(s), i.getAndIncrement());
              }
            });
        } else {
          adj.addEdge(executionNodeMap.get(o), 0, executionNodeMap.get(s), 0);
        }
      });
    return adj.compileCanonicalForm(executionNodes);
  }

}
