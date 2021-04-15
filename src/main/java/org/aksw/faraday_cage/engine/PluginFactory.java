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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A generic factory for implementations of {@code FaradayCagePlugin}.
 * <p>
 * Create an instance of this
 */
public class PluginFactory<U extends Plugin> {

  /**
   * pf4j plugin manager
   */
  private PluginManager pluginManager;
  /**
   * pf4j extension factory
   */
  private ExtensionFactory factory;
  /**
   * map of class names to class instances
   */
  private Map<Resource, Class<?>> classMap;
  /**
   * this {@code FaradayCagePluginFactory}s type parameters {@code Class} instance
   */
  private Class<U> clazz;

  /**
   * this {@code FaradayCagePluginFactory}s associated RDF type
   */
  private Resource type;

  /**
   * Constructor, takes an instance of this {@code FaradayCagePluginFactory}s type parameters
   * {@code Class}.
   *  @param clazz {@code Class} instance for this {@code FaradayCagePluginFactory}s type parameter
   * @param pluginManager the {@code PluginManager} to be used in this {@code FaradayCagePluginFactory}
   * @param type Common parent ExecutionNode subtype IRI which all instances producible
   *             by this factory share.
   *
   */
  public PluginFactory(Class<U> clazz, PluginManager pluginManager, Resource type) {
    this.clazz = clazz;
    this.pluginManager = pluginManager;
    this.factory = pluginManager.getExtensionFactory();
    this.type = type;
    this.classMap = createClassMap();
  }

  /**
   * Generates the class map for instances of the interface referenced by this
   * {@code FaradayCagePluginFactory}s type parameter.
   *
   * @return the class map for this {@code FaradayCagePluginFactory}s type parameter
   */
  private Map<Resource, Class<?>> createClassMap() {
    Map<Resource, Class<?>> classMap = new HashMap<>();
    pluginManager.getExtensions(clazz).forEach(
      (plugin) -> classMap.put(plugin.getType(), plugin.getClass())
    );
    return classMap;
  }

  /**
   * Create an instance of the {@code FaradayCagePlugin} identified by {@code id}.
   *
   * @param  id  identifier of the instance to create
   * @return  instance of the {@code FaradayCagePlugin} identified by {@code id}.
   */
  public U create(Resource id) {
    Resource type = Plugin.getImplementationType(id);
    U u = getInstance(type);
    u.initPluginId(id);
    return u;
  }

  /**
   * Create an instance of {@code FaradayCagePlugin} for a given {@code type}.
   *
   * @param  type type of the instance to create
   * @return  instance of the {@code FaradayCagePlugin} for the given {@code type}.
   */
  public U getImplementationOf(Resource type) {
    U u = getInstance(type);
    u.initPluginId(type);
    return u;
  }

  private U getInstance(Resource type) {
    if (!classMap.containsKey(type)) {
      throw new RuntimeException(clazz.getName() + " implementation for declaration \"" + type
        + "\" could not be found.");
    } else {
      Object o = factory.create(classMap.get(type));
      if (!clazz.isInstance(o)) {
        throw new RuntimeException("ExecutionNode \"" + type + "\" required as " + clazz.getName()
          + ", but has type " + o.getClass().getName());
      } else {
        return clazz.cast(o);
      }
    }
  }

  /**
   * List the names of all implementations of {@code T}
   *
   * @return list of names of all implementations of {@code T}
   */
  public final List<Resource> listAvailable() {
    Model resultModel = ModelFactory.createDefaultModel();
    return classMap.keySet().stream()
      .map(r -> resultModel.createResource(r.getURI()))
      .peek(r -> r.addProperty(RDFS.subClassOf, type))
      .collect(Collectors.toList());
  }

}