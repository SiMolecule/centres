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

import com.simolecule.centres.*;

import java.util.*;

/**
 * A descriptor pair rule. This rule defines that like descriptor pairs have
 * priority over unlike descriptor pairs.
 *
 * @author John May
 */
public class Rule5New<A, B>
        extends SequenceRule<A, B> {

  private final Descriptor ref;

  public Rule5New(BaseMol<A, B> mol)
  {
    super(mol);
    ref = null;
  }

  public Rule5New(BaseMol<A, B> mol, Descriptor ref)
  {
    super(mol);
    this.ref = ref;
  }

  @Override
  public boolean isPseudoAsymmetric()
  {
    return true;
  }

  private void fillPairs(Node<A, B> beg, PairList plist)
  {
    Sort<A, B>        sorter = getRefSorter(plist.getRefDescriptor());
    Deque<Node<A, B>> queue  = new ArrayDeque<>();
    queue.add(beg);
    while (!queue.isEmpty()) {
      Node<A, B> node = queue.poll();
      plist.add(node.getAux());
      List<Edge<A, B>> edges = node.getEdges();
      sorter.prioritise(node, edges);
      for (Edge<A, B> edge : edges) {
        if (edge.isBeg(node) && !edge.getEnd().isTerminal()) {
          queue.add(edge.getEnd());
        }
      }
    }
  }

  private Sort<A, B> getRefSorter(Descriptor refA)
  {
    List<SequenceRule<A, B>> rules = new ArrayList<>(getSorter().getRules());
    rules.remove(this);
    rules.add(new Rule5New<>(getMol(), refA));
    return new Sort<>(rules);
  }

  @Override
  public int compare(Edge<A, B> a, Edge<A, B> b)
  {
    if (!a.getBeg().getDigraph().getCurrRoot().equals(a.getBeg()) ||
        !b.getBeg().getDigraph().getCurrRoot().equals(b.getBeg())) {
      if (ref == null)
        return 0;
      Descriptor aDesc = a.getEnd().getAux();
      Descriptor bDesc = b.getEnd().getAux();
      if (aDesc != null && bDesc != null && aDesc != Descriptor.ns && bDesc != Descriptor.ns) {
        boolean alike = PairList.ref(ref) == PairList.ref(aDesc);
        boolean blike = PairList.ref(ref) == PairList.ref(bDesc);
        if (alike && !blike)
          return +1;
        if (blike && !alike)
          return -1;
      }
      return 0;
    } else {
      PairList listRA = new PairList(Descriptor.R);
      PairList listRB = new PairList(Descriptor.R);
      PairList listSA = new PairList(Descriptor.S);
      PairList listSB = new PairList(Descriptor.S);
      fillPairs(a.getEnd(), listRA);
      fillPairs(a.getEnd(), listSA);
      fillPairs(b.getEnd(), listRB);
      fillPairs(b.getEnd(), listSB);
      int cmpR = listRA.compareTo(listRB);
      int cmpS = listSA.compareTo(listSB);
      // -2/+2 for psuedo-asymetric
      // -1/+1 if not (e.g. the R > R and S > S lists)
      if (cmpR < 0)
        return cmpS < 0 ? -1 : -2;
      else if (cmpR > 0)
        return cmpS > 0 ? +1 : +2;
      else
        return 0;
    }
  }
}
