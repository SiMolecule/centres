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

import org.junit.Test;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.periodictable.PeriodicTable;
import uk.ac.ebi.centres.Centre;
import uk.ac.ebi.centres.CentreProvider;
import uk.ac.ebi.centres.Descriptor;
import uk.ac.ebi.centres.DescriptorManager;
import uk.ac.ebi.centres.Digraph;
import uk.ac.ebi.centres.PriorityRule;
import uk.ac.ebi.centres.SignCalculator;
import uk.ac.ebi.centres.descriptor.General;
import uk.ac.ebi.centres.graph.DefaultDescriptorManager;
import uk.ac.ebi.centres.io.CytoscapeWriter;
import uk.ac.ebi.centres.ligand.AbstractLigand;
import uk.ac.ebi.centres.priority.AtomicNumberRule;
import uk.ac.ebi.centres.priority.CombinedRule;
import uk.ac.ebi.centres.priority.access.AtomicNumberAccessor;
import uk.ac.ebi.centres.priority.access.descriptor.AuxiliaryDescriptor;
import uk.ac.ebi.centres.priority.access.descriptor.PrimaryDescriptor;
import uk.ac.ebi.centres.priority.descriptor.PairRule;
import uk.ac.ebi.centres.priority.descriptor.ZERule;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author John May
 */
public class CentreProviderTest {

    @Test
    public void testGetCentres() throws CDKException, IOException {

        IAtomContainer container = CMLLoader.loadCML(getClass().getResourceAsStream("(2R,3S,5R,7R,8R)-4,6-bis[(2R,3R)-3-hydroxybutan-2-yl]-3,7-dimethylnonane-2,5,8-triol.xml"));

        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
        for (IAtom atom : container.atoms())
            atom.setAtomicNumber(PeriodicTable.getAtomicNumber(atom.getSymbol()));
        DescriptorManager<IAtom> manager = new DefaultDescriptorManager<IAtom>();

        CentreProvider<IAtom> provider = new CDKCentreProvider(container);
        Collection<Centre<IAtom>> centres = provider.getCentres(manager);


        PriorityRule<IAtom> rule = new CombinedRule<IAtom>(
                new AtomicNumberRule<IAtom>(new AtomicNumberAccessor<IAtom>() {
                    @Override
                    public int getAtomicNumber(IAtom atom) {
                        return atom.getAtomicNumber();
                    }
                }),
                new ZERule<IAtom>(),
                new PairRule<IAtom>(new PrimaryDescriptor<IAtom>()));
        PriorityRule<IAtom> auxrule = new CombinedRule<IAtom>(
                new AtomicNumberRule<IAtom>(new AtomicNumberAccessor<IAtom>() {
                    @Override
                    public int getAtomicNumber(IAtom atom) {
                        return atom.getAtomicNumber();
                    }
                }),
                new ZERule<IAtom>(),
                new PairRule<IAtom>(new AuxiliaryDescriptor<IAtom>()));

        List<Centre<IAtom>> unperceived = new LinkedList<Centre<IAtom>>();

        List<Centre<IAtom>> perceived = new LinkedList<Centre<IAtom>>();
        unperceived.addAll(centres);
        SignCalculator<IAtom> calc = new CDK2DSignCalculator();

//        Centre<IAtom> centre = unperceived.iterator().next();
//        System.out.println(centre);
//        AbstractLigand<IAtom> ligand = ((AbstractLigand<IAtom>) centre);
//        ligand.getProvider().build();
//
//        System.out.println("graphing");
//        List<Ligand<IAtom>> ligandList = ligand.getProvider().getLigands(container.getAtom(2));
//        int index = 0;
//        for (Ligand<IAtom> aux : ligandList) {
//            ligand.getProvider().reroot(aux);
//            Digraph digraph = (Digraph) ((AbstractLigand) centre).getProvider();
//
//            CytoscapeWriter<IAtom> writer = new CytoscapeWriter<IAtom>(new File(centre.toString() + "-aux-digraph" + "-" + ++index),
//                                                                       digraph) {
//                @Override
//                public void mapAttributes(IAtom atom, Map<String, String> map) {
//                    map.put("symbol", atom.getSymbol());
//                    map.put("number", atom.getProperty("number").toString());
//                }
//            };
//            writer.writeSif();
//            writer.writeAttributes();
//            writer.close();
//        }


//        if (true) return;

        Boolean found = Boolean.FALSE;
        do {

            Map<Centre<IAtom>, Descriptor> map = new HashMap<Centre<IAtom>, Descriptor>();

            for (Centre<IAtom> centre : unperceived) {

                Descriptor descriptor = centre.perceive(rule, calc);

                if (descriptor != General.UNKNOWN)
                    map.put(centre, descriptor);


            }

            found = !map.isEmpty();
            // transfer descriptors
            for (Map.Entry<Centre<IAtom>, Descriptor> entry : map.entrySet()) {
                unperceived.remove(entry.getKey());
                perceived.add(entry.getKey());
                entry.getKey().setDescriptor(entry.getValue());
            }


        } while (found);

        long start = System.currentTimeMillis();

        // check for aux calculations otherwise these don't have stereo
        if (perceived.isEmpty()) {
            System.out.println("Performing auxiliary perception");

            Map<Centre<IAtom>, Descriptor> map = new HashMap<Centre<IAtom>, Descriptor>();


            for (Centre<IAtom> centre : unperceived) {

                centre.perceiveAuxiliary(unperceived, rule, calc);
                Descriptor descriptor = centre.perceive(auxrule, calc);

                if (descriptor != General.UNKNOWN)
                    map.put(centre, descriptor);


            }

            // transfer descriptors
            for (Map.Entry<Centre<IAtom>, Descriptor> entry : map.entrySet()) {
                unperceived.remove(entry.getKey());
                perceived.add(entry.getKey());
                entry.getKey().setDescriptor(entry.getValue());
            }

        }

        long end = System.currentTimeMillis();
        System.out.println(end - start);

        for (Centre<IAtom> centre : unperceived) {
            centre.setDescriptor(General.NONE);
            System.out.println(centre + ": " + centre.getDescriptor());
        }
        for (Centre<IAtom> centre : perceived) {
            System.out.println(centre + ": " + centre.getDescriptor());
            Set<IAtom> atoms = centre.getAtoms();
        }

    }


    public void write(String name, Centre centre) throws IOException {
        AbstractLigand<IAtom> ligand = ((AbstractLigand<IAtom>) centre);
        ligand.getProvider().build();


        Digraph digraph = (Digraph) ((AbstractLigand) centre).getProvider();

        CytoscapeWriter<IAtom> writer = new CytoscapeWriter<IAtom>(new File(name),
                                                                   digraph) {
            @Override
            public void mapAttributes(IAtom atom, Map<String, String> map) {
                map.put("symbol", atom.getSymbol());
                map.put("number", atom.getProperty("number").toString());
            }
        };
        writer.writeSif();
        writer.writeAttributes();
        writer.close();

    }

}
