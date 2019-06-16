//package org.aksw.faraday_cage.engine.test;
//
//import org.aksw.faraday_cage.engine.InvalidExecutionGraphException;
//import org.apache.jena.rdf.model.Resource;
//import org.apache.jena.rdf.model.ResourceFactory;
//import org.junit.Test;
//
//import java.util.Arrays;
//
//import static org.junit.Assert.assertTrue;
//
///**
// * For the sake of brevity we treat ExecutionGraphGenerator, ExecutionGraph and ExecutionGraphCompiler as a unit here.
// * Tries to test all cases of invalid ExecutionGraphs
// */
//public class ExecutionGraphCompilerTest {
//
//  private static Resource createResource(String localName) {
//    return ResourceFactory.createResource("urn:example/ExecutionGraphCompilerTest#" + localName);
//  }
//
//  @Test
//  public void cyclicExecutionGraphsShouldThrowException() {
//    TestExecutionNode ex1 = new TestExecutionNode();
//    ex1.initPluginId(createResource("ex1"));
//    TestExecutionNode ex2 = new TestExecutionNode();
//    ex2.initPluginId(createResource("ex2"));
//    TestExecutionNode ex3 = new TestExecutionNode();
//    ex3.initPluginId(createResource("ex3"));
//    ExecutionGraph<String> executionGraph = new ExecutionGraph<>();
////    executionGraph.addEdge(ex1, 0, ex2, 0);
////    executionGraph.addEdge(ex2, 0, ex3, 0);
////    executionGraph.addEdge(ex3, 0, ex2, 1);
//    assertTrue("The appropriate RuntimeException should be thrown", testConfigForRuntimeException(executionGraph, "Cyclic Graph detected!"));
//  }
//
//  @Test
//  public void rootlessExecutionGraphsShouldThrowException() {
//    TestExecutionNode ex1 = new TestExecutionNode();
//    TestExecutionNode ex2 = new TestExecutionNode();
//    ExecutionGraph<String> executionGraph = new ExecutionGraph<>();
////    executionGraph.addEdge(ex1, 0, ex2, 0);
////    executionGraph.addEdge(ex2, 0, ex1, 0);
//    assertTrue("The appropriate RuntimeException should be thrown", testConfigForRuntimeException(executionGraph, "No root"));
//  }
//
//  private boolean testConfigForRuntimeException(ExecutionGraph<String> executionGraph, String...matchStrings) {
//    boolean pass = false;
//    try {
//      executionGraph.compile();
//    } catch (InvalidExecutionGraphException e) {
//      pass = Arrays.stream(matchStrings).anyMatch(s -> e.getMessage().contains(s));
//      if (!pass) {
//        throw e;
//      }
//    }
//    return pass;
//  }
//
//}
