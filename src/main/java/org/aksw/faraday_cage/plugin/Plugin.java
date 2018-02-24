package org.aksw.faraday_cage.plugin;

import org.aksw.faraday_cage.rdf.config.Vocabulary;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;
import org.pf4j.ExtensionPoint;

/**
 *
 *
 *
 */
public interface Plugin extends ExtensionPoint, Identifiable {

  static Resource getImplementationType(Resource executionId) {
    Resource implementation = executionId.getPropertyResourceValue(Vocabulary.implementedIn());
    if (implementation == null) {
      throw new RuntimeException("Implementation type of " + executionId + " is not specified!");
    }
    return implementation;
  }


  /**
   * Get the type {@code ParametrizedPlugin}.
   * The type of a {@code ParametrizedPlugin} identifies the implementing class and <b>must</b> be
   * unique.
   * It is used to instantiate the class using the {@link PluginFactory}.
   * Defaults to using the implementing classes {@link Class#getSimpleName()}.
   *
   * @return  RDF resource identifying the implementation class
   */
  @NotNull
  default Resource getType() {
    return Vocabulary.resource(this.getClass().getSimpleName());
  }

  void init(@NotNull Resource id);


}
