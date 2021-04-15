/*
 * FARADAY-CAGE - Framework for acyclic directed graphs yielding parallel computations of great efficiency
 * Copyright © 2018 Data Science Group (DICE) (kevin.dressler@uni-paderborn.de)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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