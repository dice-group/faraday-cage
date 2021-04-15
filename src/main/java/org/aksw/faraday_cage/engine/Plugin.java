/*
 * FARADAY-CAGE - Framework for acyclic directed graphs yielding parallel computations of great efficiency
 * Copyright © 2018 Data Science Group (DICE) (kevin.dressler@uni-paderborn.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.aksw.faraday_cage.engine;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.pf4j.ExtensionPoint;

/**
 *
 */
public interface Plugin extends ExtensionPoint {

  Resource getId();

  static Resource getImplementationType(Resource executionId) {
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
  Resource getType();

  /**
   * Get initialization status
   * @return  {@code true}, if initialized; {@code false}, else.
   */
  boolean isInitialized();

  void initPluginId(Resource id);


}