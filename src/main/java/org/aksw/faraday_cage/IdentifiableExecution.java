package org.aksw.faraday_cage;

import org.aksw.faraday_cage.Execution;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public interface IdentifiableExecution<T> extends Execution<T> {

  @NotNull Resource getId();

}
