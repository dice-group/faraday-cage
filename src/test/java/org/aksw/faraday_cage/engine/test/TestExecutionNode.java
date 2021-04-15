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
package org.aksw.faraday_cage.engine.test;

import org.aksw.faraday_cage.engine.AbstractParameterizedExecutionNode;
import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.ValidatableParameterMap;
import org.aksw.faraday_cage.vocabulary.FCAGE;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.pf4j.Extension;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 *
 */
@Extension
public class TestExecutionNode extends AbstractParameterizedExecutionNode<String> implements TestUtil.ITestExecutionNode {

  @Override
  protected List<String> safeApply(List<String> data) {
    if (data == null) {
      return List.of("Hello Sir!");
    }
    UnaryOperator<String> op = s -> s + "\nOh and Hello to you, Sir!";
    return ExecutionNode.toMultiExecution(op).apply(data);
  }

  @Override
  public String deepCopy(String data) {
    return data;
  }

  @Override
  public Resource getType() {
    return ResourceFactory.createResource(FCAGE.getURI() + "TestExecutionNode");
  }

  @Override
  public DegreeBounds getDegreeBounds() {
    return new DegreeBounds(0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
  }

  @Override
  public ValidatableParameterMap createParameterMap() {
    return ValidatableParameterMap.emptyInstance();
  }
}
