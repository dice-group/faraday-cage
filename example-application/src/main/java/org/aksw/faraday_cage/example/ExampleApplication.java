package org.aksw.faraday_cage.example;

import org.aksw.faraday_cage.engine.FaradayCageContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.pf4j.DefaultPluginManager;

/**
 * Example Application main class.
 * Defines namespaces, utility methods and the {@code FaradayCageContext}.
 */
public class ExampleApplication {

  public static final String NS = "urn:example:example-application/";

  public static Resource createResource(String localName) {
    return ResourceFactory.createResource(NS + localName);
  }

  public static Property createProperty(String localName) {
    return ResourceFactory.createProperty(NS + localName);
  }

  public static void main(String[] args) {
    FaradayCageContext.of(StringOperation.class, StringOperationWrapper.class, new DefaultPluginManager())
    .run(RDFDataMgr.loadModel(args[0]));
  }

}
