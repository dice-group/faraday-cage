package org.aksw.faraday_cage.execution;

import java.util.List;

/**
 *
 *
 *
 */
public interface HubExecution<T> {

  List<T> apply(List<T> data);
  
}
