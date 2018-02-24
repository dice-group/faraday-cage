package org.aksw.faraday_cage.nodes;

import org.aksw.faraday_cage.parameter.ParameterMap;
import org.aksw.faraday_cage.Parametrized;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public interface ParametrizedNode<T> extends Node<T>, Parametrized {

  void init(@NotNull Resource id, int inDegree, int outDegree, @NotNull ParameterMap parameterMap);

}
