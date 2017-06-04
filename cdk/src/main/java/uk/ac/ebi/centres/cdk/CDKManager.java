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

package uk.ac.ebi.centres.cdk;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;
import uk.ac.ebi.centres.Descriptor;
import uk.ac.ebi.centres.DescriptorManager;
import uk.ac.ebi.centres.MutableDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author John May
 */
public class CDKManager implements DescriptorManager<IAtom> {

    public static final String PROP_KEY = "descriptor";
    private final IAtomContainer container;
    private Map<IChemObject, MutableDescriptor> map = new HashMap<IChemObject, MutableDescriptor>();


    public CDKManager(IAtomContainer container) {
        this.container = container;
    }


    @Override
    public MutableDescriptor getDescriptor(IAtom atom) {
        return _getDescriptor(atom);
    }


    @Override
    public MutableDescriptor getDescriptor(IAtom first, IAtom second) {
        IBond bond = container.getBond(first, second);
        if (bond != null)
            return _getDescriptor(bond);
        return null;
    }


    private MutableDescriptor _getDescriptor(IChemObject chemObject) {
        MutableDescriptor mutator = map.get(chemObject);
        if (mutator == null) {
            mutator = new ProxyMutator(chemObject);
            map.put(chemObject, mutator);
        }
        return mutator;
    }


    class ProxyMutator extends MutableDescriptor {
        private IChemObject chemObject;


        public ProxyMutator(IChemObject chemObject) {
            this.chemObject = chemObject;
            chemObject.setProperty("descriptor", Descriptor.Unknown);
        }


        @Override
        public synchronized Descriptor get() {
            return (Descriptor) chemObject.getProperty(PROP_KEY);
        }


        @Override
        public synchronized void set(Descriptor descriptor) {
            chemObject.setProperty(PROP_KEY, descriptor);
        }
    }


    @Override
    public void clear() {
        map.clear();
    }
}
