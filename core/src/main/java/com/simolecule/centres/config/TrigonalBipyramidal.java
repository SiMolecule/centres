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
 * An octahedral configuration is described by a focus and six carriers. The
 * configuration index can be a number 1..20 and describes the relative
 * arrangement of the neighbours. The configs allow any possible orderings
 * but to input an trigonal bipyramidal we can just use 1 (TB1). It looks like
 * this, the axis is made of the first and last carrier 'c[0]-f-c[4]'. The
 * remaining carriers c[1..3] and then ordered anti-clockwise around the focus:
 *
 * <pre>
 *
 *            c[0]
 *       c[2] |
 *           \|
 *            f -- c[1] = TB1
 *           /|       where c[0]: carriers
 *       c[3] |             f: focus
 *            c[4]          'c[0]' is in front of the focus 'f', 'c[3]' is behind
 *
 * </pre>
 *
 * @param <A> the atom class
 * @param <B> the bond class
 * @see <a href="http://opensmiles.org/opensmiles.html#_trigonal_bipyramidal_centers">OpenSMILES Trigonal Bipyramidal Centers</a>
 */
public final class TrigonalBipyramidal<A,B> extends Configuration<A,B> {

  private final Map<A,String> cache = new HashMap<>();

  public TrigonalBipyramidal(A focus, A[] carriers)
  {
    super(focus, carriers, 1);
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc) {
    mol.setAtomProp(getFocus(), BaseMol.CIP_LABEL_KEY, desc);
    mol.setAtomProp(getFocus(), BaseMol.CONF_INDEX, cache.remove(getFocus()));
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
    // check this!
    return parts.size() >= 2;
  }

  private Descriptor label(Node<A, B> root, SequenceRule<A, B> comp) {
    List<Edge<A,B>>       edges    = root.getEdges();
    Priority              priority = comp.sort(root, edges);
    List<List<Edge<A,B>>> parts    = comp.getSorter().getGroups(edges);

    if (!hasConfiguration(parts))
      return Descriptor.ns; // maybe return unknown?

    A[] carriers = getCarriers();
    for (Edge<A,B> edge : edges) {
      A beg = edge.getEnd().getAtom();
      A end = null;
      if (beg.equals(carriers[0]))
        end = carriers[4];
      else if (beg.equals(carriers[4]))
        end = carriers[0];
      if (end != null) {
        cache.put(getFocus(), "TBPY-5-" + getPriorityNumber(parts, beg)
                              + "" + getPriorityNumber(parts, end));
        return Descriptor.TBPY_5;
      }
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
