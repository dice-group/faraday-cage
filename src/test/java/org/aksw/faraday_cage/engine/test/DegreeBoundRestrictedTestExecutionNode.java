package org.aksw.faraday_cage.engine.test;

import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

/**
 *
 */
@Extension
public class DegreeBoundRestrictedTestExecutionNode extends TestExecutionNode {

  @NotNull
  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(1, 2, 2, 3);
  }

}
