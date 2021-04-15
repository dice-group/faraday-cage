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
import org.apache.jena.util.FileUtils;

import java.io.InputStream;
import java.util.Objects;

/**
 *
 *
 *
 */
public abstract class AbstractParameterizedExecutionNode<T> extends AbstractExecutionNode<T> implements Parameterized {

  private ValidatableParameterMap parameterMap = null;


  @Override
  public void initParameters(ValidatableParameterMap parameterMap) {
    this.parameterMap = parameterMap;
  }

  @Override
  public final ValidatableParameterMap getParameterMap() {
    return parameterMap;
  }

  @Override
  public final boolean isInitialized() {
    return super.isInitialized() && parameterMap != null;
  }

  protected static Model getValidationModelFor(Class<?> clazz) {
    InputStream in =
      ExecutionNode.class.getResourceAsStream("/shacl/" + clazz.getCanonicalName() + ".ttl");
    if (Objects.isNull(in)) {
      return ModelFactory.createDefaultModel();
    }
    return ModelFactory.createDefaultModel().read(in, null, FileUtils.langTurtle);
  }

}
