package org.aksw.faraday_cage.util;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.system.StreamOps;
import org.apache.jena.riot.system.StreamRDF2;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.riot.system.StreamRowRDF;
import org.apache.jena.util.FileUtils;
import org.junit.Test;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.validation.ValidationUtil;

import static org.junit.Assert.assertTrue;

/**
 *
 *
 *
 */
public class SHACLTest {

  @Test
  public void main() {

    // Load the main data model
    Model dataModel = JenaUtil.createMemoryModel();
    dataModel.read(SHACLTest.class.getResourceAsStream("/shacl/test/data.ttl"), null, FileUtils.langTurtle);
    Model shModel = JenaUtil.createMemoryModel();
    shModel.read(SHACLTest.class.getResourceAsStream("/shacl/test/sh.ttl"), null, FileUtils.langTurtle);
    // Perform the validation of everything, using the data model
    // also as the shapes model - you may have them separated
    Resource report = ValidationUtil.validateModel(dataModel, shModel, true);
    boolean conforms = report.getProperty(report.getModel().getProperty("http://www.w3.org/ns/shacl#conforms")).getObject().asLiteral().getBoolean();
    // Print violations
    System.out.println(ModelPrinter.get().print(report.getModel()));
    StreamOps ops;
    StreamRDFLib streamRDFLib;
    StreamRDF2 streamRDF2;
    StreamRowRDF streamRowRDF;
    assertTrue("The data model must conform to the shapes model", conforms);
  }

}