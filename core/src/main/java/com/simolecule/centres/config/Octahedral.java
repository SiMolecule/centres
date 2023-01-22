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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An octahedral configuration is described by a focus and six carriers. The
 * configuration index can be a number 1..30 and describes the relative
 * arrangement of the neighbours. The configs allow any possible orderings
 * but to input an octahedral we can just use 1 (OH1). It looks like this,
 * the axis is made of the first and last carrier 'c[0]-f-c[5]'. The remaining
 * carriers c[1..4] and then ordered anti-clockwise around the focus:
 *
 * <pre>
 *
 *            c[2]
 *            | c[5]
 *            |/
 *     c[3]---f---c[1] = OH1
 *           /|       where c[0]: carriers
 *       c[0] |             f: focus
 *            c[4]          'c[0]' is in front of the focus 'f', 'c[5]' is behind
 *
 * </pre>
 *
 * @param <A> the atom class
 * @param <B> the bond class
 * @see <a href="http://opensmiles.org/opensmiles.html#_octahedral_centers">OpenSMILES Octahedral Centers</a>
 */
public final class Octahedral<A, B> extends Configuration<A, B> {

  private final Map<A, String> cache = new HashMap<>();

  // the index of the carrier in the trans position
  private final int[] TRANS_INDEX = new int[]{5, 3, 4, 1, 2, 0};

  /* We are in normal form:
   *        c
   *        | a
   *        |/
   *    d---x---b = OH1
   *       /|       where a: first carrier, b: second carried, etc
   *      f |             x: focus
   *        e             'a' is in front of the focus 'x', 'f' is behind
   *
   * storage index: a@0, b@1, c@2, d@3, e@4, f@5
   *
   * so if our axis is:
   *   a-f (0->5) the plane atoms are b,c,d,e (1,2,3,4) anticlockwise
   *   b-d (1->3) the plane atoms are a,c,f,e (0,2,5,4) anticlockwise
   *   c-e (2->4) the plane atoms are a,d,f,b (0,3,5,1) anticlockwise
   */
  private final int[][] PLANE_INDEX = new int[][]{
      {1, 2, 3, 4}, // a-f
      {0, 2, 5, 4}, // b-d
      {0, 3, 5, 1}, // c-e
      {4, 5, 2, 0}, // d-b
      {1, 5, 3, 0}, // e-c
      {4, 3, 2, 1}, // f-a
  };

  public Octahedral(A focus, A[] carriers) {
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

  private A[] getPlane(A atom) {
    A[] carriers = getCarriers();
    for (int i = 0; i < carriers.length; i++) {
      if (carriers[i].equals(atom)) {
        return (A[]) new Object[]{
            carriers[PLANE_INDEX[i][0]],
            carriers[PLANE_INDEX[i][1]],
            carriers[PLANE_INDEX[i][2]],
            carriers[PLANE_INDEX[i][3]]
        };
      }
    }
    throw new IllegalArgumentException();
  }

  private int getPriorityNumber(List<List<Edge<A, B>>> parts, A atom) {
    int num = 1;
    for (List<Edge<A, B>> part : parts) {
      for (Edge<A, B> e : part) {
        if (e.getEnd().getAtom().equals(atom))
          return num;
      }
      num++;
    }
    throw new IllegalArgumentException();
  }

  private boolean hasConfiguration(List<List<Edge<A, B>>> parts) {
    return parts.size() > 2 ||
           parts.size() == 2 &&
           parts.get(0).size() != 1 && parts.get(0).size() != 5;
  }

  private Descriptor label(Node<A, B> root, SequenceRule<A, B> comp) {
    List<Edge<A, B>>       edges    = root.getEdges();
    Priority               priority = comp.sort(root, edges);
    if (priority.wasWildcardFound())
      return Descriptor.Unknown;
    List<List<Edge<A, B>>> parts    = comp.getSorter().getGroups(edges);

    if (!hasConfiguration(parts))
      return Descriptor.ns; // maybe return unknown?

    A   fstAxisBeg = null;
    int fstAxisBegRank = 0;
    int fstAxisEndRank = 0;
    int sndAxisBegRank;
    int sndAxisEndRank;
    int thdAxisBegRank;
    int thdAxisEndRank;

    for (Edge<A, B> edge : parts.get(0)) {
      A   beg = edge.getEnd().getAtom();
      A   end = findTransAtom(beg);
      int num = getPriorityNumber(parts, end);
      if (num > fstAxisEndRank) {
        fstAxisBegRank = getPriorityNumber(parts, beg);
        fstAxisEndRank = num;
        fstAxisBeg = beg;
      }
    }

    List<Integer> ccw_plane = new ArrayList<>(4);
    for (A a : getPlane(fstAxisBeg))
      ccw_plane.add(getPriorityNumber(parts, a));

    int low = 0;
    for (int i = 1; i < 4; i++) {
      if (ccw_plane.get(i) < ccw_plane.get(low)) {
        low = i;
      } else if (ccw_plane.get(i).equals(ccw_plane.get(low)) &&
                 ccw_plane.get((i + 2) % 4) > ccw_plane.get((low + 2) % 4)) {
        low = i;
      }
    }

    sndAxisBegRank = ccw_plane.get(low);
    sndAxisEndRank = ccw_plane.get((low + 2) % 4);
    thdAxisBegRank = ccw_plane.get((low + 1) % 4);
    thdAxisEndRank = ccw_plane.get((low + 3) % 4);

    int cmp = 0;

    // don't need rotation if two axes are the same or if there is a symmetric axis
    if (fstAxisEndRank != sndAxisEndRank &&
        (fstAxisBegRank != fstAxisEndRank ||
         sndAxisBegRank != sndAxisEndRank ||
         thdAxisBegRank != thdAxisEndRank)) {
      cmp = Integer.compare(ccw_plane.get((low + 1) % 4), ccw_plane.get((low + 4 - 1) % 4));
      if (cmp == 0)
        cmp = Integer.compare(ccw_plane.get((low + 1) % 4), ccw_plane.get((low + 4 - 1) % 4));
      if (cmp == 0)
        cmp = Integer.compare(ccw_plane.get((low + 2) % 4), ccw_plane.get((low + 4 - 2) % 4));
    }

    final String rotate;
    if (cmp < 0)
      rotate = "-A";
    else if (cmp > 0)
      rotate = "-C";
    else
      rotate = "";

    if (fstAxisEndRank < 7 && sndAxisEndRank < 7) {
      cache.put(getFocus(), "OC-6-" + fstAxisEndRank + "" + sndAxisEndRank + rotate);
      return Descriptor.OC_6;
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
