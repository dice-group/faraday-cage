package org.aksw.faraday_cage.decorator;

import org.aksw.faraday_cage.engine.ExecutionGraphNode;
import org.aksw.faraday_cage.engine.Plugin;


/**
 *
 */
public interface ExecutionNodeWrapper<V extends ExecutionGraphNode<T>, T> extends Plugin {

  V wrap(V executionNode);

}
