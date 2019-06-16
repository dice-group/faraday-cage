package org.aksw.faraday_cage.engine;

import java.util.List;

/**
 *
 */
public class ExecutionGraph<T> {

  protected final List<ExecutionNode<T>> ops;
  protected final short[][] entries;

  public ExecutionGraph(List<ExecutionNode<T>> ops) {
    this.ops = ops;
    this.entries = new short[ops.size()][2];
  }

  public void addRow(short i, short[] row) {
    entries[i] = row;
  }

  public int getSize() {
    return ops.size();
  }

  public ExecutionNode<T> getNode(int i) {
    return ops.get(i);
  }

  public short[] getRow(int i) {
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