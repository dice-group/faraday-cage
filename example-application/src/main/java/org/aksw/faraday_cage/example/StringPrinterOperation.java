package org.aksw.faraday_cage.example;

import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.pf4j.Extension;

import java.util.List;

/**
 * {@code StringPrinterOperation} must have exactly one input and may have at most one output.
 * Prints the provided string to {@code System.out}.
 */
@Extension
public class StringPrinterOperation extends AbstractStringOperation {
  @Override
  protected List<String> safeApply(List<String> data) {
    data.forEach(System.out::println);
    return List.of();
  }

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.emptyInstance();
  }

  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(1, 1, 0, 1);
  }
}