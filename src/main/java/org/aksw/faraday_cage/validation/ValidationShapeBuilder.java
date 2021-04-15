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
package org.aksw.faraday_cage.validation;

import org.apache.jena.rdf.model.*;
import org.topbraid.shacl.vocabulary.SH;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 *
 *
 *
 */
public class ValidationShapeBuilder implements Supplier<Model> {

  static ValidationShapeBuilder create() {
    return new ValidationShapeBuilder();
  }

  private Model model = ModelFactory.createDefaultModel();

  private Resource self = model.createResource();

  private ValidationShapeBuilder() {}

  public ValidationShapeBuilder xone(UnaryOperator<ValidationShapeBuilder>...shapes) {
    return group(ResourceFactory.createProperty(SH.BASE_URI + "xone"), shapes);
  }

  public ValidationShapeBuilder and(UnaryOperator<ValidationShapeBuilder>...shapes) {
    return group(SH.and, shapes);
  }

  public ValidationShapeBuilder or(UnaryOperator<ValidationShapeBuilder>...shapes) {
    return group(SH.or, shapes);
  }

  private ValidationShapeBuilder group(Property groupingProperty, UnaryOperator<ValidationShapeBuilder>...shapes) {
    Arrays.stream(shapes).forEach(op -> {
      Resource other = op.apply(new ValidationShapeBuilder()).getSelf();
      model.add(self, groupingProperty, other);
      model.add(other.getModel());
    });
    return this;
  }

  private Resource getSelf() {
    return this.self;
  }

  @Override
  public Model get() {
    return this.model;
  }

}
