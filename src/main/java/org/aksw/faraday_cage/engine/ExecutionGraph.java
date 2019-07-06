package org.aksw.faraday_cage.engine;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ExecutionGraph<T> {

  protected List<ExecutionNode<T>> ops;
  protected int[][] entries;

  public ExecutionGraph(int size) {
    this.ops = new ArrayList<>(size);
    for (int i = 0; i < size; i++) ops.add(null);
    this.entries = new int[size][2];
  }

  public void addRow(int i, ExecutionNode<T> op, int[] row) {
    entries[i] = row;
    ops.set(i,op);
  }

  public int getSize() {
    return entries.length;
  }

  public ExecutionNode<T> getNode(int i) {
    return ops.get(i);
  }

  public int[] getRow(int i) {
    return entries[i];
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < entries.length; i++) {
      sb.append(ops.get(i).getId());
      sb.append(" ");
      sb.append(entries[i][0]);
      sb.append(" ");
      sb.append(entries[i][1]);
      sb.append("\n");
    }
    return sb.toString();
  }

}