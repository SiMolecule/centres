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

import uk.ac.ebi.centres.Ligand;
import uk.ac.ebi.centres.LigandSorter;
import uk.ac.ebi.centres.PriorityRule;

import java.util.List;

/**
 * A simple insertion sort for ligands. The number of ligands is not likely to
 * be very larger as such doing a merge sort would have little benefit.
 *
 * @author John May
 */
public class InsertionSorter<A> implements LigandSorter<A> {

    private final PriorityRule<A> rule;


    public InsertionSorter(PriorityRule<A> comparator) {
        this.rule = comparator;
    }


    /**
     * Sorts in descending order. Currently always returns false.
     *
     * @inheritDoc
     */
    @Override
    public Boolean prioritise(List<Ligand<A>> ligands) {

        Boolean unique = Boolean.TRUE;

        for (int i = 0; i < ligands.size(); i++)
            for (int j = i; j > 0; j--) {
                int value = rule.compare(ligands.get(j - 1),
                                         ligands.get(j));
                if (value < 0) {
                    swap(ligands, j, j - 1);
                } else {
                    if (value == 0)
                        unique = Boolean.FALSE;
                    break;
                }

            }

        return unique;

    }


    public void swap(List list, int i, int j) {
        Object tmp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, tmp);
    }


}
