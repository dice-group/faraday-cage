package org.aksw.faraday_cage.decorator;

import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.Plugin;


/**
 *
 */
public interface ExecutionNodeWrapper<V extends ExecutionNode<T>, T> extends Plugin {

  V wrap(V executionNode);

}
