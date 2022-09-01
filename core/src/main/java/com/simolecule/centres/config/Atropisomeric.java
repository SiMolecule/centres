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
 * An atropisomer describes the configuration around an Ar-Ar single bond. It
 * works similar to extended-tetrahedral (and tetrahedral) where-by we describe
 * the carriers as {@link #LEFT} or {@link #RIGHT} around the central bond as
 * if the foci were laid out ontop of each other. The bond is provided as to
 * where the label should be set.
 *
 * <pre>
 *       x
 *      /
 *     /
 * === c[0]           c[2] --- x
 *      \            //         \\
 *       \          //           \\
 *        f[0] --- f[1]           x
 *      //          \            /
 *     //            \          /
 * --- c[1]           c[3] === x
 *     \             /
 *      \           /
 *       x         x
 * </pre>
 *
 * @param <A> the atom class
 * @param <B> the bond class
 * @see ExtendedTetrahedral
 */
public final class Atropisomeric<A, B> extends Configuration<A, B> {

  public static final int LEFT  = 0x1;
  public static final int RIGHT = 0x2;
  private final B bond;

  public Atropisomeric(B bond, A[] foci, A[] carriers, int cfg)
  {
    super(foci, carriers, cfg);
    if (foci.length != 2)
      throw new IllegalArgumentException("Expected two focus atoms!");
    this.bond = bond;
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc)
  {
    // we store the label on the single bond
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

    Object[] ordered = new Object[4];
    ordered[0] = edges1.get(0).getEnd().getAtom();
    ordered[1] = edges1.get(1).getEnd().getAtom();
    ordered[2] = edges2.get(0).getEnd().getAtom();
    ordered[3] = edges2.get(1).getEnd().getAtom();

    int parity = parity4(ordered, getCarriers());

    if (parity == 0)
      throw new RuntimeException("Could not calculate parity! Carrier mismatch");

    int config = this.getConfig();
    if (parity == 1)
      config ^= 0x3;

    Stats.INSTANCE.countRule(Math.max(priority1.getRuleIdx(),
                                      priority2.getRuleIdx()));

    if (config == RIGHT) {
      if (priority1.isPseduoAsymettric() !=
          priority2.isPseduoAsymettric())
        return Descriptor.m;
      else
        return Descriptor.M;
    } else if (config == LEFT) {
      if (priority1.isPseduoAsymettric() !=
          priority2.isPseduoAsymettric())
        return Descriptor.p;
      else
        return Descriptor.P;
    }

    return Descriptor.Unknown;
  }
}
