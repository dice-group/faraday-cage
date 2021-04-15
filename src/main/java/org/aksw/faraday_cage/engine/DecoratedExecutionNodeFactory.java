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
import org.aksw.faraday_cage.vocabulary.FCAGE;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.pf4j.PluginManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class DecoratedExecutionNodeFactory<U extends ExecutionNode<T>, V extends ExecutionNodeWrapper<U, T>, T> extends PluginFactory<U> {

  private PluginFactory<V> wrapperFactory;

  private List<Resource> universalWrappers = new ArrayList<>();

  private Map<Resource, List<Resource>> typeWrappers = new HashMap<>();

  private Map<Resource, List<Resource>> instanceWrappers = new HashMap<>();

  private List<Resource> types = listAvailable();


  /**
   * Constructor, takes an instance of this {@code DecoratedExecutionNodeFactory}s type parameters
   * {@code Class}.
   *
   * @param clazz
   *         {@code Class} instance for this {@code DecoratedExecutionNodeFactory}s type parameter
   * @param pluginManager
   *         the {@code PluginManager} to be used in this {@code DecoratedExecutionNodeFactory}
   */
  DecoratedExecutionNodeFactory(Class<U> clazz, Class<V> clazz2, PluginManager pluginManager, Model configModel) {
    super(clazz, pluginManager, FCAGE.ExecutionNode);
    registerWrappers(configModel);
    wrapperFactory = new PluginFactory<>(clazz2, pluginManager, FCAGE.ExecutionNodeWrapper);
  }

  private void registerWrappers(Model configModel) {
    configModel.listStatements(null, FCAGE.decoratedBy, (RDFNode) null)
      .forEachRemaining(stmt -> {
        Resource s = stmt.getSubject();
        RDFNode o = stmt.getObject();
        List<Resource> wrapperIds;
        if (o.canAs(RDFList.class)) {
          wrapperIds = o.as(RDFList.class)
            .iterator()
            .filterKeep(RDFNode::isResource)
            .mapWith(RDFNode::asResource)
            .toList();
        } else {
          wrapperIds = List.of(o.asResource());
        }
        if (s.equals(FCAGE.ExecutionNode)) {
          this.universalWrappers = wrapperIds;
        } else {
          registerWrappers(s, wrapperIds);
        }
      });
  }

  private void registerWrappers(Resource executionNode, List<Resource> executionNodeWrapper) {
    (types.contains(executionNode) ? typeWrappers : instanceWrappers).put(executionNode, executionNodeWrapper);
  }

  @Override
  public U getImplementationOf(Resource type) {
    U u = super.getImplementationOf(type);
    return wrap(u);
  }

  @Override
  public U create(Resource id) {
    U u = super.create(id);
    return wrap(u);
  }

  public PluginFactory<V> getWrapperFactory() {
    return wrapperFactory;
  }

  private U wrap(U u) {
    U wrapped = u;
    List<Resource> wrapperIds = new ArrayList<>(universalWrappers);
    if (typeWrappers.containsKey(u.getType())) {
      wrapperIds.addAll(typeWrappers.get(u.getType()));
    }
    if (instanceWrappers.containsKey(u.getId())) {
      wrapperIds.addAll(instanceWrappers.get(u.getId()));
    }
    for (Resource wrapperId : wrapperIds) {
        wrapped = createWrapper(wrapperId).wrap(wrapped);
    }
    return wrapped;
  }

  @SuppressWarnings("Duplicates")
  private V createWrapper(Resource wrapperId) {
    V wrapper = wrapperFactory.create(wrapperId);
    if (wrapper instanceof Parameterized) {
      ValidatableParameterMap parameterMap = ((Parameterized) wrapper).createParameterMap();
      parameterMap.populate(wrapperId);
      parameterMap.init();
      ((Parameterized) wrapper).initParameters(parameterMap);
    }
    return wrapper;
  }

}
