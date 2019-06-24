package org.aksw.faraday_cage.engine;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;

import java.io.InputStream;
import java.util.Objects;

/**
 *
 *
 *
 */
public abstract class AbstractParameterizedExecutionNode<T> extends AbstractExecutionNode<T> implements Parameterized {

  private ValidatableParameterMap parameterMap = null;


  @Override
  public void initParameters(ValidatableParameterMap parameterMap) {
    this.parameterMap = parameterMap;
  }

  @Override
  public final ValidatableParameterMap getParameterMap() {
    return parameterMap;
  }

  @Override
  public final boolean isInitialized() {
    return super.isInitialized() && parameterMap != null;
  }

  protected static Model getValidationModelFor(Class<?> clazz) {
    InputStream in =
      ExecutionNode.class.getResourceAsStream("/shacl/" + clazz.getCanonicalName() + ".ttl");
    if (Objects.isNull(in)) {
      return ModelFactory.createDefaultModel();
    }
    return ModelFactory.createDefaultModel().read(in, null, FileUtils.langTurtle);
  }

}
