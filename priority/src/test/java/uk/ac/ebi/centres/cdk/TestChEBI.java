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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author John May
 */
public class TestChEBI {

    public static void main(String[] args) throws IOException, CDKException, InterruptedException {

        IteratingMDLReader reader = new IteratingMDLReader(new BufferedInputStream(new FileInputStream("/databases/chebi/ChEBI_lite.sdf"), 4096),
                                                           SilentChemObjectBuilder.getInstance(),
                                                           true);

        final List<IAtomContainer> containers = new ArrayList<IAtomContainer>(12000);

        int max = 2000;

        long rStart = System.currentTimeMillis();
        while (reader.hasNext()) {
            containers.add(reader.next());
            if (containers.size() == max)
                break;
        }
        long rEnd = System.currentTimeMillis();

        reader.close();

        System.out.println("Read: " + (rEnd - rStart) + " ms");

        List<IAtomContainer> bad = new ArrayList<IAtomContainer>();
        IsotopeFactory factory = IsotopeFactory.getInstance(SilentChemObjectBuilder.getInstance());

        long tStart = System.currentTimeMillis();
        for (int i = 0; i < containers.size(); i++) {
            IAtomContainer container = containers.get(i);
            try {
                if (container.getAtomCount() > 60)
                    bad.add(container);
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

        System.out.println("Tell me when:");
        new Scanner(System.in).next();

        final CDKPerceptor perceptor = new CDKPerceptor();
        final List<RuntimeException> exceptions = new ArrayList<RuntimeException>();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                long pStart = System.currentTimeMillis();

                for (IAtomContainer container : containers) {
                    try {
                        perceptor.perceive(container);
                    } catch (OutOfMemoryError err) {
                        System.err.println("OOME: " + container.getProperty("ChEBI ID"));
                    } catch (IllegalArgumentException ex) {
                        System.err.println("Check this molecule: " + container.getProperty("ChEBI ID"));
                    } catch (RuntimeException ex) {
                        exceptions.add(ex);
                    }
                }
                long pEnd = System.currentTimeMillis();
                System.out.println("Perceived: " + (pEnd - pStart) + " ms");

                System.out.println("Exceptions: ");
                for (RuntimeException ex : exceptions) {
                    ex.printStackTrace();
                }
            }
        });
        t.setName("Perception");
        t.start();
        t.join();


    }

}
