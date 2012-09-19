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

import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import uk.ac.ebi.mdk.domain.identifier.ChEBIIdentifier;
import uk.ac.ebi.mdk.service.DefaultServiceManager;
import uk.ac.ebi.mdk.service.query.name.PreferredNameService;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author John May
 */
public class TestChEBI {

    public static void main(String[] args) throws IOException, CDKException, InterruptedException {

        IteratingMDLReader reader = new IteratingMDLReader(new BufferedInputStream(new FileInputStream("/databases/chebi/ChEBI_lite.sdf"), 4096),
                                                           SilentChemObjectBuilder.getInstance(),
                                                           true);

        final List<IAtomContainer> containers = new ArrayList<IAtomContainer>(12000);

        long rStart = System.currentTimeMillis();
        while (reader.hasNext()) {
            IAtomContainer container = reader.next();
            containers.add(container);
        }
        long rEnd = System.currentTimeMillis();

        reader.close();

        System.out.println("Read: " + (rEnd - rStart) + " ms");

        List<IAtomContainer> bad = new ArrayList<IAtomContainer>();
        IsotopeFactory factory = IsotopeFactory.getInstance(SilentChemObjectBuilder.getInstance());

        //Set<String> ignore = new HashSet<String>(Arrays.asList("CHEBI:3499", "CHEBI:16155", "CHEBI:16157", "CHEBI:16322", "CHEBI:16323", "CHEBI:16324", "CHEBI:16507", "CHEBI:16508", "CHEBI:17401", "CHEBI:17402", "CHEBI:17403", "CHEBI:17404", "CHEBI:17405", "CHEBI:17406", "CHEBI:17407", "CHEBI:17409", "CHEBI:17410", "CHEBI:17411"));

        long tStart = System.currentTimeMillis();
        for (int i = 0; i < containers.size(); i++) {
            IAtomContainer container = containers.get(i);
            try {
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
                for (IAtom atom : container.atoms()) {
                    if (atom.getSymbol().equals("R"))
                        atom.setMassNumber(0);
                    if (atom.getMassNumber() == null) {
                        bad.add(container);
                    }
                }
                CDKHueckelAromaticityDetector.detectAromaticity(container);
            } catch (CDKException ex) {
                System.err.println(ex.getMessage());
                bad.add(container);
            }
        }

        System.out.println(bad.size());

        long tEnd = System.currentTimeMillis();
        System.out.println("Typed: " + (tEnd - tStart) + " ms");

        // remove problem molecules
        containers.removeAll(bad);


        final PreferredNameService<ChEBIIdentifier> names = DefaultServiceManager.getInstance().getService(ChEBIIdentifier.class,
                                                                                                           PreferredNameService.class);
        final List<ChEBIIdentifier> timeout = new ArrayList<ChEBIIdentifier>();


        long pStart = System.currentTimeMillis();
        int count = 0;
        for (final IAtomContainer container : containers) {

            if (++count % 100 == 100) {
                System.out.println("[" + count + "/" + containers.size() + "]");
            }

            ChEBIIdentifier identifier = new ChEBIIdentifier(container.getProperty("ChEBI ID").toString());
            try {
                CDKPerceptor perceptor = new CDKPerceptor();
                perceptor.perceive(container);
                perceptor.shutdown();
            } catch (RuntimeException ex) {
                System.err.println("Combinatorial explosion possible " + identifier);
            } catch (TimeoutException e) {
                System.out.println("Timeout whilst perceiving (" + identifier + "): " + new CDKCentreProvider(container).getCentres(new CDKManager(container)) + " centres");
                if (identifier.getAccession().equals("CHEBI:51442")) {
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException e1) {
                        System.err.println(e1.getMessage());
                    }
                }

                timeout.add(identifier);
            }
        }

        long pEnd = System.currentTimeMillis();
        System.out.println("Perceived: " + (pEnd - pStart) + " ms");

        System.out.println("Timeouts: ");
        for (ChEBIIdentifier identifier : timeout) {
            System.out.println(names.getPreferredName(identifier) + " (" + identifier + ") timed out");
        }

        System.out.println("Completed: " + (containers.size() - timeout.size()) / (double) containers.size() * 100);


    }

}
