package org.aksw.faraday_cage.nodes;

import org.aksw.faraday_cage.AnalyticsFrame;

import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 *
 */
public class DefaultAnalyticsFrame implements AnalyticsFrame {

  private Map<String, String> entryMap = new HashMap<>();

  @Override
  public void put(String name, String information) {
    entryMap.put(name, information);
  }

  public String toString() {
    class LengthPair {
      int a, b;
      LengthPair(int a, int b) {
        this.a = a;
        this.b = b;
      }
    }
    StringBuilder sb = new StringBuilder();
    LengthPair maxLengths = entryMap.entrySet().stream()
      .map(entry -> new LengthPair(entry.getKey().length(), entry.getValue().length()))
      .reduce(new LengthPair(0, 0), (k, l) ->
        new LengthPair(k.a > l.a ? k.a : l.a, k.b > l.b ? k.b : l.b));
    entryMap.forEach((name, information) -> {
      sb.append(name);
      sb.append(":    ");
      for (int i = 0; i < maxLengths.a - name.length(); i++) {
        sb.append(" ");
      }
      sb.append(information);
    });
    return sb.toString();
  }

}
