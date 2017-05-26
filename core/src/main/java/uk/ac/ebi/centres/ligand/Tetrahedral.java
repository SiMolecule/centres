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

import org.openscience.cdk.interfaces.IAtom;
import uk.ac.ebi.centres.Centre;
import uk.ac.ebi.centres.Descriptor;
import uk.ac.ebi.centres.Ligand;
import uk.ac.ebi.centres.MutableDescriptor;
import uk.ac.ebi.centres.Priority;
import uk.ac.ebi.centres.PriorityRule;
import uk.ac.ebi.centres.SignCalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author John May
 */
public class Tetrahedral<A> extends AbstractLigand<A> implements Centre<A> {

    private final A atom;
    private       A parent;

    public Tetrahedral(MutableDescriptor descriptor,
                       A atom) {
        super(descriptor, 0);
        this.atom = atom;
        this.parent = atom;
    }


    @Override
    public A getAtom() {
        return atom;
    }


    @Override
    public void setParent(A atom) {
        // don't have a parent here
        this.parent = atom;
    }


    @Override
    public A getParent() {
        return this.parent;
    }


    @Override
    public Set<A> getAtoms() {
        return Collections.singleton(atom);
    }


    @Override
    public int perceiveAuxiliary(Collection<Centre<A>> centres,
                                 PriorityRule<A> rule,
                                 SignCalculator<A> calculator) {

        Map<Ligand<A>, uk.ac.ebi.centres.Descriptor> auxiliary = new HashMap<Ligand<A>, uk.ac.ebi.centres.Descriptor>(centres.size());
        Set<Ligand<A>>                               done      = new HashSet<Ligand<A>>();

        // ensure the entire digraph is built
        getProvider().build();

        for (Ligand<A> ligand : getProvider().ligands()) {
            ligand.setAuxiliary(Descriptor.Unknown);
        }
        
        int size = 0;

        do {
            auxiliary.clear();
            for (Centre<A> centre : centres) {

                // don't do aux perception on self
                if (centre == this)
                    continue;                                  
                
                // can only reroot on single atom centres
                if (centre.getAtoms().size() == 1) {

                    for (Ligand<A> ligand : getProvider().ligandInstancesForAtom(centre.getAtom())) {

                        if (done.contains(ligand)) continue;
                        
                        getProvider().reroot(ligand);

                        uk.ac.ebi.centres.Descriptor descriptor = centre.perceive(getProvider().getLigands(ligand),
                                                                                  rule,
                                                                                  calculator);
                        
                        if (descriptor != Descriptor.Unknown) {
                            auxiliary.put(ligand, descriptor);
                            done.add(ligand);
                        }

                    }
                }
            }

            // transfer auxiliary descriptors to their respective ligands
            for (Map.Entry<Ligand<A>, uk.ac.ebi.centres.Descriptor> entry : auxiliary.entrySet())
                entry.getKey().setAuxiliary(entry.getValue());
            size += auxiliary.size();
            
        } while (!auxiliary.isEmpty());

        // reroot on this
        getProvider().reroot(this);

        return size;

    }

    @Override
    public uk.ac.ebi.centres.Descriptor perceive(List<Ligand<A>> proximal, PriorityRule<A> rule, SignCalculator<A> calculator) {

        if (proximal.size() < 3) {
            return Descriptor.None;
        }

        IAtom iatom = (IAtom) getAtom();

       
        Priority priority = rule.prioritise(proximal);


        if (priority.isUnique()) {

            // remove any H that were added with null coordinates
            proximal = filterAddedHydrogens(proximal);

            if (proximal.size() < 3) {
                return Descriptor.Unknown;
            }

            int sign = calculator.getSign(this,
                                          proximal.get(0),
                                          proximal.get(1),
                                          proximal.get(2),
                                          proximal.size() == 4 ? proximal.get(3) : this);

            if (sign == 0)
                return Descriptor.Unknown;
            
            if (priority.isPseduoAsymettric())
                return sign > 0 ? Descriptor.s : Descriptor.r;
            else
                return sign > 0 ? Descriptor.S : Descriptor.R;
        }

        return Descriptor.Unknown;
    }

    private List<Ligand<A>> filterAddedHydrogens(List<Ligand<A>> proximal) {
        List<Ligand<A>> filtered = new ArrayList<Ligand<A>>();
        // remove hydrogen and 'duplicated' double-bond atoms (i.e. sulfoxide)
        for (Ligand<A> ligand : new ArrayList<Ligand<A>>(proximal)) {
            if (((IAtom) ligand.getAtom()).getPoint2d() != null) {
                filtered.add(ligand);
            }
        }
        return filtered;
    }

    @Override
    public uk.ac.ebi.centres.Descriptor perceive(PriorityRule<A> rule, SignCalculator<A> calculator) {
        return perceive(getLigands(), rule, calculator);
    }


    @Override
    public Boolean isParent(A atom) {
        return parent.equals(atom);
    }


    @Override
    public void dispose() {
        getProvider().dispose();
        setProvider(null);
    }
}