package org.aksw.faraday_cage.plugin;


import org.apache.jena.rdf.model.Resource;

import java.util.List;

/**
 *
 *
 *
 */
public interface IdentifiableFactory<T extends Identifiable> {

  T create(Resource id);

  List<Resource> listAvailable();

}
