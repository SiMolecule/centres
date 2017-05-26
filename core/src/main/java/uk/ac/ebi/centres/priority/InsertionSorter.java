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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple insertion sort for ligands. The number of ligands is not likely to
 * be very larger as such doing a merge sort would have little benefit.
 *
 * @author John May
 */
public class InsertionSorter<A> implements LigandSorter<A> {

    private final List<PriorityRule<A>> rules = new ArrayList<PriorityRule<A>>(5);


    public InsertionSorter(PriorityRule<A> comparator) {
        this.rules.add(comparator);
    }


    public InsertionSorter(List<PriorityRule<A>> comparators) {
        rules.addAll(comparators);
    }


    /**
     * Sorts in descending order and indicates whether all elements are unique
     * and the type of descriptor used.
     *
     * @inheritDoc
     */
    @Override
    public Priority prioritise(List<Node<A>> nodes) {

        Boolean unique = Boolean.TRUE;
        boolean pseudoAsym = false;
//        Set<Set<Integer>> duplicates = null;

        for (int i = 0; i < nodes.size(); i++)
            for (int j = i; j > 0; j--) {

                Comparison comparison = compareLigands(nodes.get(j - 1),
                                                       nodes.get(j));

                if (comparison.isPseduoAsym())
                    pseudoAsym = true;

                if (comparison.getOrder() < 0) {
                    swap(nodes, j, j - 1);
                } else {
                    if (comparison.getOrder() == 0)
                        unique = Boolean.FALSE;
                    break;
                }

            }

        return new Priority(unique, pseudoAsym);

    }


    public Comparison compareLigands(Node<A> first, Node<A> second) {
        for (PriorityRule<A> rule : rules) {
            Comparison comparison = rule.compareLigands(first, second);
            if (comparison.getOrder() != 0) return comparison;
        }
        return new Comparison(0, false);
    }


    public void swap(List list, int i, int j) {
        Object tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }


    public List<List<Node<A>>> getGroups(List<Node<A>> sorted) {

        // would be nice to have this integrated whilst sorting - may provide a small speed increase
        // but as most of our lists are small we take use ugly sort then group approach
        LinkedList<List<Node<A>>> groups = new LinkedList<List<Node<A>>>();

        for (Node<A> node : sorted) {

            if (groups.isEmpty()
                    || compareLigands(groups.getLast().iterator().next(),
                                      node).getOrder() != 0)
                groups.add(new ArrayList<Node<A>>());

            groups.getLast().add(node);

        }

        return groups;

    }


}
