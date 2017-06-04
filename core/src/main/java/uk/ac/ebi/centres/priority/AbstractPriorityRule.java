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

package uk.ac.ebi.centres.priority;

import uk.ac.ebi.centres.Comparison;
import uk.ac.ebi.centres.Node;
import uk.ac.ebi.centres.LigandSorter;
import uk.ac.ebi.centres.Priority;
import uk.ac.ebi.centres.PriorityRule;

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
public abstract class AbstractPriorityRule<A>
        implements PriorityRule<A> {

  private LigandSorter<A> sorter;
  private boolean halted = Boolean.FALSE;

  /**
   * The type is store here and appended with the {@link
   * #compareLigands(Node, Node)}
   */
  private final boolean isPseudoAssymetric;
  private final Type    ordering;


  /**
   * Default constructor creates an non-pseudo assymetric rule
   * comparator.
   */
  public AbstractPriorityRule(Type ordering)
  {
    this(false, ordering);
  }


  /**
   * Constructor creates a comparator with the specified type.
   */
  public AbstractPriorityRule(boolean isPseudoAssymetric, Type ordering)
  {
    this.ordering = ordering;
    this.isPseudoAssymetric = isPseudoAssymetric;

  }


  @Override
  public void setHalt(boolean halt)
  {
    this.halted = halt;
  }

  public int recursiveCompare(Node<A> a, Node<A> b)
  {

    int cmp = compare(a, b);
    if (cmp != 0) return cmp;

    Queue<Node<A>> aQueue = new LinkedList<Node<A>>();
    Queue<Node<A>> bQueue = new LinkedList<Node<A>>();

    aQueue.add(a);
    bQueue.add(b);

    while (!aQueue.isEmpty() && !bQueue.isEmpty()) {
      a = aQueue.poll();
      b = bQueue.poll();
      List<Node<A>> as = a.getNodes();
      List<Node<A>> bs = b.getNodes();

      if (!a.isOrderedBy(getClass()))
        prioritise(as);
      if (!b.isOrderedBy(getClass()))
        prioritise(bs);
      a.markOrderedBy(getClass());
      b.markOrderedBy(getClass());

      Iterator<Node<A>> aIt = as.iterator();
      Iterator<Node<A>> bIt = bs.iterator();
      while (aIt.hasNext() && bIt.hasNext()) {
        Node<A> aChild = aIt.next();
        Node<A> bChild = bIt.next();
        cmp = compare(aChild, bChild);
        if (cmp != 0) return cmp;
        aQueue.add(aChild);
        bQueue.add(bChild);
      }

      int sizediff = as.size() - bs.size();

      if (sizediff != 0)
        return sizediff;
    }
    return 0;
  }


  /**
   * @inheritDoc
   */
  @Override
  public Comparison compareLigands(Node<A> o1, Node<A> o2)
  {
    return new Comparison(recursiveCompare(o1, o2), isPseudoAssymetric);
  }


  /**
   * @inheritDoc
   */
  public void setSorter(LigandSorter<A> sorter)
  {
    this.sorter = sorter;
  }


  /**
   * Access the ligand sorter, if the sorter is null a default  insertion
   * sorter ({@link InsertionSorter}) is created using 'this; rule as the
   * comparator.
   *
   * @return a set ligand sorter or a newly created insertion sorter
   */
  public final LigandSorter<A> getSorter()
  {
    if (sorter == null)
      sorter = new InsertionSorter<A>(this);
    return sorter;
  }

  /**
   * Uses the injected ligand sorter to order the ligands.
   *
   * @param nodes the ligands that are to be sorted
   * @return whether the ligands are unique
   */
  public Priority prioritise(List<Node<A>> nodes)
  {
    return getSorter().prioritise(nodes);
  }

  public boolean isHalted()
  {
    return halted;
  }

  @Override
  public boolean isPseudoAsymmetric()
  {
    return isPseudoAssymetric;
  }

  /**
   * Indicates whether the rule is conditional etc.
   *
   * @return
   */
  public Type getRuleType()
  {
    return ordering;
  }
}
