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

package uk.ac.ebi.centres.ligand;

import uk.ac.ebi.centres.Centre;
import uk.ac.ebi.centres.ConnectionProvider;
import uk.ac.ebi.centres.Descriptor;
import uk.ac.ebi.centres.Ligand;
import uk.ac.ebi.centres.MutableDescriptor;
import uk.ac.ebi.centres.PriorityRule;
import uk.ac.ebi.centres.SignCalculator;
import uk.ac.ebi.centres.descriptor.General;
import uk.ac.ebi.centres.descriptor.Tetrahedral;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author John May
 */
public class TetrahedralCentre<A>
        extends AbstractLigand<A>
        implements Centre<A> {

    private final A atom;


    public TetrahedralCentre(MutableDescriptor descriptor,
                             A atom) {
        super(descriptor);
        this.atom = atom;
    }


    @Override
    public A getAtom() {
        return atom;
    }


    @Override
    public Set<A> getAtoms() {
        return Collections.singleton(atom);
    }


    @Override
    public Descriptor perceive(PriorityRule<A> rule, SignCalculator<A> calculator) {

        List<Ligand<A>> ligands = getLigands();

        boolean unique = rule.prioritise(ligands);


        if (unique) {

            if (ligands.size() < 4) ligands.add(this);

            int sign = calculator.getSign(ligands.get(0),
                                          ligands.get(1),
                                          ligands.get(2),
                                          ligands.get(3));

            return sign > 0 ? Tetrahedral.S : sign < 0
                            ? Tetrahedral.R : General.UNSPECIFIED;


        }

        return General.UNKNOWN;
    }


    @Override
    public Boolean isParent(A atom) {
        return Boolean.FALSE;
    }
}
