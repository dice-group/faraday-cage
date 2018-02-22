package org.aksw.faraday_cage.execution;

import org.aksw.deer.enrichment.EnrichmentOperator;
import org.aksw.deer.io.ModelReader;
import org.aksw.deer.io.ModelWriter;
import org.aksw.faraday_cage.parameter.ParameterMap;
import org.aksw.faraday_cage.parameter.ParametrizedPluginFactory;
import org.aksw.faraday_cage.rdf.vocab.BaseVocabulary;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Generate an {@code ExecutionModel} from a given RDF configuration model.
 * <p>
 *
 */
public class ExecutionModelGenerator {

  private static final Logger logger = LoggerFactory.getLogger(ExecutionModelGenerator.class);

  private ExecutionGraph executionGraph;
  private List<ExecutionPipelineBuilder> pipeBuilders;
  private List<Resource> hubs;
  private ParametrizedPluginFactory<EnrichmentOperator> pluginFactory;

  /**
   *
   * @param model  a configuration RDF model
   */
  public ExecutionModelGenerator(Model model) {
    this();
    this.executionGraph = new ExecutionGraph(model);
  }

  /**
   *
   */
  private ExecutionModelGenerator() {
    this.pluginFactory = new ParametrizedPluginFactory<>(EnrichmentOperator.class);
    this.pipeBuilders = new ArrayList<>();
    this.hubs = new ArrayList<>();
  }

  /**
   *
   * @return
   */
  public ExecutionModel generate() {
    // first step: build pipelines
    List<ExecutionPipeline> pipes = buildPipelines();
    // second step: glue them together using hubs and return resulting executionModel
    return gluePipelines(pipes);
  }

  /**
   * Do a depth-first search starting at input dataset nodes in the given configuration graph.
   *
   * @return
   */
  private List<ExecutionPipeline> buildPipelines() {
    Set<Resource> datasets = executionGraph.getStartDatasets();
    Deque<Resource> stack = new ArrayDeque<>();
    for (Resource ds : datasets) {
      executionGraph.setSubGraphId(ds, pipeBuilders.size());
      stack.push(ds);
      pipeBuilders.add(new ExecutionPipelineBuilder().writeFirstUsing(getWriter(ds)));
    }
    while (!stack.isEmpty()) {
      Set<Resource> links = traverse(stack.pop());
      for (Resource link : links) {
        stack.push(link);
      }
    }
    return pipeBuilders.stream().map(ExecutionPipelineBuilder::build).collect(Collectors.toList());
  }

  /**
   * Get datasets connected to this dataset with one enrichment operator inbetween.
   *
   * @param ds Input dataset
   * @return Set of datasets connected to this dataset with one enrichment or operator inbetween.
   */
  @SuppressWarnings("unchecked")
  private Set<Resource> traverse(Resource ds) {
    Set<Resource> links = new HashSet<>();
    List<Resource> operators = executionGraph.getDatasetConsumers(ds);
    int currentSubGraphId = executionGraph.getSubGraphId(ds);
    for (Resource operator : operators) {
      if (!executionGraph.isVisited(operator)) {
        if (executionGraph.isHub(operator)) {
          hubs.add(operator);
          for (Resource dataset : executionGraph.getOperatorOutputs(operator)) {
            // set subgraph id (visited state) and add to links
            executionGraph.setSubGraphId(dataset, pipeBuilders.size());
            // create new pipelines
            pipeBuilders.add(new ExecutionPipelineBuilder().writeFirstUsing(getWriter(dataset)));
            links.add(dataset);
          }
        } else {
          Resource dataset = executionGraph.getOperatorOutputs(operator).get(0);
          // add enrichment function to pipe
          EnrichmentOperator fn = getOperator(operator);
          ParameterMap parameterMap = fn.createParameterMap();
          parameterMap.init(operator);
          fn.init(parameterMap, 1, 1);
          pipeBuilders.get(currentSubGraphId).chain(fn, getWriter(dataset));
          // set subgraph id (visited state) and add to links
          executionGraph.setSubGraphId(dataset, currentSubGraphId);
          links.add(dataset);
        }
        executionGraph.visit(operator);
      }
    }
    return links;
  }

  /**
   *
   * @param pipes
   * @return
   */
  private ExecutionModel gluePipelines(List<ExecutionPipeline> pipes) {
    for (Resource operatorHub : hubs) {
      List<Resource> operatorInputs = executionGraph.getOperatorInputs(operatorHub);
      List<Resource> operatorOutputs = executionGraph.getOperatorOutputs(operatorHub);
      EnrichmentOperator operator = getOperator(operatorHub);
      ParameterMap parameterMap = operator.createParameterMap();
      parameterMap.init(operatorHub);
      operator.init(parameterMap, operatorInputs.size(), operatorOutputs.size());
      Function<List<Resource>, List<ExecutionPipeline>> getPipes = l -> l.stream()
        .map(r -> pipes.get(executionGraph.getSubGraphId(r)))
        .collect(Collectors.toList());
      new ExecutionHub(operator, getPipes.apply(operatorInputs), getPipes.apply(operatorOutputs));
    }
    ExecutionModel executionModel = new ExecutionModel();
    for (Resource startDs : executionGraph.getStartDatasets()) {
      executionModel.addPipeline(pipes.get(executionGraph.getSubGraphId(startDs)), readDataset(startDs));
    }
    return executionModel;
  }


  /**
   * @param dataset
   * @return dataset model from file/uri/endpoint
   */
  private Supplier<Model> readDataset(Resource dataset) {
    ModelReader modelReader = new ModelReader();
    final String s;
    if (dataset.hasProperty(BaseVocabulary.fromEndPoint)) {
      s = dataset.getProperty(BaseVocabulary.fromEndPoint).getObject().toString();
      return () -> modelReader.readModelFromEndPoint(dataset, s);
    } else if (dataset.hasProperty(BaseVocabulary.hasUri)) {
      s = dataset.getProperty(BaseVocabulary.hasUri).getObject().toString();
    } else if (dataset.hasProperty(BaseVocabulary.inputFile)) {
      s = dataset.getProperty(BaseVocabulary.inputFile).getObject().toString();
    } else {
      //@todo: introduce MalformedConfigurationException
      throw new RuntimeException("Encountered root dataset without source declaration: " + dataset);
    }
    return () -> modelReader.readModel(s);
  }


  /**
   *
   *
   * @param operator
   * @return Implementation of IModule defined by the given resource's rdf:type
   */
  private EnrichmentOperator getOperator(Resource operator) {
    Resource implementation = operator.getPropertyResourceValue(BaseVocabulary.implementedIn);
    if (implementation == null) {
      throw new RuntimeException("Implementation type of enrichment " + operator + " is not specified!");
    }
    return pluginFactory.create(implementation.getURI());
  }

  /**
   * Return instance of ModelWriter for a dataset, configured by relevant RDF properties
   *
   * @param datasetUri URI of the Resource describing the dataset and its configuration
   * @return Configured Instance of ModelWriter
   */
  @Nullable
  private Consumer<Model> getWriter(Resource datasetUri) {
    Statement outputStatement = datasetUri.getProperty(BaseVocabulary.outputFile);
    Statement formatStatement = datasetUri.getProperty(BaseVocabulary.outputFormat);
    if (outputStatement == null) {
      return null;
    }
    String outputFile = outputStatement.getString();
    if (formatStatement == null) {
      return new ModelWriter(outputFile);
    } else {
      return new ModelWriter(outputFile, formatStatement.getString());
    }
  }

}
