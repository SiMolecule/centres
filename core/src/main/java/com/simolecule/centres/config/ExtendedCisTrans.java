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
import com.simolecule.centres.rules.SequenceRule;

import java.util.ArrayList;
import java.util.List;

/**
 * This configurations is used to describe the extended cis-trans specification
 * of cumulated Sp2 bonds. It described in terms of two foci (the atoms at each
 * end) and two carries (each attached to either end atom). The arrangement of the
 * carriers can either be {@link #OPPOSITE} (trans) or {@link #TOGETHER} (cis).
 * Importantly carrier[0] should be bonded to foci[0] and carrier[1] to
 * foci[1]. If there are additional atoms bonded they should be ignored, it
 * does not matter which atom is ignored as the labeler will inspect the
 * molecule and flip the labelling if needed. Only odd numbers of double bonds
 * make cis-trans, even numbers lead to {@link ExtendedTetrahedral}.
 * The central bond is also provided as to where the label should be set.
 *
 * <pre>
 *    c[0]                   c[1]
 *     \                    /
 *      \                  /
 *       f[0] == x == x == f[1]
 *                         \
 *                          \
 *                           x - ignored atom
 * "Together"
 * </pre>
 *
 * <pre>
 *    c[0]
 *     \
 *      \
 *       f[0] == x == x == f[1]
 *                         \
 *                          \
 *                           c[1]
 * "Opposite"
 * </pre>
 *
 *
 * @param <A> the atom class
 * @param <B> the bond class
 */
public class ExtendedCisTrans<A, B> extends Configuration<A, B> {

  public static final int OPPOSITE = 0x1;
  public static final int TOGETHER = 0x2;

  private final B bond;

  public ExtendedCisTrans(B bond, A[] foci, A[] carriers, int cfg)
  {
    super(foci, carriers, cfg);
    this.bond = bond;
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc)
  {
    mol.setBondProp(bond, BaseMol.CIP_LABEL_KEY, desc);
  }

  private Edge<A, B> findDoubleBond(List<Edge<A, B>> edges, BaseMol<A,B> mol)
  {
    for (Edge<A, B> edge : edges) {
      if (!edge.getEnd().isDuplicate() && mol.getBondOrder(edge.getBond()) == 2)
        return edge;
    }
    return null;
  }

  @Override
  public Descriptor label(SequenceRule<A, B> comp)
  {
    final A focus1 = getFoci()[0];

    Digraph<A,B> digraph = getDigraph();
    if (digraph.getCurrRoot() == null)
      digraph.init(focus1);
    Node<A,B> root1 = digraph.getRoot();
    return label(root1, digraph, comp);
  }

  @Override
  public Descriptor label(Node<A, B>    root1,
                          Digraph<A, B> digraph,
                          SequenceRule<A, B>   comp) {



    final A focus1 = getFoci()[0];
    final A focus2 = getFoci()[1];

    digraph.changeRoot(root1);

    List<Edge<A, B>> edges1 = new ArrayList<>(root1.getEdges());
    edges1.remove(findDoubleBond(edges1, digraph.getMol()));
    removeDuplicatedEdges(edges1);

    Priority priority1 = comp.sort(root1, edges1);
    if (!priority1.isUnique())
      return Descriptor.Unknown;

    Node<A,B> root2 = null;
    for (Node<A,B> node : digraph.getNodes(focus2)) {
      if (node.isDuplicate())
        continue;
      if (root2 == null || node.getDistance() < root2.getDistance())
        root2 = node;
    }
    if (root2 == null)
      return Descriptor.Unknown;

    digraph.changeRoot(root2);
    List<Edge<A, B>> edges2 = new ArrayList<>(root2.getEdges());
    edges2.remove(findDoubleBond(edges2, digraph.getMol()));
    removeDuplicatedEdges(edges2);

    Priority priority2 = comp.sort(root2, edges2);
    if (!priority2.isUnique())
      return Descriptor.Unknown;

    A[] carriers = getCarriers();
    int config   = getConfig();

    // swap
    if (carriers[0].equals(edges1.get(0).getEnd().getAtom()) !=
        carriers[1].equals(edges2.get(0).getEnd().getAtom()))
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
}
