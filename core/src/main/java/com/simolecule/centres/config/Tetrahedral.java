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
import java.util.List;

public class Tetrahedral<A, B> extends Configuration<A, B> {

  public static final int LEFT  = 0x1;
  public static final int RIGHT = 0x2;

  public Tetrahedral(A focus, A[] carriers, int cfg)
  {
    super(focus, carriers, cfg);
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc)
  {
    mol.setAtomProp(getFocus(), BaseMol.CIP_LABEL_KEY, desc);
  }

  @Override
  public Descriptor label(SequenceRule<A, B> comp)
  {
    Digraph<A, B> digraph = getDigraph();
    Node<A, B>    root    = digraph.getRoot();
    if (root == null)
      root = digraph.init(getFocus());
    else
      digraph.changeRoot(digraph.getRoot());

    return label(root, comp);
  }

  private Descriptor label(Node<A, B> node, SequenceRule<A, B> comp)
  {
    A focus = getFocus();
    final List<Edge<A, B>> edges = new ArrayList<>(node.getEdges());

    // something not right!?! bad creation
    if (edges.size() < 3)
      return Descriptor.ns;

    Priority priority = comp.sort(node, edges);
    boolean isUnique = priority.isUnique();
    if (!isUnique && edges.size() == 4) {
      if (comp.getNumSubRules() == 3)
        return Descriptor.Unknown;
      List<List<Edge<A,B>>> partition = comp.getSorter().getGroups(edges);
      if (partition.size() == 2) {
        // a a' b b' and a a' a'' b
        node.getDigraph().setRule6Ref(edges.get(1).getEnd().getAtom());
        priority = comp.sort(node, edges);
        node.getDigraph().setRule6Ref(null);
      } else if (partition.size() == 1) {
        // S4 symmetric case
        node.getDigraph().setRule6Ref(edges.get(0).getEnd().getAtom());
        comp.sort(node, edges);
        Edge[] nbrs1 = edges.toArray(new Edge[4]);
        node.getDigraph().setRule6Ref(edges.get(1).getEnd().getAtom());
        priority = comp.sort(node, edges);
        Edge[] nbrs2 = edges.toArray(new Edge[4]);
        if (parity4(nbrs1, nbrs2) == 1)
          return Descriptor.Unknown;
        node.getDigraph().setRule6Ref(null);
      }
      if (!priority.isUnique())
        return Descriptor.Unknown;
    } else if (!isUnique) {
      return Descriptor.Unknown;
    }

    Object[] ordered = new Object[4];
    int      idx     = 0;
    for (Edge<A, B> edge : edges) {
      if (edge.getEnd().isSet(Node.BOND_DUPLICATE) ||
          edge.getEnd().isSet(Node.IMPL_HYDROGEN))
        continue;
      ordered[idx++] = edge.getEnd().getAtom();
    }
    if (idx < 4)
      ordered[idx] = focus;

    if (node.getDigraph().getRoot() == node)
      Stats.INSTANCE.countRule(priority.getRuleIdx());

    int parity = parity4(ordered, getCarriers());

    if (parity == 0)
      throw new RuntimeException("Could not calculate parity! Carrier mismatch");

    int config = this.getConfig();
    if (parity == 1)
      config ^= 0x3;

    if (config == 0x1) {
      if (priority.isPseduoAsymettric())
        return Descriptor.s;
      else
        return Descriptor.S;
    } else if (config == 0x2) {
      if (priority.isPseduoAsymettric())
        return Descriptor.r;
      else
        return Descriptor.R;
    }

    return Descriptor.Unknown;
  }

  @Override
  public Descriptor label(Node<A, B> node, Digraph<A,B> digraph, SequenceRule<A, B> comp) {
    digraph.changeRoot(node);
    return label(node, comp);
  }
}
