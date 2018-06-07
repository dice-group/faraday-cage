package org.aksw.faraday_cage;

/**
 *
 *
 *
 */
public class ExecutionGraph implements Runnable {

  private Runnable fn;
  private Analytics analytics;

  public ExecutionGraph(Runnable fn, Analytics analytics) {
    this.fn = fn;
    this.analytics = analytics;
  }

  @Override
  public void run() {
    fn.run();
  }

  public Analytics getAnalytics() {
    return analytics;
  }

}
