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
import com.simolecule.centres.Descriptor;
import com.simolecule.centres.Edge;
import com.simolecule.centres.Node;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * An abstract comparator that provides construction of the {@link Sort}
 * wrapper allowing subclasses to focus on the actual comparison of ligands.
 * <br>
 * Note there is some special semantics on the return value of the comparison.
 * Possible values are -3 (COMP_TO_WILDCARD), -2 (LESS using rule &le; 5),
 * -1 (LESS), 0 (EQ), +1 (MORE), +2 (MORE using rule &ge; 5). We need thse
 * to indicate back up to the caller that this is either undefined
 * stereochemistry or it is defined by pseudo-asymmetric (super-impossible
 * mirror image R=&gt;r, S=&gt;s, etc).
 *
 * @author John May
 */
public abstract class SequenceRule<A, B> implements Comparator<Edge<A, B>> {

  /* Sentinel value returned if a comparison was made to a wildcard (any atom).
   * Since these have undefined ranking we need to indicate back up the call
   * stack that this centre is undefined. */
  public static final int COMP_TO_WILDCARD = -3;

  public Sort<A, B> sorter = null;
  private final BaseMol<A, B> mol;

  public SequenceRule(BaseMol<A, B> mol)
  {
    this.mol = mol;
  }

  public BaseMol<A,B> getMol() {
    return mol;
  }

  public Descriptor getBondLabel(Edge<A, B> edge)
  {
    B bond = edge.getBond();
    if (bond == null)
      return null;
    return edge.getAux();
  }

  public int getNumSubRules() {
    return 1;
  }

  public boolean isPseudoAsymmetric()
  {
    return false;
  }

  public int recursiveCompare(Edge<A, B> a, Edge<A, B> b)
  {
    int cmp = compare(a, b);
    if (cmp != 0) return cmp;

    Queue<Edge<A, B>> aQueue = new LinkedList<>();
    Queue<Edge<A, B>> bQueue = new LinkedList<>();

    aQueue.add(a);
    bQueue.add(b);

    while (!aQueue.isEmpty() && !bQueue.isEmpty()) {
      a = aQueue.poll();
      b = bQueue.poll();
      assert a != null && b != null;
      List<Edge<A, B>> as = a.getEnd().getEdges();
      List<Edge<A, B>> bs = b.getEnd().getEdges();

      // shallow sort first of all
      if (sort(a.getEnd(), as, false).wasWildcardFound())
        return COMP_TO_WILDCARD;
      if (sort(b.getEnd(), bs, false).wasWildcardFound())
        return COMP_TO_WILDCARD;

      int sizediff = Integer.compare(as.size(), bs.size());

      {
        Iterator<Edge<A, B>> aIt = as.iterator();
        Iterator<Edge<A, B>> bIt = bs.iterator();
        while (aIt.hasNext() && bIt.hasNext()) {
          Node<A, B> aNode = a.getEnd();
          Node<A, B> bNode = b.getEnd();
          Edge<A, B> aEdge = aIt.next();
          Edge<A, B> bEdge = bIt.next();

          if (areUpEdges(aNode, bNode, aEdge, bEdge))
            continue;
          cmp = compare(aEdge, bEdge);
          if (cmp != 0) return cmp;
        }
      }

      if (sizediff != 0)
        return sizediff;

      if (sort(a.getEnd(), as).wasWildcardFound())
        return COMP_TO_WILDCARD;
      if (sort(b.getEnd(), bs).wasWildcardFound())
        return COMP_TO_WILDCARD;

      {
        Iterator<Edge<A, B>> aIt = as.iterator();
        Iterator<Edge<A, B>> bIt = bs.iterator();
        while (aIt.hasNext() && bIt.hasNext()) {
          Node<A, B> aNode = a.getEnd();
          Node<A, B> bNode = b.getEnd();
          Edge<A, B> aEdge = aIt.next();
          Edge<A, B> bEdge = bIt.next();

          if (areUpEdges(aNode, bNode, aEdge, bEdge))
            continue;
          cmp = compare(aEdge, bEdge);
          if (cmp != 0) return cmp;

          aQueue.add(aEdge);
          bQueue.add(bEdge);
        }
      }


    }
    return 0;
  }

  private boolean areUpEdges(Node<A, B> aNode, Node<A, B> bNode, Edge<A, B> aEdge, Edge<A, B> bEdge)
  {
    // step over 'up' edges
    if (aEdge.isEnd(aNode)) {
      // if b is 'down' something's not right!
      if (!bEdge.isEnd(bNode))
        throw new IllegalArgumentException("Something unexpected!");
      return true;
    }
    return false;
  }

  public final int getComparision(Edge<A, B> a, Edge<A, B> b)
  {
    return getComparision(a, b, true);
  }

  public int getComparision(Edge<A, B> a, Edge<A, B> b, boolean deep)
  {
    return deep ? recursiveCompare(a, b) : compare(a, b);
  }

  public void setSorter(Sort<A, B> sorter)
  {
    this.sorter = sorter;
  }

  public Sort<A, B> getSorter()
  {
    if (sorter == null)
      sorter = new Sort<>(this);
    return sorter;
  }

  public Priority sort(Node<A, B> node, List<Edge<A, B>> edges, boolean deep)
  {
    return getSorter().prioritise(node, edges, deep);
  }

  public Priority sort(Node<A, B> node, List<Edge<A, B>> edges)
  {
    return sort(node, edges, true);
  }
}
