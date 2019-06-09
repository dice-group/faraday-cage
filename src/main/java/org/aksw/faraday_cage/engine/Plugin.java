package org.aksw.faraday_cage.engine;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.pf4j.ExtensionPoint;

/**
 *
 */
public interface Plugin extends ExtensionPoint {

  @NotNull Resource getId();

  static Resource getImplementationType(@NotNull Resource executionId) {
    Resource implementation = executionId.getPropertyResourceValue(RDF.type);
    if (implementation == null) {
      throw new RuntimeException("Implementation type of " + executionId + " is not specified!");
    }
    return implementation;
  }

  /**
   * Get the type URI resource of the {@code ParametrizedPlugin}.
   * The type of a {@code ParametrizedPlugin} identifies the implementing class, <b>must</b> be
   * unique and <b>must not</b> be a blank node.
   * It is used to instantiate the class using the {@link PluginFactory}.
   * Defaults to using the implementing classes {@link Class#getSimpleName()}.
   *
   * @return  RDF URI resource identifying the implementation class
   */
  @NotNull Resource getType();

  /**
   * Get initialization status
   * @return  {@code true}, if initialized; {@code false}, else.
   */
  boolean isInitialized();

  void initPluginId(@NotNull Resource id);


}