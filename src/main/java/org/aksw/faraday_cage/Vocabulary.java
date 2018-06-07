package org.aksw.faraday_cage;

import org.apache.jena.rdf.model.*;

/**
 *
 *
 *
 */
public class Vocabulary {

  private static Model vocabModel = ModelFactory.createDefaultModel();

  private static String uri = "http://aksw.org/faraday_cage/#";

  public static Property implementedIn() {
    return property("implementedIn");
  }

  public static Property hasInput() {
    return property("hasInput");
  }

  public static Property hasOutput() {
    return property("hasOutput");
  }

  public static Property toNode() { return property("toNode"); }

  public static Property toPort() { return property("toPort"); }

  public static Property property(String s) {
    if (s.contains("http://") ||
        s.contains("https://") ||
        s.contains(":") && vocabModel.getNsPrefixMap()
      .containsKey(s.substring(0, s.indexOf(":")))) {
      return vocabModel.createProperty(s);
    }
    return vocabModel.createProperty(getURI() + s);
  }

  public static Resource resource(String s) {
    if (s.contains("http://") ||
      s.contains("https://") ||
      s.contains(":") && vocabModel.getNsPrefixMap()
        .containsKey(s.substring(0, s.indexOf(":")))) {
      return vocabModel.createResource(s);
    }
    return vocabModel.createResource(getURI() + s);
  }

  public static String getURI() {
    return Vocabulary.uri;
  }

  public static void setDefaultURI(String uri) {
    Vocabulary.uri = uri;
  }

  public static void addNSPrefix(String prefix, String expansion) {
    vocabModel.setNsPrefix(prefix, expansion);
  }

}
