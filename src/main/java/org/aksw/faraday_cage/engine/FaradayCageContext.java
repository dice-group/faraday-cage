package org.aksw.faraday_cage.engine;

import org.aksw.faraday_cage.decorator.ExecutionNodeWrapper;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.shacl.util.ModelPrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 *
 */
public class FaradayCageContext<U extends ExecutionNode<T>, V extends ExecutionNodeWrapper<U, T>, T> {

  public static <U extends ExecutionNode<T>, V extends ExecutionNodeWrapper<U, T>, T> FaradayCageContext<U, V, T> of(Class<U> uClass, Class<V> vClass, PluginManager pluginManager) {
    return new FaradayCageContext<>(uClass, vClass, pluginManager);
  }

  public static String getRunId() {
    return runId.get();
  }

  public static String newRunId() {
    return UUID.randomUUID().toString();
  }

  public static void addForkListener(Consumer<String> runIdConsumer) {
    valueConsumer.add(runIdConsumer);
  }

  public static <X> CompletableFuture<X> getCompletableFuture() {
    return new ThreadlocalInheritingCompletableFuture<>();
  }

  public static <X> CompletableFuture<X> getCompletedFuture(X completionValue) {
    return ThreadlocalInheritingCompletableFuture.completedFuture(completionValue);
  }

  static void setRunId(String runId) {
    FaradayCageContext.runId.set(runId);
  }

  static final List<Consumer<String>> valueConsumer = new ArrayList<>();

  private static final ThreadLocal<String> runId = ThreadLocal.withInitial(() -> null);

  private static final Logger logger = LoggerFactory.getLogger(FaradayCageContext.class);

  private final Class<U> uClass;

  private final Class<V> vClass;

  private final PluginManager pluginManager;

  private final DecoratedExecutionNodeFactory<U,V,T> factory;

  private final Model pluginDeclarations = ModelFactory.createDefaultModel();

  private FaradayCageContext(Class<U> uClass, Class<V> vClass, PluginManager pluginManager) {
    this.uClass = uClass;
    this.vClass = vClass;
    this.pluginManager = pluginManager;
    this.factory
      = new DecoratedExecutionNodeFactory<>(uClass, vClass, pluginManager, ModelFactory.createDefaultModel());
    final List<Resource> allPluginTypes = factory.listAvailable();
    allPluginTypes.addAll(factory.getWrapperFactory().listAvailable());
    allPluginTypes.stream()
      .map(Resource::listProperties)
      .forEach(pluginDeclarations::add);
  }

  public Model getAllAvailablePluginDeclarations() {
    return pluginDeclarations;
  }

  public Model getValidationModelFor(Resource id) {
    return new ConfigurationGraphValidator(factory).getValidationModelFor(id);
  }

  public Model getFullValidationModel() {
    return new ConfigurationGraphValidator(factory).getFullValidationModel();
  }

  public void run(Model configModel) {
    run(compile(configModel));
  }

  public void run(CompiledExecutionGraph compiledExecutionGraph) {
    StopWatch time = new StopWatch();
    logger.info("Starting execution...");
    time.start();
    compiledExecutionGraph.run();
    time.split();
    logger.info("Execution finished after {}ms", time.getSplitTime());
  }

  public CompiledExecutionGraph compile(Model configModel) {
    return compile(configModel, newRunId());
  }

  public CompiledExecutionGraph compile(Model configModel, String runId) {
    StopWatch time = new StopWatch();
    logger.info("Starting Faraday-Cage engine... runId: " + runId);
    logger.info("Building execution model...");
    time.start();
    DecoratedExecutionNodeFactory<U,V,T> decoratedFactory
      = new DecoratedExecutionNodeFactory<>(uClass, vClass, pluginManager, configModel);
    validate(configModel, decoratedFactory);
    time.split();
    logger.info("Configuration shape validated using SHACL after {}ms.", time.getSplitTime());
    ExecutionGraph executionGraph = generateExecutionGraphFromConfiguration(configModel, decoratedFactory);
    time.split();
    logger.info("Execution graph built after {}ms.", time.getSplitTime());
    CompiledExecutionGraph compiled = executionGraph.compile(runId);
    time.split();
    logger.info("Execution graph compiled after {}ms.", time.getSplitTime());
    return compiled;
  }

  private void validate(Model configModel, DecoratedExecutionNodeFactory<U,V,T> pluginFactory) {
    Resource validationReport = new ConfigurationGraphValidator(pluginFactory).validate(configModel);
    if (!ConfigurationGraphValidator.isConformingValidationReport(validationReport)) {
      throw new IllegalArgumentException("Invalid configuration graph!\n" +
        "Please check the SHACL validation report for further hints:\n\n" +
        ModelPrinter.get().print(validationReport.getModel()));
    }
  }

  private ExecutionGraph<T> generateExecutionGraphFromConfiguration(Model configModel, PluginFactory<U> pluginFactory) {
    return ExecutionGraphGenerator.generate(configModel, pluginFactory);
  }

}
