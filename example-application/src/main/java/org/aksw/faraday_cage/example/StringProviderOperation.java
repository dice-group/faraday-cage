package org.aksw.faraday_cage.example;

import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Property;
import org.pf4j.Extension;

import java.util.List;

/**
 * {@code StringProviderOperation} must have no inputs and exactly one output.
 * Provides a string to the connected {@code StringOperation}.
 */
@Extension
public class StringProviderOperation extends AbstractStringOperation {

  public static final Property INPUT_STRING = ExampleApplication.createProperty("inputString");

  @Override
  protected List<String> safeApply(List<String> data) {
    return List.of(getParameterMap().get(INPUT_STRING).asLiteral().getString());
  }

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(INPUT_STRING)
      .declareValidationShape(getValidationModelFor(StringProviderOperation.class))
      .build();
  }

  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(0, 0, 1, 1);
  }
}