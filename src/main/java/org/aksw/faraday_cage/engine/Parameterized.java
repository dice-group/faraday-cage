package org.aksw.faraday_cage.engine;

/**
 *
 */
public interface Parameterized {
  /**
   * Create an uninitialized {@code ParameterMap} to be filled by the
   * {@link ExecutionGraphGenerator}.
   *
   * @return  uninitialized {@code ParameterMap} containing all allowed {@code Parameter}
   */
  ValidatableParameterMap createParameterMap();

  /**
   * Accept an initialized {@code ParameterMap} in order to configure this instance.
   *
   * @param parameterMap  an initialized {@code ParameterMap}
   */
  void initParameters(ValidatableParameterMap parameterMap);

  /**
   * Get this instances configuration as an initialized {@code ParameterMap}.
   *
   * @return  an initialized {@code ParameterMap}.
   */
  ValidatableParameterMap getParameterMap();

  /**
   * Get initialization status
   * @return  {@code true}, if initialized; {@code false}, else.
   */
  boolean isInitialized();

}
