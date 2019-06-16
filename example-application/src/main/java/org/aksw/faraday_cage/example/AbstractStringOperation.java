package org.aksw.faraday_cage.example;

import org.aksw.faraday_cage.engine.AbstractParameterizedExecutionNode;
import org.apache.jena.rdf.model.Resource;

/**
 *
 */
public abstract class AbstractStringOperation extends AbstractParameterizedExecutionNode<String> implements StringOperation {

  @Override
  protected String deepCopy(String data) {
    // String in Java is immutable, so no need to make a deep copy
    return data;
  }

  @Override
  public Resource getType() {
    return ExampleApplication.createResource(this.getClass().getSimpleName());
  }
}
