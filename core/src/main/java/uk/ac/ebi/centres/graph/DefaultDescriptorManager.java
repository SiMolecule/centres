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

package uk.ac.ebi.centres.graph;

import com.google.common.collect.Sets;
import uk.ac.ebi.centres.DescriptorManager;
import uk.ac.ebi.centres.MutableDescriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author John May
 */
public class DefaultDescriptorManager<A> implements DescriptorManager<A> {

    private Map<A, MutableDescriptor>      atomMap = new HashMap<A, MutableDescriptor>();
    private Map<Set<A>, MutableDescriptor> bondMap = new HashMap<Set<A>, MutableDescriptor>();


    @Override
    public MutableDescriptor getDescriptor(A atom) {
        MutableDescriptor mutableDescriptor = atomMap.get(atom);
        if (mutableDescriptor == null) {
            mutableDescriptor = new MutableDescriptor();
            atomMap.put(atom, mutableDescriptor);
        }
        return mutableDescriptor;
    }


    @Override
    public MutableDescriptor getDescriptor(A first, A second) {
        Set<A> atoms = Sets.newHashSet(first, second);
        MutableDescriptor mutableDescriptor = bondMap.get(atoms);
        if (mutableDescriptor == null) {
            mutableDescriptor = new MutableDescriptor();
            bondMap.put(atoms, mutableDescriptor);
        }
        return mutableDescriptor;
    }
}
