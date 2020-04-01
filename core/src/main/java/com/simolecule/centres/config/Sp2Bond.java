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

package com.simolecule.centres.config;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Descriptor;
import com.simolecule.centres.Digraph;
import com.simolecule.centres.Edge;
import com.simolecule.centres.Node;
import com.simolecule.centres.Stats;
import com.simolecule.centres.rules.Priority;
import com.simolecule.centres.rules.Rules;
import com.simolecule.centres.rules.SequenceRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Sp2Bond<A, B> extends Configuration<A, B> {

  public static final int OPPOSITE = 0x1;
  public static final int TOGETHER = 0x2;

  private B bond;

  public Sp2Bond(B bond, A[] foci, A[] carriers, int cfg)
  {
    super(foci, carriers, cfg);
    this.bond = bond;
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc)
  {
    mol.setBondProp(bond, BaseMol.CIP_LABEL_KEY, desc);
  }

  @Override
  public Descriptor label(SequenceRule<A, B> comp)
  {
    final Digraph<A, B> digraph = getDigraph();

    final A focus1 = getFoci()[0];
    final A focus2 = getFoci()[1];

    Node<A, B> root1 = digraph.getRoot();
    if (root1 == null)
      root1 = digraph.init(focus1);
    else
      digraph.changeRoot(root1);

    Edge<A,B> internal = findInternalEdge(root1.getEdges(), focus1, focus2);

    List<Edge<A, B>> edges1 = new ArrayList<>(root1.getEdges());
    edges1.remove(internal);

    Priority priority1 = comp.sort(root1, edges1);
    if (!priority1.isUnique())
      return Descriptor.Unknown;

    Node<A,B> root2 = internal.getOther(root1);
    digraph.changeRoot(root2);
    List<Edge<A, B>> edges2 = new ArrayList<>(root2.getEdges());
    edges2.remove(internal);

    Priority priority2 = comp.sort(root2, edges2);
    if (!priority2.isUnique())
      return Descriptor.Unknown;

    A[] carriers = getCarriers();
    int config   = getConfig();

    // swap
    if (edges1.size() > 1 && carriers[0].equals(edges1.get(1).getEnd().getAtom()))
      config ^= 0x3;
    // swap
    if (edges2.size() > 1 && carriers[1].equals(edges2.get(1).getEnd().getAtom()))
      config ^= 0x3;

    Stats.INSTANCE.countRule(Math.max(priority1.getRuleIdx(), priority2.getRuleIdx()));

    if (config == TOGETHER) {
      if (priority1.isPseduoAsymettric() !=
          priority2.isPseduoAsymettric())
        return Descriptor.seqCis;
      else
        return Descriptor.Z;
    } else if (config == OPPOSITE) {
      if (priority1.isPseduoAsymettric() !=
          priority2.isPseduoAsymettric())
        return Descriptor.seqTrans;
      else
        return Descriptor.E;
    }

    return Descriptor.Unknown;
  }

  @Override
  public Descriptor label(Node<A, B>    root1,
                          Digraph<A, B> digraph,
                          SequenceRule<A, B>   rules) {
    A focus1 = getFoci()[0];
    A focus2 = getFoci()[1];

    Edge<A,B> internal = findInternalEdge(root1.getEdges(), focus1, focus2);
    if (internal == null)
      return Descriptor.Unknown;
    Node<A,B> root2 = internal.getOther(root1);

    List<Edge<A,B>> edges1 = new ArrayList<>(root1.getEdges());
    List<Edge<A,B>> edges2 = new ArrayList<>(root2.getEdges());
    removeInternalEdges(edges1, focus1, focus2);
    removeInternalEdges(edges2, focus1, focus2);

    A[] carriers = getCarriers().clone();
    int config   = getConfig();

    if (root1.getAtom().equals(focus2)) {
      A tmp = carriers[1];
      carriers[1] = carriers[0];
      carriers[0] = tmp;
    }

    digraph.changeRoot(root1);
    Priority priority1 = rules.sort(root1, edges1);
    if (!priority1.isUnique())
      return Descriptor.Unknown;
    // swap
    if (edges1.size() > 1 && carriers[0].equals(edges1.get(1).getEnd().getAtom()))
      config ^= 0x3;
    digraph.changeRoot(root2);
    Priority priority2 = rules.sort(root2, edges2);
    if (!priority2.isUnique())
      return Descriptor.Unknown;
    // swap
    if (edges2.size() > 1 && carriers[1].equals(edges2.get(1).getEnd().getAtom()))
      config ^= 0x3;

    if (config == TOGETHER) {
      if (priority1.isPseduoAsymettric() !=
          priority2.isPseduoAsymettric()) {
        return Descriptor.seqCis;
      } else {
        return Descriptor.Z;
      }
    } else if (config == OPPOSITE) {
      if (priority1.isPseduoAsymettric() !=
          priority2.isPseduoAsymettric()) {
        return Descriptor.seqTrans;
      } else {
        return Descriptor.E;
      }
    }
    return Descriptor.Unknown;
  }
}
