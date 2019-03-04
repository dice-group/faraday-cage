package org.aksw.faraday_cage.engine;

/**
 * Custom Exception for invalid ExecutionGraphs
 */
public class InvalidExecutionGraphException extends RuntimeException {

  public InvalidExecutionGraphException(String message) {
    super(message);
  }

  public InvalidExecutionGraphException(String message, Throwable cause) {
    super(message, cause);
  }


}
