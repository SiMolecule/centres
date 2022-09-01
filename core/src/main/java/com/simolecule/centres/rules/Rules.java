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

  private static final boolean SORT_BRANCHES_WITH_RULE5 = false;

  /**
   * Rule storage
   */
  private final List<SequenceRule<A, B>> rules = new ArrayList<>();


  public Rules(SequenceRule<A, B>... rules) {
    super(null);
    for (SequenceRule<A, B> rule : rules)
      add(rule);
  }

  public void add(SequenceRule<A, B> rule) {
    if (rule == null)
      throw new NullPointerException("No sequence rule provided");
    rules.add(rule);
    rule.setSorter(createSorter(rules));
  }


  public Sort<A, B> createSorter(List<SequenceRule<A, B>> rules) {
    List<SequenceRule<A, B>> subrules = new ArrayList<>(rules.size());
    for (SequenceRule<A, B> rule : rules) {
      if (!SORT_BRANCHES_WITH_RULE5 && rule instanceof Rule5)
        continue;
      subrules.add(rule);
    }
    return new Sort<>(subrules);
  }

  @Override
  public int getNumSubRules() {
    return rules.size();
  }

  public Sort<A, B> getSorter() {
    return new Sort<>(rules);
  }

  @Override
  public BaseMol<A, B> getMol() {
    BaseMol<A, B> res = null;
    for (SequenceRule<A, B> rule : rules) {
      res = rule.getMol();
      if (res != null)
        break;
    }
    return res;
  }

  @Override
  public int compare(Edge<A, B> o1, Edge<A, B> o2) {
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
  public int getComparision(Edge<A, B> a, Edge<A, B> b, boolean deep) {
    // Try using each rules. The rules will expand the search exhaustively
    // to all child ligands
    for (SequenceRule<A, B> rule : rules) {

      // compare expands exhaustively across the whole graph
      int value = rule.recursiveCompare(a, b);

      if (value != 0)
        return value;

    }

    return 0;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("Combined rules:");
    for (SequenceRule<A, B> rule : rules)
      builder.append(rule.toString()).append(", ");
    return builder.toString();
  }
}
