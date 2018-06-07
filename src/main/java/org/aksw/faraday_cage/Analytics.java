package org.aksw.faraday_cage;

import org.apache.jena.rdf.model.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 *
 */
public class Analytics {

  private Map<Resource, AnalyticsFrame> frames = new HashMap<>();

  public void gatherFrom(Execution Execution) {
    frames.put(Execution.getId(), Execution.gatherAnalytics());
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    frames.entrySet().forEach(entry -> {
      sb.append(entry.getKey());
      sb.append(":\n");
      sb.append(entry.getValue());
      sb.append("\n");
    });
    return sb.toString();
  }

}
