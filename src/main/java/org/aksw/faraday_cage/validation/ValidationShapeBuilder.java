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
