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
package org.aksw.faraday_cage.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for https://w3id.org/fcage/
 */
public class FCAGE {

  public static String NS = "https://w3id.org/fcage/";

  public static String PREFIX = "fcage";

  public static Property hasInput = ResourceFactory.createProperty(NS + "hasInput");

  public static Property fromNode = ResourceFactory.createProperty(NS + "fromNode");

  public static Property fromPort = ResourceFactory.createProperty(NS + "fromPort");

  public static Property decoratedBy = ResourceFactory.createProperty(NS + "decoratedBy");

  public static Resource ExecutionNode = ResourceFactory.createResource(NS + "ExecutionNode");

  public static Resource ExecutionNodeWrapper = ResourceFactory.createResource(NS + "ExecutionNodeWrapper");

  public static String getURI() {
    return NS;
  }

}
