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
import java.util.Iterator;
import java.util.List;

/**
 * This configurations is used to describe the extended tetrahedral specification
 * of cumulated Sp2 bonds. It described in terms of two foci (the atoms at each
 * end) and four carries (each attached to either end atom). The arrangement of the
 * carriers can either be {@link #LEFT} or {@link #RIGHT}.
 * Importantly carrier[0,1] should be bonded to foci[1] and carrier[1,2] to
 * foci[1]. The first foci, foci[0], is used to determine where the label is
 * set.
 *
 * <pre>
 *    c[0]                  c[2]
 *     \                   /
 *      \                 /
 *       f[1] == f[0] == f[2]
 *      /                 \
 *     /                   \
 *   c[1]                   c[3]
 * </pre>
 *
 * The relative position of the carriers is described the same as {@link Tetrahedral}
 * as if the foci where all laid on top of each other:
 *
 * <pre>
 *    c[0]   c[2]
 *     \    /
 *      \  /            - if the bond f[2]..c[0] pointed towards us the carriers
 *       f[1,0,2]         c[1..3] are arranged clockwise/right-handed
 *      /  \            - if the bond f[2]..c[0] pointed away from us the carriers
 *     /    \             c[1..3] are arranged anti-clockwise/left-handed
 *   c[1]    c[3]
 * </pre>
 *
 *
 * @param <A> the atom class
 * @param <B> the bond class
 */
public final class ExtendedTetrahedral<A, B> extends Configuration<A, B> {

  public static final int LEFT  = 0x1;
  public static final int RIGHT = 0x2;

  public ExtendedTetrahedral(A[] foci, A[] carriers, int cfg)
  {
    super(foci, carriers, cfg);
    if (foci.length != 3)
      throw new IllegalArgumentException("Only X=X=X cumulenes are currently supported.");
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc)
  {
    // we store the label on the middle atom
    mol.setAtomProp(getFocus(), BaseMol.CIP_LABEL_KEY, desc);
  }

  private List<Edge<A,B>> getEdges(Node<A, B> node) {
    List<Edge<A, B>> edges = node.getEdges();
    List<Edge<A,B>>  res   = new ArrayList<>();
    for (Edge<A,B> edge : edges) {
      if (!edge.getBeg().equals(node))
        continue;
      if (edge.getEnd().isDuplicate())
        continue;
      res.add(edge);
    }
    return res;
  }

  private Node<A,B> findOther(Node<A,B> node, Node<A,B> skip) {
    BaseMol<A, B> mol = node.getDigraph().getMol();
    for (Edge<A,B> e : getEdges(node)) {
      if (e.getEnd().equals(skip))
        continue;
      if (e.getBond() == null ||
          mol.getBondOrder(e.getBond()) != 2)
        continue;
      return e.getEnd();
    }
    return null;
  }

  private Node<A,B>[] getEnds(Node<A,B> root) {
    List<Edge<A,B>> edges = getEdges(root);
    Node<A,B> pEnd1 = root;
    Node<A,B> pEnd2 = root;
    Node<A,B> end1  = edges.get(0).getEnd();
    Node<A,B> end2  = edges.get(1).getEnd();
    Node<A,B> tmp;
    while (end1 != null && end2 != null) {
      tmp = findOther(end1, pEnd1);
      pEnd1 = end1;
      end1  = tmp;
      tmp = findOther(end2, pEnd2);
      pEnd2 = end2;
      end2  = tmp;
    }
    return (Node<A,B>[]) new Node[]{pEnd1, pEnd2};
  }

  @Override
  public Descriptor label(SequenceRule<A, B> comp)
  {
    Digraph<A, B> digraph = getDigraph();
    Node<A, B>    root;
    if (digraph.getRoot() == null)
      root = digraph.init(getFocus());
    else
      root = digraph.getRoot();
    digraph.changeRoot(root);
    Node<A,B>[] ends = getEnds(root);

    Node<A, B> end1 = ends[0];
    Node<A, B> end2 = ends[1];

    A atom1 = getFoci()[1];
    A atom2 = getFoci()[2];

    // swap to make things easier when comparing to reference ordering
    if (end1.getAtom() == atom2 && end2.getAtom() == atom1) {
      A tmp = atom1;
      atom1 = atom2;
      atom2 = tmp;
    } else if (end1.getAtom() != atom1 && end2.getAtom() != atom2) {
      System.err.println("Stereo Focus mismatch!");
      return Descriptor.Unknown;
    }

    return label2(comp, end1, end2, atom1, atom2, getEdges(end1), getEdges(end2));
  }

  private void removeDuplicates(List<Edge<A,B>> e) {
    Iterator<Edge<A,B>> iter = e.iterator();
    while (iter.hasNext()) {
      if (iter.next().getEnd().isDuplicate())
        iter.remove();
    }
  }

  @Override
  public Descriptor label(Node<A, B>    node,
                          Digraph<A, B> digraph,
                          SequenceRule<A, B>   comp) {

    A[] foci = getFoci();
    A focus = foci[0];

    if (focus != node.getAtom())
      return Descriptor.Unknown;

    Node<A,B> root = node;
    digraph.changeRoot(root);
    List<Edge<A, B>> edges = root.getEdges();
    removeDuplicates(edges);
    if (edges.size() != 2)
      return Descriptor.Unknown;

    Node<A, B> end1 = edges.get(0).getEnd();
    Node<A, B> end2 = edges.get(1).getEnd();

    A atom1 = getFoci()[1];
    A atom2 = getFoci()[2];

    // swap to make things easier when comparing to reference ordering
    if (end1.getAtom() == atom2 && end2.getAtom() == atom1) {
      A tmp = atom1;
      atom1 = atom2;
      atom2 = tmp;
    } else if (end1.getAtom() != atom1 && end2.getAtom() != atom2) {
      System.err.println("Stereo Focus mismatch!");
      return Descriptor.Unknown;
    }

    List<Edge<A, B>> edges1 = new ArrayList<>(end1.getEdges());
    List<Edge<A, B>> edges2 = new ArrayList<>(end2.getEdges());
    removeDuplicates(edges1);
    removeDuplicates(edges2);
    edges1.remove(edges.get(0));
    edges2.remove(edges.get(1));

    return label2(comp, end1, end2, atom1, atom2, edges1, edges2);
  }

  private Descriptor label2(SequenceRule<A, B> comp,
                            Node<A, B> end1, Node<A, B> end2,
                            A atom1, A atom2,
                            List<Edge<A, B>> edges1,
                            List<Edge<A, B>> edges2) {
    if (edges1.size() > 2)
      return Descriptor.Unknown;
    if (edges2.size() > 2)
      return Descriptor.Unknown;

    BaseMol<A,B> mol = end1.getDigraph().getMol();

    end1.getDigraph().changeRoot(end1);
    Priority priority1 = comp.sort(end1, edges1);
    end2.getDigraph().changeRoot(end2);
    Priority priority2 = comp.sort(end2, edges2);
    if (!priority1.isUnique() && !priority2.isUnique() &&
        mol.isInRing(edges1.get(0).getBond()) &&
        mol.isInRing(edges2.get(0).getBond())) {
      end1.getDigraph().setRule6Ref(edges1.get(0).getEnd().getAtom());
      priority1 = comp.sort(end1, edges1);
      priority2 = comp.sort(end2, edges2);
      end1.getDigraph().setRule6Ref(null);
    }
    if (!priority1.isUnique() || !priority2.isUnique())
      return Descriptor.Unknown;

    Object[] ordered = new Object[4];
    ordered[0] = edges1.get(0).getEnd().getAtom();
    ordered[1] = edges1.size() > 1 ? edges1.get(1).getEnd().getAtom() : atom1;
    ordered[2] = edges2.get(0).getEnd().getAtom();
    ordered[3] = edges2.size() > 1 ? edges2.get(1).getEnd().getAtom() : atom2;

    if (ordered[1] == null)
      ordered[1] = atom1;
    if (ordered[3] == null)
      ordered[3] = atom2;

    int parity = parity4(ordered, getCarriers());

    if (parity == 0)
      return Descriptor.Unknown;
    // throw new RuntimeException("Could not calculate parity! Carrier mismatch");

    int config = this.getConfig();
    if (parity == 1)
      config ^= 0x3;

    Stats.INSTANCE.countRule(Math.max(priority1.getRuleIdx(),
                                      priority2.getRuleIdx()));

    if (config == RIGHT) {
      if (priority1.isPseduoAsymettric() != priority2.isPseduoAsymettric())
        return Descriptor.m;
      else
        return Descriptor.M;
    } else if (config == LEFT) {
      if (priority1.isPseduoAsymettric() != priority2.isPseduoAsymettric())
        return Descriptor.p;
      else
        return Descriptor.P;
    }
    return null;
  }
}
