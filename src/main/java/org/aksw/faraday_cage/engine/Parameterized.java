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
