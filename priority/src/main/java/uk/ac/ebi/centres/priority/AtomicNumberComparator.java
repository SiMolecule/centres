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

/**
 * An abstract class for constitutional priority based on atomic number.
 * Sub-classes should implement {@link #getAtomicNumber(A)} which allows the
 * comparator to determine the rank of the ligands.
 *
 * @author John May
 */
public abstract class AtomicNumberComparator<A>
        extends AbstractLigandComparator<A> {

    /**
     * Compares the ligands by their atoms atomic numbers.
     *
     * @inheritDoc
     */
    @Override
    public int compare(Ligand<A> o1, Ligand<A> o2) {
        int value = getAtomicNumber(o1.getAtom()) - getAtomicNumber(o2.getAtom());
        return value != 0 ? value : compare(o1.getLigands(), o2.getLigands());
    }


    /**
     * Access the atomic number of the atom type
     *
     * @param atom
     *
     * @return
     */
    public abstract int getAtomicNumber(A atom);

}
