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

import com.google.common.collect.Sets;
import uk.ac.ebi.centres.ConnectionProvider;
import uk.ac.ebi.centres.Descriptor;
import uk.ac.ebi.centres.Ligand;
import uk.ac.ebi.centres.MutableDescriptor;
import uk.ac.ebi.centres.descriptor.General;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author John May
 */
public abstract class AbstractLigand<A> implements Ligand<A> {

    private      Descriptor auxiliary = General.UNKNOWN;
    private final ConnectionProvider<A> provider;
    private final Set<A>                visited;
    private final MutableDescriptor     descriptor;


    public AbstractLigand(ConnectionProvider<A> provider,
                          Set<A> visited,
                          MutableDescriptor descriptor) {

        this.provider = provider;
        this.descriptor = descriptor;

        // optimise size for a load factor of 0.75
        this.visited = Sets.newHashSet(visited);

    }


    public AbstractLigand(ConnectionProvider<A> provider,
                          MutableDescriptor descriptor) {

        this.provider = provider;
        this.descriptor = descriptor;


        this.visited = Collections.EMPTY_SET;

    }


    @Override
    public Boolean isVisited(A atom) {
        return visited.contains(atom);
    }


    @Override
    public Set<A> getVisited() {
        return visited;
    }


    @Override
    public void setDescriptor(Descriptor descriptor) {
        this.descriptor.set(descriptor);
    }


    @Override
    public Descriptor getDescriptor() {
        return descriptor.get();
    }


    /**
     * @inheritDoc
     */
    @Override
    public List<Ligand<A>> getLigands() {
        return provider.getLigands(this);
    }


    /**
     * @inheritDoc
     */
    @Override
    public Descriptor getAuxiliary() {
        return auxiliary;
    }


    /**
     * @inheritDoc
     */
    @Override
    public void setAuxiliary(Descriptor descriptor) {
        this.auxiliary = descriptor;
    }


}
