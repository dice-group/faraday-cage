package org.aksw.faraday_cage.parameter;

import org.aksw.faraday_cage.parameter.conversions.ParameterConversion;
import org.aksw.faraday_cage.parameter.conversions.StringParameterConversion;
import org.aksw.faraday_cage.Vocabulary;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

/**
 */
public class ParameterImpl implements Parameter {

  private Property property;
  private boolean required;
  private ParameterConversion conversion;

  public ParameterImpl(String propertyName) {
    this(Vocabulary.property(propertyName), StringParameterConversion.getInstance(), true);
  }

  public ParameterImpl(String propertyName, ParameterConversion conversion, boolean required) {
    this(Vocabulary.property(propertyName), conversion, required);
  }

  private ParameterImpl(Property property, ParameterConversion conversion, boolean required) {
    this.property = property;
    this.required = required;
    this.conversion = conversion;
  }

  @Override
  public Property getProperty() {
    return property;
  }

  @Override
  public RDFNode applySerialization(Object object) {
    return conversion.toRDF(object);
  }

  @Override
  public Object applyDeserialization(RDFNode node) {
    return conversion.fromRDF(node);
  }

  @Override
  public boolean isRequired() {
    return required;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Parameter) {
      Parameter p = (Parameter) o;
      return this.getProperty().equals(p.getProperty());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return this.getProperty().hashCode();
  }

  @Override
  public String toString() {
    return this.getProperty().toString() + " [required]";
  }

}