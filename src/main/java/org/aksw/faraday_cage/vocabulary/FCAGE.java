package org.aksw.faraday_cage.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for http://w3id.org/fcage/
 */
public class FCAGE {

  public static String NS = "http://w3id.org/fcage/";

  public static String PREFIX = "fcage";

  public static Property hasInput = ResourceFactory.createProperty(NS + "hasInput");

  public static Property hasOutput = ResourceFactory.createProperty(NS + "hasOutput");

  public static Property toNode = ResourceFactory.createProperty(NS + "toNode");

  public static Property toPort = ResourceFactory.createProperty(NS + "toPort");

  public static Property decoratedBy = ResourceFactory.createProperty(NS + "decoratedBy");

  public static Resource ExecutionNode = ResourceFactory.createResource(NS + "ExecutionNode");

  public static Resource ExecutionNodeWrapper = ResourceFactory.createResource(NS + "ExecutionNodeWrapper");

  public static String getURI() {
    return NS;
  }

}
