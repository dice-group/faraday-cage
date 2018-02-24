package org.aksw.faraday_cage.plugin;

import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public interface Identifiable {

  @NotNull Resource getId();

}
