package org.aksw.faraday_cage.rdf.config;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 *
 *
 *
 */
public class Vocabulary {

  private static String uri = "http://aksw.org/faraday_cage/#";

  public static Resource executionNode() {
    return resource("ExecutionNode");
  }

  public static Resource parametrizedExecutionNode() {
    return resource("ParametrizedExecutionNode");
  }

  public static Property implementedIn() {
    return property("implementedIn");
  }

  public static Property hasInput() {
    return property("hasInput");
  }

  public static Property hasOutput() {
    return property("hasOutput");
  }

  public static Property property(String name) {
    return ResourceFactory.createProperty(getURI() + name);
  }

  public static Resource resource(String local) {
    return ResourceFactory.createResource(getURI() + local);
  }

  public static String getURI() {
    return Vocabulary.uri;
  }

  public static void setURI(String uri) {
    Vocabulary.uri = uri;
  }

}
