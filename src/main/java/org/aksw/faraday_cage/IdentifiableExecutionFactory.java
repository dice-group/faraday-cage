package org.aksw.faraday_cage;


import org.apache.jena.rdf.model.Resource;

import java.util.List;

/**
 *
 *
 *
 */
public interface IdentifiableExecutionFactory<T extends IdentifiableExecution> {

  T create(Resource id);

  List<Resource> listAvailable();

}