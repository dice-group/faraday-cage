package org.aksw.faraday_cage.execution;


/**
 *
 *
 *
 */
public interface Execution<T> {

  T apply(T data);

}
