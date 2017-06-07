/*
 * Copyright (c) 2012. John May
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
 * An abstract comparator that provides construction of the {@link Comparison}
 * wrapper allowing subclasses to focus on the actual comparison of ligands.
 *
 * @author John May
 */
public abstract class SequenceRule<A, B> implements Comparator<Edge<A, B>> {

  public Sort<A, B> sorter = null;
  private BaseMol<A, B> mol;

  public SequenceRule(BaseMol<A, B> mol)
  {
    this.mol = mol;
  }

  public Descriptor getBondLabel(Edge<A, B> edge)
  {
    B bond = edge.getBond();
    if (bond == null)
      return null;
    Descriptor label = edge.getAux();
    if (label != null)
      return label;
    label = mol.getBondProp(bond, BaseMol.CIP_LABEL_KEY);
    if (label != null)
      edge.setAux(label);
    return label;
  }

  public Descriptor getAtomLabel(Node<A, B> node)
  {
    A atom = node.getAtom();
    if (atom == null)
      return null;
    Descriptor label = node.getAux();
    if (label != null)
      return label;
    label = mol.getAtomProp(atom, BaseMol.CIP_LABEL_KEY);
    if (label != null)
      node.setAux(label);
    return label;
  }

  public boolean isPseudoAsymmetric()
  {
    return false;
  }

  public int recursiveCompare(Edge<A, B> a, Edge<A, B> b)
  {

    // pseudo atoms (atomic no. 0) match all
    if (mol.getAtomicNum(a.getEnd().getAtom()) == 0 ||
        mol.getAtomicNum(b.getEnd().getAtom()) == 0)
      return 0;

    int cmp = compare(a, b);
    if (cmp != 0) return cmp;

    Queue<Edge<A, B>> aQueue = new LinkedList<Edge<A, B>>();
    Queue<Edge<A, B>> bQueue = new LinkedList<Edge<A, B>>();

    aQueue.add(a);
    bQueue.add(b);

    while (!aQueue.isEmpty() && !bQueue.isEmpty()) {
      a = aQueue.poll();
      b = bQueue.poll();
      List<Edge<A, B>> as = a.getEnd().getEdges();
      List<Edge<A, B>> bs = b.getEnd().getEdges();

//      if (!a.isOrderedBy(getClass()))
      prioritise(a.getEnd(), as);
//      if (!b.isOrderedBy(getClass()))
      prioritise(b.getEnd(), bs);
//      a.markOrderedBy(getClass());
//      b.markOrderedBy(getClass());

      Iterator<Edge<A, B>> aIt = as.iterator();
      Iterator<Edge<A, B>> bIt = bs.iterator();
      while (aIt.hasNext() && bIt.hasNext()) {
        Node<A, B> aNode = a.getEnd();
        Node<A, B> bNode = a.getEnd();
        Edge<A, B> aEdge = aIt.next();
        Edge<A, B> bEdge = bIt.next();

        // step over 'up' edges
        if (aEdge.isEnd(aNode)) {
          // if b is 'down' something's not right!
          if (bEdge.isBeg(bNode))
            throw new IllegalArgumentException("Something unexpected!");
          continue;
        }

        // pseudo atoms (atomic no. 0) match all
        if (mol.getAtomicNum(aEdge.getEnd().getAtom()) == 0 ||
            mol.getAtomicNum(bEdge.getEnd().getAtom()) == 0)
          return 0;

        cmp = compare(aEdge, bEdge);
        if (cmp != 0) return cmp;

        aQueue.add(aEdge);
        bQueue.add(bEdge);
      }

      int sizediff = Integer.compare(as.size(), bs.size());

      if (sizediff != 0)
        return sizediff;
    }
    return 0;
  }

  public int getComparision(Edge<A, B> a, Edge<A, B> b)
  {
    final int cmp = recursiveCompare(a, b);
    assert cmp < 2 && cmp > -2;
    if (isPseudoAsymmetric())
      return 2 * cmp; // -2,0,+2
    else
      return cmp; // -1,0,+1
  }

  public void setSorter(Sort<A, B> sorter)
  {
    this.sorter = sorter;
  }


  public final Sort<A, B> getSorter()
  {
    if (sorter == null)
      sorter = new Sort<A, B>(this);
    return sorter;
  }

  public Priority prioritise(Node<A, B> node, List<Edge<A, B>> edges)
  {
    return getSorter().prioritise(node, edges);
  }
}
