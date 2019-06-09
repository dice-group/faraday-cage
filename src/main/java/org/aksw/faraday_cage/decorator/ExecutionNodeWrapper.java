package org.aksw.faraday_cage.decorator;

import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.Plugin;
import org.jetbrains.annotations.NotNull;


/**
 *
 */
public interface ExecutionNodeWrapper<V extends ExecutionNode<T>, T> extends Plugin {

  @NotNull V wrap(V executionNode);

}
