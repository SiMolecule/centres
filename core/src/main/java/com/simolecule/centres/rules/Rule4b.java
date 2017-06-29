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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package com.simolecule.centres.rules;

import com.google.common.collect.Collections2;
import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Edge;
import com.simolecule.centres.Node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * A descriptor pair rule. This rule defines that like descriptor pairs have
 * priority over unlike descriptor pairs.
 *
 * @author John May
 */
public class Rule4b<A, B>
        extends SequenceRule<A, B> {

  public Rule4b(BaseMol<A,B> mol)
  {
    super(mol);
  }

  /**
   * Generates a set of descriptor lists that maintain the like/unlike pairing
   * whilst descriptors are added. The set is navigable and maintains priority
   * ordering when multiple lists are present. This method is a wrapper for
   * adding the seeding ligand to the queue.
   *
   * @return navigable set of descriptor lists
   */
  protected NavigableSet<PairList> generate(Edge<A, B> e)
  {
    // would be good to give an expected size
    Queue<Edge<A, B>> queue = new ArrayDeque<>();
    queue.add(e);
    return new TreeSet<PairList>(generate(queue));
  }


  @Override
  public int recursiveCompare(Edge<A, B> a, Edge<A, B> b)
  {
    // can't/don't need to do recursive on the pair rule
    if (a.getEnd().getDistance() > 2 || b.getEnd().getDistance() > 2)
      return 0;
    return compare(a, b);
  }

  private boolean hasDescriptors(Node<A,B> node) {
    Deque<Node<A,B>> deque = new ArrayDeque<>();
    deque.add(node);
    while (!deque.isEmpty()) {
      Node<A,B> n = deque.poll();
      if (getAtomLabel(n) != null)
        return true;
      for (Edge<A,B> e : n.getEdges()) {
        if (e.getEnd().equals(n))
          continue;
        if (getBondLabel(e) != null)
          return true;
        deque.add(e.getEnd());
      }
    }
    return false;
  }

  /**
   * Generates a set of descriptor lists that maintain the like/unlike pairing
   * whilst descriptors are added. The set is navigable and maintains priority
   * ordering when multiple lists are present.
   *
   * @param queue a queue of ligands for which to get descriptors and expand
   * @return navigable set of descriptor lists
   */
  protected Set<PairList> generate(Queue<Edge<A, B>> queue)
  {

    final Set<PairList> lists = new HashSet<PairList>();

    // create a descriptor list with given exclusions
    PairList descriptors = new PairList();

    while (!queue.isEmpty()) {

      Edge<A, B> edge = queue.poll();

      descriptors.add(getBondLabel(edge));
      descriptors.add(getAtomLabel(edge.getEnd()));

      Node<A, B>       node     = edge.getEnd();
      List<Edge<A, B>> edges    = getLigandsToSort(node, node.getEdges());
      Priority         priority = sort(node, edges);
      if (priority.isUnique()) {
        // unique
        queue.addAll(edges);
      } else {
        // non unique need to subdivide and combine
        List<List<Edge<A, B>>> groups = getSorter().getGroups(edges);
        for (List<Edge<A,B>> combinated : permutate(groups)) {
          Deque<Edge<A,B>> subqueue = new ArrayDeque<>(queue);
          subqueue.addAll(combinated);
          // add to current descriptor list
          lists.addAll(descriptors.append(generate(subqueue)));
        }

        // queue was copied and delegated so we clear this instance
        queue.clear();
      }

    }

    if (lists.isEmpty())
      lists.add(descriptors);

    return lists;
  }

  /**
   * Reduce the number of combinations by not including terminal ligands in
   * the permuting. They can't be stereocentres and so won't contribute the
   * the like / unlike list.
   *
   * @param edges a list of edges
   * @return a list of non-terminal ligands
   */
  private List<Edge<A, B>> getLigandsToSort(Node<A,B> node, List<Edge<A, B>> edges)
  {
    List<Edge<A, B>> filtered = new ArrayList<Edge<A, B>>();
    for (Edge<A, B> edge : edges) {
      if (edge.isEnd(node) || edge.getEnd().isTerminal())
        continue;
      if (!hasDescriptors(node))
        continue;
      filtered.add(edge);
    }
    return filtered;
  }


  /**
   * Ugly piece of code to generate permutation of the given ligand groups.
   * There may be a much better way to do this. This method converts lists
   * with duplicates into all possible combinations. A, {B1, B2}, C would
   * permutate to A, B1, B2, C and A, B2, B1, C.
   * <p/>
   * This method was adapted from http://goo.gl/s6R7E
   *
   * @see <a href="http://goo.gl/s6R7E">http://www.daniweb.com/</a>
   */
  private static <T> List<List<T>> permutate(List<List<T>> uncombinedList)
  {

    List<List<T>> list = new ArrayList<List<T>>();

    // permeate the sublist
    for (List sublist : uncombinedList) {
      if (sublist.size() > 1) {
        Collection<List> tmp = Collections2.permutations(sublist);
        sublist.clear();
        sublist.addAll(tmp);
      }
    }

    int index[]      = new int[uncombinedList.size()];
    int combinations = combinations(uncombinedList) - 1;
    // Initialize index
    for (int i = 0; i < index.length; i++) {
      index[i] = 0;
    }
    // First combination is always valid
    List<T> combination = new ArrayList<T>();
    for (int m = 0; m < index.length; m++) {
      Object o = uncombinedList.get(m).get(index[m]);
      if (o instanceof Collection) {
        combination.addAll((Collection) o);
      } else {
        combination.add((T) o);
      }
    }
    list.add(combination);


    for (int k = 0; k < combinations; k++) {
      combination = new ArrayList<T>();
      boolean found = false;
      // We Use reverse order
      for (int l = index.length - 1; l >= 0 && found == false; l--) {
        int currentListSize = uncombinedList.get(l).size();
        if (index[l] < currentListSize - 1) {
          index[l] = index[l] + 1;
          found = true;
        } else {
          // Overflow
          index[l] = 0;
        }
      }
      for (int m = 0; m < index.length; m++) {
        Object o = uncombinedList.get(m).get(index[m]);
        if (o instanceof Collection) {
          combination.addAll((Collection) o);
        } else {
          combination.add((T) o);
        }
      }
      list.add(combination);
    }
    return list;
  }


  private static <T> int combinations(List<List<T>> list)
  {
    int count = 1;
    for (List<T> current : list) {
      count = count * current.size();
    }
    return count;
  }

  @Override
  public int compare(Edge<A, B> a, Edge<A, B> b)
  {
    if (a.getEnd().getDistance() > 2 || b.getEnd().getDistance() > 2)
      return 0;

    // produced pair lists are in order
    Set<PairList> list1 = generate(a);
    Set<PairList> list2 = generate(b);

    Iterator<PairList> list1It = list1.iterator();
    Iterator<PairList> list2It = list2.iterator();

    while (list1It.hasNext() && list2It.hasNext()) {
      int value = list1It.next().compareTo(list2It.next());
      if (value != 0) return value;
    }

    // there may be a different is list size but normally you'd have a
    // constitutional rule (which would find this) before this pairing rule

    // we don't go to the next level on this rule. We've already
    // exhaustively create pair lists (generate) for each ligand.
    return 0;
  }
}
