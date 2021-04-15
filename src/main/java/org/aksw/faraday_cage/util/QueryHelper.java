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
package org.aksw.faraday_cage.util;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Helper class for working with Jena SPARQL API.
 * Best used with static imports.
 */
public class QueryHelper {

  public static void forEachResultOf(Query q, Model m, Consumer<QuerySolution> f) {
    QueryExecution qExec = QueryExecutionFactory.create(q, m);
    ResultSet queryResults = qExec.execSelect();
    while (queryResults.hasNext()) {
      QuerySolution qs = queryResults.nextSolution();
      f.accept(qs);
    }
    qExec.close();
  }

  public static void forEachResultOf(String q, Model m, Consumer<QuerySolution> f) {
    forEachResultOf(QueryFactory.create(q), m, f);
  }

  public static <V> List<V> mapResultOf(Query q, Model m, Function<QuerySolution, V> f) {
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

  public static <V> List<V> mapResultOf(String q, Model m, Function<QuerySolution, V> f) {
    return mapResultOf(QueryFactory.create(q), m, f);
  }

  public static boolean hasEmptyResult(Query q, Model m) {
    QueryExecution qExec = QueryExecutionFactory.create(q, m);
    ResultSet queryResults = qExec.execSelect();
    return !queryResults.hasNext();
  }

  public static String not(String s) {
    return "NOT " + s;
  }

  public static String exists(String s) {
    return "EXISTS { " + s + " }";
  }

  public static String triple(String s, Property p, Resource o) {
    return s + " <" + p + "> <" + o + "> .";
  }

  public static String triple(String s, Property p, String o) {
    return s + " <" + p + "> " + o + " .";
  }

  public static String triple(Resource s, Property p, Resource o) {
    return "<" + s + "> <" + p + "> <" + o + "> .";
  }

  public static String triple(Resource s, Property p, String o) {
    return "<" + s + "> <" + p + "> " + o + " .";
  }

}
