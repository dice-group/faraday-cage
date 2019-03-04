package org.aksw.faraday_cage.engine.test;

import org.aksw.faraday_cage.engine.AbstractParameterizedExecutionGraphNode;
import org.aksw.faraday_cage.engine.ExecutionGraphNode;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.aksw.faraday_cage.vocabulary.FCAGE;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 *
 */
@Extension
public class TestExecutionGraphNode extends AbstractParameterizedExecutionGraphNode<String> implements TestUtil.ITestExecutionGraphNode {

  @Override
  protected List<String> safeApply(List<String> data) {
    if (data == null) {
      return List.of("Hello Sir!");
    }
    UnaryOperator<String> op = s -> s + "\nOh and Hello to you, Sir!";
    return ExecutionGraphNode.toMultiExecution(op).apply(data);
  }

  @Override
  protected String deepCopy(String data) {
    return data;
  }

  @Override
  public @NotNull Resource getType() {
    return ResourceFactory.createResource(FCAGE.getURI() + "TestExecutionNode");
  }

  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
  }

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.emptyInstance();
  }
}
