/*
 * Copyright (c) 2012. John May
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.simolecule.centres.rules;

import com.google.common.collect.Lists;
import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Edge;

import java.util.ArrayList;
import java.util.List;

/**
 * A priority rules made up of other rules. Each sub-rules is used exhaustively on
 * the digraph before the next one is applied.
 *
 * @author John May
 */
public class Rules<A, B> extends SequenceRule<A, B> {

  /**
   * Rule storage
   */
  private final List<SequenceRule<A, B>> rules = new ArrayList<>();


  public Rules(SequenceRule<A, B> ... rules)
  {
    super(null);
    for (SequenceRule<A, B> rule : rules)
      add(rule);
  }

  public void add(SequenceRule<A, B> rule)
  {
    if (rule == null)
      throw new NullPointerException("No sequence rule provided");
    rules.add(rule);
    rule.setSorter(createSorter(rules));
  }

  public Sort<A, B> createSorter(List<SequenceRule<A, B>> rules)
  {
    return new Sort<A, B>(rules); // restriction should be configurable
  }

  @Override
  public BaseMol<A, B> getMol()
  {
    BaseMol<A,B> res = null;
    for (SequenceRule<A,B> rule : rules) {
      res = rule.getMol();
      if (res != null)
        break;
    }
    return res;
  }

  @Override
  public int compare(Edge<A, B> o1, Edge<A, B> o2)
  {
    // Try using each rules. The rules will expand the search exhaustively
    // to all child ligands
    for (SequenceRule<A, B> rule : rules) {
      // compare expands exhaustively across the whole graph
      int value = rule.recursiveCompare(o1, o2);
      if (value != 0) return value;
    }
    return 0;
  }

  @Override
  public int getComparision(Edge<A, B> a, Edge<A, B> b, boolean deep)
  {
    // Try using each rules. The rules will expand the search exhaustively
    // to all child ligands
    for (SequenceRule<A,B> rule : rules) {

      // compare expands exhaustively across the whole graph
      int value = rule.recursiveCompare(a, b);

      if (value != 0) {
        if (rule.isPseudoAsymmetric())
          return 2 * value;
        else
          return value;
      }

    }

    return 0;
  }

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder("Combined rules:");
    for (SequenceRule<A,B> rule : rules)
      builder.append(rule.toString()).append(", ");
    return builder.toString();
  }
}
