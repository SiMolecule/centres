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
import uk.ac.ebi.mdk.domain.identifier.Identifier;
import uk.ac.ebi.mdk.service.DefaultServiceManager;
import uk.ac.ebi.mdk.service.query.name.PreferredNameService;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author John May
 */
public class TestChEBI {

    public static void main(String[] args) throws IOException, CDKException, InterruptedException {

        IteratingMDLReader reader = new IteratingMDLReader(new BufferedInputStream(new FileInputStream("/databases/chebi/ChEBI_lite.sdf"), 4096),
                                                           SilentChemObjectBuilder.getInstance(),
                                                           true);

         List<IAtomContainer> containers = new ArrayList<IAtomContainer>(12000);

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

        //containers = containers.subList(14500, containers.size());

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
        final Map<Identifier, Long> timing = new HashMap<Identifier, Long>();

        CDKPerceptor perceptor = new CDKPerceptor();

        for (final IAtomContainer container : containers) {
            ChEBIIdentifier identifier = new ChEBIIdentifier(container.getProperty("ChEBI ID").toString());
            try {
//                long innerStart = System.currentTimeMillis();
                perceptor.perceive(container);
//                long innerEnd = System.currentTimeMillis();
//                timing.put(identifier, innerEnd - innerStart);
            } catch (RuntimeException ex) {
                System.err.println("Combinatorial explosion possible " + identifier + ": " + ex.getMessage());
            }
        }

        long pEnd = System.currentTimeMillis();
        System.out.println("Perceived: " + (pEnd - pStart) + " ms");

        System.out.println("Completed: " + (containers.size() - timeout.size()) / (double) containers.size() * 100);

        Map<Identifier, Long> orderedMap = new TreeMap<Identifier, Long>(new Comparator<Identifier>() {
            @Override
            public int compare(Identifier o1, Identifier o2) {
                return timing.get(o2).compareTo(timing.get(o1));
            }
        });

        System.out.println("Timings:");
        int printcount = 0;
        orderedMap.putAll(timing);
        for (Map.Entry<Identifier, Long> e : orderedMap.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
            if (++printcount == 20)
                break;
        }

    }

}
