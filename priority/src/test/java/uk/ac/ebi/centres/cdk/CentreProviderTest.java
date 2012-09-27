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
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.periodictable.PeriodicTable;
import uk.ac.ebi.centres.Centre;
import uk.ac.ebi.centres.DefaultPerceptor;
import uk.ac.ebi.centres.Digraph;
import uk.ac.ebi.centres.Perceptor;
import uk.ac.ebi.centres.PriorityRule;
import uk.ac.ebi.centres.graph.ConnectionTableDigraph;
import uk.ac.ebi.centres.graph.DefaultDescriptorManager;
import uk.ac.ebi.centres.io.CytoscapeWriter;
import uk.ac.ebi.centres.ligand.AbstractLigand;
import uk.ac.ebi.centres.priority.AtomicNumberRule;
import uk.ac.ebi.centres.priority.CombinedRule;
import uk.ac.ebi.centres.priority.MassNumberRule;
import uk.ac.ebi.centres.priority.access.AtomicNumberAccessor;
import uk.ac.ebi.centres.priority.access.MassNumberAccessor;
import uk.ac.ebi.centres.priority.access.descriptor.AuxiliaryDescriptor;
import uk.ac.ebi.centres.priority.access.descriptor.PrimaryDescriptor;
import uk.ac.ebi.centres.priority.descriptor.PairRule;
import uk.ac.ebi.centres.priority.descriptor.RSRule;
import uk.ac.ebi.centres.priority.descriptor.ZERule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author John May
 */
public class CentreProviderTest {

    @Test
    public void testGetCentres() throws CDKException, IOException, ExecutionException, TimeoutException, InterruptedException {

        final IAtomContainer container = CMLLoader.loadCML(getClass().getResourceAsStream("myo-inositol.xml"));

        // setting correct properties :/
        for (IAtom atom : container.atoms()) {
            atom.setAtomicNumber(PeriodicTable.getAtomicNumber(atom.getSymbol()));
            atom.setMassNumber(IsotopeFactory.getInstance(atom.getBuilder()).getMajorIsotope(atom.getSymbol()).getMassNumber());
        }
        AtomContainerManipulator.percieveAtomTypesAndConfigureUnsetProperties(container);

        long start = System.currentTimeMillis();
        System.out.println("Number of centres: " + new CDKCentreProvider(container).getCentres(new DefaultDescriptorManager<IAtom>()).size());
        long end = System.currentTimeMillis();

        System.out.println(end - start);

        PriorityRule<IAtom> rule = new CombinedRule<IAtom>(
                new AtomicNumberRule<IAtom>(new AtomicNumberAccessor<IAtom>() {
                    @Override
                    public int getAtomicNumber(IAtom atom) {
                        return atom.getAtomicNumber();
                    }
                }),
                new MassNumberRule<IAtom>(new MassNumberAccessor<IAtom>() {
                    @Override
                    public int getMassNumber(IAtom atom) {
                        return atom.getMassNumber();
                    }
                }),
                new ZERule<IAtom>(),
                new PairRule<IAtom>(new PrimaryDescriptor<IAtom>()),
                new RSRule<IAtom>(new PrimaryDescriptor<IAtom>())
        );

        PriorityRule<IAtom> auxrule = new CombinedRule<IAtom>(
                new AtomicNumberRule<IAtom>(new AtomicNumberAccessor<IAtom>() {
                    @Override
                    public int getAtomicNumber(IAtom atom) {
                        return atom.getAtomicNumber();
                    }
                }),
                new MassNumberRule<IAtom>(new MassNumberAccessor<IAtom>() {
                    @Override
                    public int getMassNumber(IAtom atom) {
                        return atom.getMassNumber();
                    }
                }),
                new ZERule<IAtom>(),
                new PairRule<IAtom>(new AuxiliaryDescriptor<IAtom>()),
                new RSRule<IAtom>(new AuxiliaryDescriptor<IAtom>()));

        final Perceptor<IAtom> perceptor = new DefaultPerceptor<IAtom>(rule,
                                                                       auxrule,
                                                                       new CDK2DSignCalculator());


        perceptor.perceive(new CDKCentreProvider(container), new CDKManager(container));

        perceptor.shutdown();

        Centre<IAtom> centre = new ArrayList<Centre<IAtom>>(new CDKCentreProvider(container).getCentres(new DefaultDescriptorManager<IAtom>())).get(0);


        Digraph<IAtom> digraph = new ConnectionTableDigraph<IAtom>(centre,
                                                                   new DefaultDescriptorManager<IAtom>(),
                                                                   new CDKConnectionTable(container));

        CytoscapeWriter<IAtom> writer = new CytoscapeWriter<IAtom>(new File("/Users/johnmay/Desktop/digraph"), digraph) {
            @Override
            public void mapAttributes(IAtom atom, Map<String, String> map) {
                map.put("atom.symbol", atom.getSymbol());
                map.put("atom.number", atom.getProperty("number").toString());
            }
        };
        System.out.println(centre);
        writer.writeSif();
        writer.writeAttributes();
        writer.close();


        for (IAtom atom : container.atoms()) {
            System.out.println(atom.getSymbol() + atom.getProperty("number")
                                       + ": " + atom.getProperty("descriptor"));
        }
        System.out.println("Bonds:");
        for (IBond bond : container.bonds()) {
            System.out.println(bond.getAtom(0).getSymbol() + "=" + bond.getAtom(1).getSymbol() + container.getBondNumber(bond)
                                       + ": " + bond.getProperty("descriptor"));
        }


    }


    /**
     * Simple method to dump the graph to a cytoscape file
     *
     * @param name
     * @param centre
     *
     * @throws IOException
     */
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
