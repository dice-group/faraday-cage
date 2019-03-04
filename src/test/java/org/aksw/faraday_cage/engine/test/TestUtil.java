package org.aksw.faraday_cage.engine.test;

import org.aksw.faraday_cage.engine.ExecutionGraphNode;
import org.aksw.faraday_cage.engine.PluginFactory;
import org.aksw.faraday_cage.vocabulary.FCAGE;
import org.pf4j.DefaultPluginManager;

import static org.mockito.Mockito.mock;

/**
 *
 */
class TestUtil {

  interface ITestExecutionGraphNode extends ExecutionGraphNode<String> {}

  static PluginFactory<ITestExecutionGraphNode> testFactory
    = new PluginFactory<>(ITestExecutionGraphNode.class, new DefaultPluginManager(), FCAGE.ExecutionNode);

  @SuppressWarnings("unchecked")
  static <V extends ExecutionGraphNode<T>, T> PluginFactory<V> mockFactory() {
    return mock(PluginFactory.class);
  }

}
