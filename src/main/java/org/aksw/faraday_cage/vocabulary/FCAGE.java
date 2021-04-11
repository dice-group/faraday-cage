package org.aksw.faraday_cage.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for https://w3id.org/fcage/
 */
public class FCAGE {

  public static String NS = "https://w3id.org/fcage/";

  public static String PREFIX = "fcage";

  public static Property hasInput = ResourceFactory.createProperty(NS + "hasInput");

  public static Property fromNode = ResourceFactory.createProperty(NS + "fromNode");

  public static Property fromPort = ResourceFactory.createProperty(NS + "fromPort");

  public static Property decoratedBy = ResourceFactory.createProperty(NS + "decoratedBy");

  public static Resource ExecutionNode = ResourceFactory.createResource(NS + "ExecutionNode");

  public static Resource ExecutionNodeWrapper = ResourceFactory.createResource(NS + "ExecutionNodeWrapper");

  public static String getURI() {
    return NS;
  }

}
