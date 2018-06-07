package org.aksw.faraday_cage;


import org.aksw.faraday_cage.nodes.DefaultAnalyticsFrame;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 *
 *
 *
 */
public interface Execution<T> {

  static <T> UnaryOperator<T> toSingleExecution(UnaryOperator<List<T>> multiExecution) {
    return (data -> {
      List<T> dates = multiExecution.apply(List.of(data));
      return dates.isEmpty() ? null : dates.get(0);
    });
  }

  static <T> UnaryOperator<List<T>> toMultiExecution(UnaryOperator<T> singleExecution) {
    return (dates -> List.of(singleExecution.apply(dates.isEmpty() ? null : dates.get(0))));
  }

  T apply(T data);

  List<T> apply(List<T> data);

  default AnalyticsFrame gatherAnalytics() {
    return new DefaultAnalyticsFrame();
  }

  @NotNull Resource getId();



}
