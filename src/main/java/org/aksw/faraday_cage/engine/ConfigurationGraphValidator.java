/*
 * FARADAY-CAGE - Framework for acyclic directed graphs yielding parallel computations of great efficiency
 * Copyright © 2018 Data Science Group (DICE) (kevin.dressler@uni-paderborn.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    return ValidationUtil.validateModel(configGraph, validationModel, true);
  }

  static boolean isConformingValidationReport(Resource validationReport) {
    return Objects.nonNull(validationReport) &&
      validationReport.getProperty(SH.conforms).getBoolean();
  }

}

