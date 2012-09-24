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

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import uk.ac.ebi.centres.Descriptor;
import uk.ac.ebi.centres.descriptor.General;
import uk.ac.ebi.mdk.domain.identifier.ChEBIIdentifier;
import uk.ac.ebi.mdk.service.DefaultServiceManager;
import uk.ac.ebi.mdk.service.ServiceManager;
import uk.ac.ebi.mdk.service.query.structure.StructureService;

import java.util.Scanner;


/**
 * @author John May
 */
public class TestChEBIEntry {

    public static void main(String[] args) throws CDKException, InterruptedException {

        ServiceManager manager = DefaultServiceManager.getInstance();
        StructureService<ChEBIIdentifier> service = manager.getService(ChEBIIdentifier.class,
                                                                       StructureService.class);

        final IAtomContainer container = service.getStructure(new ChEBIIdentifier("CHEBI:63067"));

        final CDKPerceptor perceptor = new CDKPerceptor();
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);

        new Scanner(System.in).next();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                perceptor.perceive(container);
                for (IAtom atom : container.atoms()) {
                    Descriptor descriptor = (Descriptor) atom.getProperty("descriptor");
                    if (descriptor != General.NONE)
                        System.out.println((container.getAtomNumber(atom) + 1) + ": " + descriptor);
                }
                long end = System.currentTimeMillis();
                System.out.println(end - start);
            }
        });
        t.setName("PERCEPTION");
        t.start();
        t.join();


    }

}
