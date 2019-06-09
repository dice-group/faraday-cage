package org.aksw.faraday_cage.example;

import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.apache.jena.rdf.model.Property;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

import java.util.List;

/**
 * {@code SubStringOperation} accepts exactly one input and exactly one output.
 * The input string will be cut off at the specified length.
 * If the input string is shorter than the specified length, it has no effect.
 */
@Extension
public class SubStringOperation extends AbstractStringOperation {

  public static final Property LENGTH = ExampleApplication.createProperty("length");

  @NotNull
  @Override
  protected List<String> safeApply(@NotNull List<String> data) {
    String s = data.get(0);
    int i = 0;
    int len = getParameterMap().get(LENGTH).asLiteral().getInt();
    StringBuilder out = new StringBuilder();
    for (char c : s.toCharArray()) {
      out.append(c);
      if (++i == len) break;
    }
    return List.of(out.toString());
  }

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.builder()
      .declareProperty(LENGTH)
      .declareValidationShape(getValidationModelFor(SubStringOperation.class))
      .build();
  }

  @NotNull
  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(1, 1, 1, 1);
  }
}