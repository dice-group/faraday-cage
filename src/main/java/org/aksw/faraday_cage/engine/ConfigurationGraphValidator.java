package org.aksw.faraday_cage.engine;

import org.aksw.faraday_cage.decorator.ExecutionNodeWrapper;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

import java.util.List;
import java.util.Objects;

/**
 *
 *
 *
 */
class ConfigurationGraphValidator {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationGraphValidator.class);

  private static final Model baseValidationModel = JenaUtil.createMemoryModel();

  static {
    baseValidationModel.read(ConfigurationGraphValidator.class.getResourceAsStream("/shacl/fcage:ExecutionNode.ttl"), null, FileUtils.langTurtle);
  }

  private final DecoratedExecutionNodeFactory<? extends ExecutionNode<?>, ? extends ExecutionNodeWrapper<? extends ExecutionNode<?>, ?>, ?> factory;

  private final List<Resource> allPluginTypes;

  ConfigurationGraphValidator(DecoratedExecutionNodeFactory<? extends ExecutionNode<?>, ? extends ExecutionNodeWrapper<? extends ExecutionNode<?>, ?>, ?> factory) {
    this.factory = factory;
    this.allPluginTypes = factory.listAvailable();
    allPluginTypes.addAll(factory.getWrapperFactory().listAvailable());
  }

  private static Model getBaseValidationModel() {
    Model m = JenaUtil.createMemoryModel();
    m.add(baseValidationModel);
    return m;
  }

  Model getValidationModelFor(Resource id) {
    Plugin implementation;
    try {
      implementation = factory.getImplementationOf(id);
    } catch (RuntimeException e) {
      implementation = factory.getWrapperFactory().getImplementationOf(id);
    }
    if (implementation instanceof Parameterized) {
      return ((Parameterized) implementation).createParameterMap().getValidationModel();
    } else {
      return ModelFactory.createDefaultModel();
    }
  }

  Model getFullValidationModel() {
    final Model validationModel = getBaseValidationModel();
    allPluginTypes.stream()
      .map(this::getValidationModelFor)
      .forEach(validationModel::add);
    return validationModel;
  }

  Resource validate(Model configGraph) {
    final Model validationModel = getFullValidationModel();
    allPluginTypes.stream()
      .map(Resource::listProperties)
      .forEach(configGraph::add);
//    ModelFactory.createInfModel(ReasonerRegistry.getTransitiveReasoner(), configGraph)
//      .listStatements(null, RDF.type, (RDFNode) null)
//      .filterKeep(stmt -> stmt.getObject().asResource().hasProperty(RDFS.subClassOf, FCAGE.ExecutionNode))
//      .forEachRemaining(validationModel::add);
    Resource validationReport = ValidationUtil.validateModel(configGraph, validationModel, true);
//    Supplier<String> traceInfo = () -> {
//      ByteArrayOutputStream stream = new ByteArrayOutputStream();
//      configGraph.write(stream, "TTL");
//      validationModel.write(stream, "TTL");
//      try {
//        return stream.toString("UTF-8");
//      } catch (UnsupportedEncodingException e) {
//        // print and ignore
//        e.printStackTrace();
//      }
//      return "";
//    };
//    logger.trace("DEBUG INFO: GRAPHS FOR VALIDATION\n{}", traceInfo);
    return validationReport;
  }

  static boolean isConformingValidationReport(Resource validationReport) {
    return Objects.nonNull(validationReport) &&
      validationReport.getProperty(SH.conforms).getBoolean();
  }

}

