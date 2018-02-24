package org.aksw.faraday_cage;


import java.util.List;

/**
 *
 *
 *
 */
public interface Execution<T> {

  T apply(T data);

  List<T> apply(List<T> data);


}
