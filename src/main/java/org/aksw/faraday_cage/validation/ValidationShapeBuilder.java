package org.aksw.faraday_cage.validation;

import org.apache.jena.rdf.model.*;
import org.jetbrains.annotations.NotNull;
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

  @NotNull
  static ValidationShapeBuilder create() {
    return new ValidationShapeBuilder();
  }

  @NotNull
  private Model model = ModelFactory.createDefaultModel();

  private Resource self = model.createResource();

  private ValidationShapeBuilder() {}

  @NotNull
  public ValidationShapeBuilder xone(UnaryOperator<ValidationShapeBuilder>...shapes) {
    return group(ResourceFactory.createProperty(SH.BASE_URI + "xone"), shapes);
  }

  @NotNull
  public ValidationShapeBuilder and(UnaryOperator<ValidationShapeBuilder>...shapes) {
    return group(SH.and, shapes);
  }

  @NotNull
  public ValidationShapeBuilder or(UnaryOperator<ValidationShapeBuilder>...shapes) {
    return group(SH.or, shapes);
  }

  @NotNull
  private ValidationShapeBuilder group(Property groupingProperty, @NotNull UnaryOperator<ValidationShapeBuilder>...shapes) {
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

  @NotNull
  @Override
  public Model get() {
    return this.model;
  }

}
