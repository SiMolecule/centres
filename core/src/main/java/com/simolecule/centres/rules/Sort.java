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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.simolecule.centres.rules;

import com.simolecule.centres.Edge;
import com.simolecule.centres.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple insertion sort for ligands. The number of ligands is not likely to
 * be very larger as such doing a merge sort would have little benefit.
 *
 * @author John May
 */
public class Sort<A, B> {

  private int ruleMax = 0;

  private final List<SequenceRule<A, B>> rules = new ArrayList<>(5);

  public Sort(SequenceRule<A, B> comparator)
  {
    this.rules.add(comparator);
  }

  public Sort(List<SequenceRule<A, B>> comparators)
  {
    rules.addAll(comparators);
  }

  public List<SequenceRule<A, B>> getRules()
  {
    return Collections.unmodifiableList(rules);
  }

  public Priority prioritise(Node<A, B> node, List<Edge<A, B>> edges)
  {
    return prioritise(node, edges, true);
  }

  public Priority prioritise(Node<A, B> node, List<Edge<A, B>> edges, boolean deep)
  {
    Boolean unique        = Boolean.TRUE;
    int     numPseudoAsym = 0;

    for (int i = 0; i < edges.size(); i++)
      for (int j = i; j > 0; j--) {

        int cmp = compareLigands(node, edges.get(j - 1), edges.get(j), deep);

        if (cmp < -1 || cmp > +1)
          numPseudoAsym++;

        if (cmp < 0) {
          swap(edges, j, j - 1);
        } else {
          if (cmp == 0)
            unique = false;
          break;
        }
      }

    return new Priority(unique, ruleMax, numPseudoAsym == 1);
  }

  public final int compareLigands(Node<A, B> node, Edge<A, B> a, Edge<A, B> b, boolean deep)
  {
    // ensure 'up' edges are moved to the front
    if (!a.isBeg(node) && b.isBeg(node))
      return +1;
    else if (a.isBeg(node) && !b.isBeg(node))
      return -1;

    for (int i = 0; i < rules.size(); i++) {
      SequenceRule<A, B> rule = rules.get(i);
      int                cmp  = rule.getComparision(a, b, deep);
      if (cmp != 0) {
        ruleMax = Math.max(ruleMax, i);
        return cmp;
      }
    }
    return 0;
  }


  public void swap(List<Edge<A, B>> list, int i, int j)
  {
    Edge<A, B> tmp = list.get(i);
    list.set(i, list.get(j));
    list.set(j, tmp);
  }


  public List<List<Edge<A, B>>> getGroups(List<Edge<A, B>> sorted)
  {

    // would be nice to have this integrated whilst sorting - may provide a small speed increase
    // but as most of our lists are small we take use ugly sort then group approach
    List<List<Edge<A, B>>> groups = new ArrayList<>();

    Edge<A, B> prev = null;
    for (Edge<A, B> edge : sorted) {
      if (prev == null || compareLigands(prev.getBeg(), prev, edge, true) != 0)
        groups.add(new ArrayList<Edge<A, B>>());
      prev = edge;
      groups.get(groups.size() - 1).add(edge);
    }

    return groups;

  }

}
