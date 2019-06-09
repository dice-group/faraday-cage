package org.aksw.faraday_cage.util;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Helper class for working with Jena SPARQL API.
 * Best used with static imports.
 */
public class QueryHelper {

  public static void forEachResultOf(@NotNull Query q, @NotNull Model m, @NotNull Consumer<QuerySolution> f) {
    QueryExecution qExec = QueryExecutionFactory.create(q, m);
    ResultSet queryResults = qExec.execSelect();
    while (queryResults.hasNext()) {
      QuerySolution qs = queryResults.nextSolution();
      f.accept(qs);
    }
    qExec.close();
  }

  public static void forEachResultOf(String q, @NotNull Model m, @NotNull Consumer<QuerySolution> f) {
    forEachResultOf(QueryFactory.create(q), m, f);
  }

  @NotNull
  public static <V> List<V> mapResultOf(@NotNull Query q, @NotNull Model m, @NotNull Function<QuerySolution, V> f) {
    QueryExecution qExec = QueryExecutionFactory.create(q, m);
    ResultSet queryResults = qExec.execSelect();
    List<V> result = new ArrayList<>();
    while (queryResults.hasNext()) {
      QuerySolution qs = queryResults.nextSolution();
      result.add(f.apply(qs));
    }
    qExec.close();
    return result;
  }

  @NotNull
  public static <V> List<V> mapResultOf(String q, @NotNull Model m, @NotNull Function<QuerySolution, V> f) {
    return mapResultOf(QueryFactory.create(q), m, f);
  }

  public static boolean hasEmptyResult(@NotNull Query q, @NotNull Model m) {
    QueryExecution qExec = QueryExecutionFactory.create(q, m);
    ResultSet queryResults = qExec.execSelect();
    return !queryResults.hasNext();
  }

  @NotNull
  public static String not(String s) {
    return "NOT " + s;
  }

  @NotNull
  public static String exists(String s) {
    return "EXISTS { " + s + " }";
  }

  @NotNull
  public static String triple(String s, Property p, Resource o) {
    return s + " <" + p + "> <" + o + "> .";
  }

  @NotNull
  public static String triple(String s, Property p, String o) {
    return s + " <" + p + "> " + o + " .";
  }

  @NotNull
  public static String triple(Resource s, Property p, Resource o) {
    return "<" + s + "> <" + p + "> <" + o + "> .";
  }

  @NotNull
  public static String triple(Resource s, Property p, String o) {
    return "<" + s + "> <" + p + "> " + o + " .";
  }

}
