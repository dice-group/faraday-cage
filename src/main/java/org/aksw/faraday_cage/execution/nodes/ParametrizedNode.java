package org.aksw.faraday_cage.execution.nodes;

import org.aksw.faraday_cage.plugin.Plugin;
import org.aksw.faraday_cage.plugin.parametrized.ParameterMap;
import org.aksw.faraday_cage.plugin.parametrized.Parametrized;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

/**
 *
 *
 *
 */
public interface ParametrizedNode<T> extends Node<T>, Parametrized, Plugin {

  void init(@NotNull Resource id, int inDegree, int outDegree, @NotNull ParameterMap parameterMap);

}
