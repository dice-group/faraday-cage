package org.aksw.faraday_cage.engine.test;

import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.PluginFactory;
import org.aksw.faraday_cage.vocabulary.FCAGE;
import org.pf4j.DefaultPluginManager;

import static org.mockito.Mockito.mock;

/**
 *
 */
class TestUtil {

  interface ITestExecutionNode extends ExecutionNode<String> {}

  static PluginFactory<ITestExecutionNode> testFactory
    = new PluginFactory<>(ITestExecutionNode.class, new DefaultPluginManager(), FCAGE.ExecutionNode);

  @SuppressWarnings("unchecked")
  static <V extends ExecutionNode<T>, T> PluginFactory<V> mockFactory() {
    return mock(PluginFactory.class);
  }

}
