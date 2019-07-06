package org.aksw.faraday_cage.util;

import org.aksw.faraday_cage.engine.ExecutionGraph;
import org.aksw.faraday_cage.engine.ExecutionNode;
import org.aksw.faraday_cage.engine.Parameterized;
import org.aksw.faraday_cage.vocabulary.FCAGE;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

/**
 *
 */
public class ExecutionGraphSerializer {

  public static Model serialize(ExecutionGraph executionGraph) {
    int n = executionGraph.getSize();
    Model serialized = ModelFactory.createDefaultModel();
    for (int i = 0; i < n; i++) {
      int[] row = executionGraph.getRow(i);
      Resource[] inputDefs = new Resource[row[0]];
      if (row[0] > 0) {
        for (int j = 2; j < 2 + row[0] * 2; j+=2) {
          (inputDefs[(j/2)-1] = serialized.createResource())
            .addProperty(FCAGE.fromNode, executionGraph.getNode(row[j]).getId())
            .addProperty(FCAGE.fromPort, String.valueOf(row[j+1]), XSDDatatype.XSDinteger);
        }
      }
      ExecutionNode node = executionGraph.getNode(i);
      Resource nodeResource = node.getId().inModel(serialized)
        .addProperty(RDF.type, node.getType());
      if (inputDefs.length > 0) {
        nodeResource.addProperty(FCAGE.hasInput, serialized.createList(inputDefs));
      }
      if (node instanceof Parameterized) {
        Parameterized pNode = (Parameterized) node;
        serialized.add(pNode.getParameterMap().parametrize(nodeResource));
      }
    }
    return serialized;
  }
}