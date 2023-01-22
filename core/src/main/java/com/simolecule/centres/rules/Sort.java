/*
 * Copyright (c) 2020 John Mayfield
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
    boolean unique = true;
    boolean foundWildcard  = false;
    int     numPseudoAsym = 0;

    outer:
    for (int i = 0; i < edges.size(); i++) {
      for (int j = i; j > 0; j--) {

        int cmp = compareLigands(node, edges.get(j - 1), edges.get(j), deep);


        if (cmp == SequenceRule.COMP_TO_WILDCARD) {
          unique = false;
          foundWildcard = true;
          break outer;
        }

        // -2/+2 means we used Rule 5 (or more) and the ligands are mirror
        // images
        if (cmp < -1 || cmp > 1)
          numPseudoAsym++;

        if (cmp < 0) {
          swap(edges, j, j - 1);
        } else {
          if (cmp == 0)
            unique = false;
          break;
        }
      }
    }

    return new Priority(unique, foundWildcard, ruleMax, numPseudoAsym == 1);
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
