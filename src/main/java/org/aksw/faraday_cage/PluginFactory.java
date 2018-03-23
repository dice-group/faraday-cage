package org.aksw.faraday_cage;

import org.apache.jena.rdf.model.Resource;
import org.pf4j.ExtensionFactory;
import org.pf4j.PluginManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A generic factory for implementations of {@code ParametrizedPlugin}.
 * <p>
 * Create an instance of this
 */
public class PluginFactory<V extends Plugin<T>, T> implements IdentifiableExecutionFactory<T> {

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
   * this {@code ParametrizedPluginFactory}s type parameters {@code Class} instance
   */
  private Class<V> clazz;

  /**
   * Constructor, takes an instance of this {@code ParametrizedPluginFactory}s type parameters
   * {@code Class}.
   *
   * @param clazz {@code Class} instance for this {@code ParametrizedPluginFactory}s type parameter
   * @param pluginManager the {@code PluginManager} to be used in this {@code PluginFactory}
   *
   */
  public PluginFactory(Class<V> clazz, PluginManager pluginManager) {
    this.clazz = clazz;
    this.pluginManager = pluginManager;
    this.factory = pluginManager.getExtensionFactory();
    this.classMap = createClassMap();
  }

  /**
   * Generates the class map for instances of the interface referenced by this
   * {@code ParametrizedPluginFactory}s type parameter.
   *
   * @return the class map for this {@code ParametrizedPluginFactory}s type parameter
   */
  private Map<Resource, Class<?>> createClassMap() {
    Map<Resource, Class<?>> classMap = new HashMap<>();
    pluginManager.getExtensions(clazz).forEach(
      (plugin) -> classMap.put(plugin.getType(), plugin.getClass())
    );
    return classMap;
  }

  /**
   * Create an instance of the {@code ParametrizedPlugin} identified by {@code id}.
   *
   * @param id  identifier of the instance to create
   * @return  instance of the {@code ParametrizedPlugin} identified by {@code id}.
   */
  public final V create(Resource id) {
    Resource type = Plugin.getImplementationType(id);
    if (!classMap.containsKey(type)) {
      throw new RuntimeException(clazz.getName() + " implementation for declaration \"" + type
        + "\" could not be found.");
    } else {
      Object o = factory.create(classMap.get(type));
      if (!clazz.isInstance(o)) {
        throw new RuntimeException("Plugin \"" + type + "\" required as " + clazz.getName()
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
    return classMap.keySet().stream()
      .peek(r -> r.addProperty(Vocabulary.implementedIn(),r))
      .collect(Collectors.toList());
  }

}