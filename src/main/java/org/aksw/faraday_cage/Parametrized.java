package org.aksw.faraday_cage;

import org.aksw.faraday_cage.parameter.ParameterMap;
import org.jetbrains.annotations.NotNull;


/**
 * Base interface for parametrized plugins, defines a contract.
 * <p>
 * A {@code ParametrizedPlugin} is a class that is parametrized using a {@link ParameterMap}.
 * <br>
 * It should only be instantiated from instances of {@link PluginFactory}.
 * For this to work, each plugin type needs to define a <i>plugin type interface</i> that extends
 * at least {@code ParametrizedPlugin} and {@link org.pf4j.ExtensionPoint}.
 * Furthermore, each non-abstract class implementing a <i>plugin type interface</i> should be
 * annotated with {@link org.pf4j.Extension}.
 *
 */
public interface Parametrized {

  /**
   * Create an uninitialized {@code ParameterMap} to be filled by the
   * {@link ExecutionGraphGenerator}.
   *
   * @return  uninitialized {@code ParameterMap} containing all allowed {@code Parameter}
   */
  @NotNull
  ParameterMap createParameterMap();

  /**
   * Accept an initialized {@code ParameterMap} in order to configure this instance.
   *
   * @param parameterMap  an initialized {@code ParameterMap}
   */
  void init(@NotNull ParameterMap parameterMap);

  /**
   * Get this instances configuration as an initialized {@code ParameterMap}.
   *
   * @return  an initialized {@code ParameterMap}.
   */
  @NotNull
  ParameterMap getParameterMap();

  /**
   * Get initialization status
   * @return  {@code true}, if initialized; {@code false}, else.
   */
  boolean isInitialized();

}
