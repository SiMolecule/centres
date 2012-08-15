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
import uk.ac.ebi.centres.MutableDescriptor;
import uk.ac.ebi.centres.PriorityRule;
import uk.ac.ebi.centres.SignCalculator;

import java.util.Collections;
import java.util.Set;

/**
 * @author John May
 */
public class TetrahedralCentre<A>
        extends AbstractLigand<A>
        implements Centre<A> {

    private final A atom;


    public TetrahedralCentre(ConnectionProvider<A> provider,
                             MutableDescriptor descriptor,
                             A atom) {
        super(provider, descriptor);
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
        return null;
    }


    @Override
    public Boolean isParent(A atom) {
        return Boolean.FALSE;
    }
}
