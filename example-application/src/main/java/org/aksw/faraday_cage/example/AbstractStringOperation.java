package org.aksw.faraday_cage.example;

import org.aksw.faraday_cage.engine.AbstractParameterizedExecutionGraphNode;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public abstract class AbstractStringOperation extends AbstractParameterizedExecutionGraphNode<String> implements StringOperation {

  @Override
  protected String deepCopy(String data) {
    // String in Java is immutable, so no need to make a deep copy
    return data;
  }

  @NotNull
  @Override
  public Resource getType() {
    return ExampleApplication.createResource(this.getClass().getSimpleName());
  }
}
