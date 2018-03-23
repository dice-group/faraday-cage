package org.aksw.faraday_cage;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import static org.junit.Assert.*;
import org.pf4j.DefaultPluginManager;
import org.pf4j.Extension;

import java.util.Arrays;
import java.util.List;

public class PluginFactoryTest {
  interface TestPluginType extends Plugin<Model> {}
  @Extension
  public static class TestPlugin implements TestPluginType {
    @Override
    public void init(@NotNull Resource id) {
      System.out.println(id.getURI());
    }

    @NotNull
    @Override
    public Resource getId() {
      return ResourceFactory.createResource("test");
    }

    @Override
    public Model apply(Model data) {
      return null;
    }

    @Override
    public List<Model> apply(List<Model> data) {
      return null;
    }
  }
  @Extension
  public static class TestPlugin2 extends TestPlugin { }

  @Test
  public void test() {
    List<Resource> expected = Arrays.asList(Vocabulary.resource("TestPlugin"), Vocabulary.resource("TestPlugin2"));
    PluginFactory<TestPluginType, Model> factory = new PluginFactory<>(TestPluginType.class, new DefaultPluginManager());
    List<Resource> actual = factory.listAvailable();
    assertEquals("it should return exactly two plugin types: TestPlugin and TestPlugin2", expected, actual);
  }

}
