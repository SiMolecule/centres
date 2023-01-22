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
import com.simolecule.centres.rules.Priority;
import com.simolecule.centres.rules.SequenceRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An square-planar configuration is described by a focus and four carriers. The
 * configuration index can be a number 1..3 and describes the relative
 * arrangement/shape of the neighbours in the plane. The configs allow any
 * possible orderings but to input an square planar we can just use 1 (SP1).
 * This describes the carriers as appearing sequentially either clockwise
 * or anticlockwise (there is no mirror image).
 *
 * <pre>
 *
 *            c[0]
 *            |
 *            |
 *     c[3]---f---c[1] = SP1
 *            |       where c[0]: carriers
 *            |             f: focus
 *            c[2]
 *
 * </pre>
 *
 * @param <A> the atom class
 * @param <B> the bond class
 * @see <a href="http://opensmiles.org/opensmiles.html#_square_planar_centers">OpenSMILES Square Planar Centers</a>
 */
public final class SquarePlanar<A,B> extends Configuration<A,B> {

  private final Map<A,String> cache = new HashMap<>();

  // the index of the carrier in the trans position
  private final int[] TRANS_INDEX = new int[]{2,3,0,1};

  public SquarePlanar(A focus, A[] carriers)
  {
    super(focus, carriers, 1);
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc) {
    mol.setAtomProp(getFocus(), BaseMol.CIP_LABEL_KEY, desc);
    mol.setAtomProp(getFocus(), BaseMol.CONF_INDEX, cache.remove(getFocus()));
  }

  private A findTransAtom(A atom) {
    A[] carriers = getCarriers();
    for (int i = 0; i < carriers.length; i++) {
      if (carriers[i].equals(atom)) {
        return carriers[TRANS_INDEX[i]];
      }
    }
    throw new IllegalArgumentException();
  }

  private int getPriorityNumber(List<List<Edge<A,B>>> parts, A atom) {
    int num = 1;
    for (List<Edge<A,B>> part : parts) {
      for (Edge<A,B> e : part) {
        if (e.getEnd().getAtom().equals(atom))
          return num;
      }
      num++;
    }
    throw new IllegalArgumentException();
  }

  private boolean hasConfiguration(List<List<Edge<A,B>>> parts) {
    return parts.size() > 2 ||
           parts.size() == 2 &&
           parts.get(0).size() != 1 && parts.get(0).size() != 3;
  }

  private Descriptor label(Node<A, B> root, SequenceRule<A, B> comp) {
    List<Edge<A,B>>       edges    = root.getEdges();
    Priority              priority = comp.sort(root, edges);
    if (priority.wasWildcardFound())
      return Descriptor.Unknown;
    List<List<Edge<A,B>>> parts    = comp.getSorter().getGroups(edges);

    if (!hasConfiguration(parts))
      return Descriptor.ns; // maybe return unknown?

    int low = 5;
    for (Edge<A,B> edge : parts.get(0)) {
      A beg = edge.getEnd().getAtom();
      A end = findTransAtom(beg);
      int num = getPriorityNumber(parts, end);
      if (num < low) {
        low = num;
      }
    }

    if (low < 5) {
      cache.put(getFocus(), "SP-4-" + low);
      return Descriptor.SP_4;
    }

    return Descriptor.ns;
  }

  @Override
  public Descriptor label(SequenceRule<A, B> comp) {

    Digraph<A, B> digraph = getDigraph();
    Node<A, B>    root    = digraph.getRoot();
    if (root == null)
      root = digraph.init(getFocus());
    else
      digraph.changeRoot(digraph.getRoot());

    return label(root, comp);
  }
}
