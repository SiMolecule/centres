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

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.AtomContainerAtomPermutor;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import uk.ac.ebi.centres.Descriptor;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * This test suite tests correct perception of several difficult cases.
 *
 * @author John May
 */
public class PerceptorTest {

    private CDKPerceptor perceptor = new CDKPerceptor();

    /**
     * Tests whether the sulphur dioxide tetrahedral is handled
     *
     * @throws Exception
     */
    @Test
    public void testAlliin() throws Exception {

        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream("alliin.xml"));

        assertNotNull("molecule was not loaded", container);

        perceptor.perceive(container);

        assertEquals("incorrect descriptor for c7 of alliin",
                     Descriptor.R,
                     container.getAtom(6).getProperty("descriptor"));
        assertEquals("invariance due to lone pair was not perceived",
                     Descriptor.S,
                     container.getAtom(3).getProperty("descriptor"));

    }

    /**
     * Test the symmetric myo-inositol
     *
     * @throws Exception
     */
    @Test public void testMyoinositol() throws Exception {

        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream("myo-inositol.xml"));

        assertNotNull("molecule was not loaded", container);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);                                            
        perceptor.perceive(container);

        assertEquals(Descriptor.r, container.getAtom(0).getProperty("descriptor"));
        assertEquals(Descriptor.R, container.getAtom(1).getProperty("descriptor"));
        assertEquals(Descriptor.S, container.getAtom(2).getProperty("descriptor"));
        assertEquals(Descriptor.s, container.getAtom(3).getProperty("descriptor"));
        assertEquals(Descriptor.R, container.getAtom(4).getProperty("descriptor"));
        assertEquals(Descriptor.S, container.getAtom(5).getProperty("descriptor"));

    }

    /**
     * Test the symmetric myo-inositol (alternate drawing [wedge/hatch
     * flipped])
     *
     * @throws Exception
     */
    @Test
    public void testMyoinositol_inverse() throws Exception {

        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream("myo-inositol.xml"));

        assertNotNull("molecule was not loaded", container);

        for (IBond bond : container.bonds()) {
            if (IBond.Stereo.DOWN.equals(bond.getStereo()))
                bond.setStereo(IBond.Stereo.UP);
            else if (IBond.Stereo.UP.equals(bond.getStereo()))
                bond.setStereo(IBond.Stereo.DOWN);
        }

        perceptor.perceive(container);

        assertEquals(Descriptor.r, container.getAtom(0).getProperty("descriptor"));
        assertEquals(Descriptor.S, container.getAtom(1).getProperty("descriptor")); // maps to atom 6 non inverse
        assertEquals(Descriptor.R, container.getAtom(2).getProperty("descriptor")); // maps to atom 5 non inverse
        assertEquals(Descriptor.s, container.getAtom(3).getProperty("descriptor"));
        assertEquals(Descriptor.S, container.getAtom(4).getProperty("descriptor")); // maps to atom 3 non inverse
        assertEquals(Descriptor.R, container.getAtom(5).getProperty("descriptor")); // maps to atom 2 non inverse

    }


    @Test
    public void testE22Furyl35nitro2furylacrylamide() {

        String path = "(E)-2-(2-Furyl)-3-(5-nitro-2-furyl)acrylamide.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));

        assertNotNull("molecule was not loaded", container);

        // the E may actually be read at some point in future but not atm
        assertNotSame(IBond.Stereo.E, container.getBond(6).getStereo());

        assertNull("descriptor should be null before perception",
                   container.getBond(6).getProperty("descriptor"));

        perceptor.perceive(container);

        assertEquals("Expected E conformation",
                     Descriptor.E, container.getBond(6).getProperty("descriptor"));

    }

    /**
     * This also uses 3D cordinates
     */
    @Test
    public void testZ22Furyl35nitro2furylacrylamide() {

        String path = "(Z)-2-(2-Furyl)-3-(5-nitro-2-furyl)acrylamide.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));

        assertNotNull("molecule was not loaded", container);

        // the E may actually be read at some point in future but not atm
        assertNotSame(IBond.Stereo.E, container.getBond(6).getStereo());

        assertNull("descriptor should be null before perception",
                   container.getBond(6).getProperty("descriptor"));

        CDKPerceptor perceptor3D = new CDKPerceptor(new CDK3DSignCalculator());
        perceptor3D.perceive(container);

        assertEquals("Expected Z conformation",
                     Descriptor.Z, container.getBond(6).getProperty("descriptor"));

    }

    /**
     * Tests that stereo-centres involving a pseudo atom give a meaningful
     * descriptor.
     */
    @Test public void testImplicitPsuedo() {

        String path = "implicitPseudoCentre.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));

        assertNotNull("molecule was not loaded", container);

        perceptor.perceive(container);

        assertEquals("Expected S conformation",
                     Descriptor.S, container.getAtom(0).getProperty("descriptor"));


    }

    /**
     * Tests that stereo-centres involving a pseudo atom do not change given
     * explicit hydrogens.
     */
    @Test public void testExplicitPsuedo() {

        String path = "explicitPseudoCentre.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));

        assertNotNull("molecule was not loaded", container);

        perceptor.perceive(container);

        assertEquals("Expected S conformation",
                     Descriptor.S, container.getAtom(0).getProperty("descriptor"));


    }


    @Test public void testVomifoliol() {

        String path = "(6R)-vomifoliol.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));

        assertNotNull("molecule was not loaded", container);

        perceptor.perceive(container);

        assertEquals("expected R conformation",
                     Descriptor.R, container.getAtom(12).getProperty("descriptor"));


    }

    @Test public void testCHEBI_73215() {

        String path = "CHEBI_73215.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));

        perceptor.perceive(container);

        assertThat(container.getBond(48).getProperty("descriptor", Descriptor.class),
                   is(Descriptor.Z));

        assertThat(container.getAtom(0).getProperty("descriptor", Descriptor.class),
                   is(Descriptor.R));
        assertThat(container.getAtom(1).getProperty("descriptor", Descriptor.class),
                   is(Descriptor.S));
        assertThat(container.getAtom(2).getProperty("descriptor", Descriptor.class),
                   is(Descriptor.R));
        assertThat(container.getAtom(3).getProperty("descriptor", Descriptor.class),
                   is(Descriptor.R));
        assertThat(container.getAtom(4).getProperty("descriptor", Descriptor.class),
                   is(Descriptor.S));
        assertThat(container.getAtom(5).getProperty("descriptor", Descriptor.class),
                   is(Descriptor.S));

        assertThat(container.getAtom(17).getProperty("descriptor", Descriptor.class),
                   is(Descriptor.R));

    }

    @Test public void testIntradependants() throws CDKException {
        String path = "intradependants.xml";
        for (int i = 0; i < 20; i++) {
            IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));
            
            //new MDLV2000Writer(System.out).write(container);

            perceptor.perceive(container);

            int j = 0;
            for (IAtom a : container.atoms()) {
                uk.ac.ebi.centres.Descriptor descriptor = a.getProperty("descriptor");
                if (descriptor != Descriptor.Unknown) {
                    if (j++ > 0)
                        System.out.print(", ");
                    System.out.print(a.getSymbol() + ((container.getAtomNumber(a) + 1)) + ": " + descriptor);
                }
            }
            System.out.println();
        }
    }
    
    @Test public void doubleIntradependant() throws CDKException {
        String path = "double-intradependant.xml";
        for (int i = 0; i < 1; i++) {
            IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));
            
            //new MDLV2000Writer(System.out).write(container);

            perceptor.perceive(container);

            int j = 0;
            for (IAtom a : container.atoms()) {
                uk.ac.ebi.centres.Descriptor descriptor = a.getProperty("descriptor");
                if (descriptor != Descriptor.Unknown) {
                    if (j++ > 0)
                        System.out.print(", ");
                    System.out.print(a.getSymbol() + ((container.getAtomNumber(a) + 1)) + ": " + descriptor);
                }
            }
            System.out.println();
        }
    }
    
    @Test public void testIntradependants2() throws CDKException {
        String path = "unsolvable.xml";
        for (int i = 0; i < 1; i++) {
            IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));
            new MDLV2000Writer(System.out).write(container);
            perceptor.perceive(container);

            int j = 0;
            for (IAtom a : container.atoms()) {
                uk.ac.ebi.centres.Descriptor descriptor = a.getProperty("descriptor");
                if (descriptor != Descriptor.Unknown) {
                    if (j++ > 0)
                        System.out.print(", ");
                    System.out.print(a.getSymbol() + ((container.getAtomNumber(a) + 1)) + ": " + descriptor);
                }
            }
            System.out.println();
        }
    }
    
    @Test public void pairrule() throws CDKException {
        String path = "pairrule.xml";
        for (int i = 0; i < 10; i++) {
            IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));

            //new MDLV2000Writer(System.out).write(container);
            
            perceptor.perceive(container);

            int j = 0;
            for (IAtom a : container.atoms()) {
                uk.ac.ebi.centres.Descriptor descriptor = a.getProperty("descriptor");
                if (descriptor != Descriptor.Unknown) {
                    if (j++ > 0)
                        System.out.print(", ");
                    System.out.print(a.getSymbol() + ((container.getAtomNumber(a) + 1)) + ": " + descriptor);
                }
            }
            System.out.println();
        }
    }
    
    @Test public void chebi_3353() {
        String path = "CHEBI_3353.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        System.out.println(container.getAtom(3).getProperty("descriptor"));
        System.out.println(container.getAtom(9).getProperty("descriptor"));
        System.out.println(container.getAtom(15).getProperty("descriptor"));
    }

    @Test public void chebi_15645() {
        String path = "CHEBI_15645.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        System.out.println(container.getAtom(12).getProperty("descriptor"));
        System.out.println(container.getAtom(14).getProperty("descriptor"));
        System.out.println(container.getAtom(16).getProperty("descriptor"));
        System.out.println(container.getAtom(17).getProperty("descriptor"));
    }
    
    @Test public void chebi_15406() {
        String path = "CHEBI_15406.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        System.out.println(container.getAtom(0).getProperty("descriptor"));
        System.out.println(container.getAtom(5).getProperty("descriptor"));
    }
    
    @Test public void chebi_2955() {
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream("CHEBI_2955.mol"));
        perceptor.perceive(container);
        System.out.println(container.getAtom(7).getProperty("descriptor"));
    }
    
    @Test public void chebi_3049() {
        String path = "CHEBI_3049.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        assertThat(container.getAtom(0).getProperty("descriptor"), CoreMatchers.<Object>is(Descriptor.R));
        System.out.println(container.getAtom(1).getProperty("descriptor")); // check this one
        assertThat(container.getAtom(3).getProperty("descriptor"), CoreMatchers.<Object>is(Descriptor.S));
    }

    @Test public void mixed_h_representation() {
        String path = "mixed_h_representation.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        System.out.println(container.getAtom(1).getProperty("descriptor"));
        assertThat(container.getAtom(1).getProperty("descriptor"), CoreMatchers.<Object>is(Descriptor.None));
    }

    @Test public void bad_h_representation() {
        String path = "bad_h_labels.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        assertThat(container.getAtom(1).getProperty("descriptor"), CoreMatchers.<Object>is(Descriptor.None));
    }

    @Test public void r_sarin() {
        String path = "r_sarin.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        assertThat(container.getAtom(4).getProperty("descriptor"), CoreMatchers.<Object>is(Descriptor.R));
    }

    @Test public void s_sarin() {
        String path = "s_sarin.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        assertThat(container.getAtom(4).getProperty("descriptor"), CoreMatchers.<Object>is(Descriptor.S));
    }
    
    @Test public void chebi_16419() {
        String path = "CHEBI_16419.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
        System.out.println(container.getAtom(10).getProperty("descriptor"));
    }

    @Test public void chebi_61677() {
        String path = "CHEBI_61677.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
        assertThat(container.getAtom(1).getProperty("descriptor"), CoreMatchers.<Object>is(Descriptor.S));
    }

    // ChEBI 4991 used to throw an exception
    @Test public void chebi_4991() {
        String path = "CHEBI_4991.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
    }

    @Test public void chebi_33517() {
        String path = "CHEBI_33517.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
    }

    @Test public void chebi_82965() {
        String path = "CHEBI_82965.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
    }

    // previously causing stack overflow
    @Test public void chebi_53643() {
        String path = "CHEBI_53643.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
    }

    // previously causing NPE
    @Test public void chebi_2639() {
        String path = "CHEBI_2639.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
    }

    // affected by Hydrogen representation???
    @Test public void chebi_10642() {
        String path = "chebi_10642.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        //AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
        for (IAtom atom : container.atoms()) {
            System.out.println(atom.getProperty("descriptor"));
        }
    }

    @Test public void chebi_66261() {
        String path = "CHEBI_66261.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
        assertThat(container.getAtom(0).getProperty("descriptor"), CoreMatchers.<Object>is(Descriptor.R));
        assertThat(container.getAtom(2).getProperty("descriptor"), CoreMatchers.<Object>is(Descriptor.S));
    }

    @Test public void demo() {
        String path = "demo.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
        System.out.println(container.getAtom(3).getProperty("descriptor"));
    } 
    
    @Test public void priority_test() {
        String path = "priority-test.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
        System.out.println(container.getAtom(2).getProperty("descriptor"));
    }

    @Ignore public void pathological_case() {
        String path = "pathological-case.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
        for (IAtom atom : container.atoms()){
            System.out.println(atom.getProperty("number") + ": " + atom.getProperty("descriptor"));
        }
    }

    @Test public void handbook_example_8() {
        String path = "handbook_example_8.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        for (IAtom atom : container.atoms()){
            System.out.println(atom.getProperty("number") + ": " + atom.getProperty("descriptor"));
        }
    }

    @Test public void handbook_example_9() {
        String path = "handbook_example_9.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        for (IAtom atom : container.atoms()){
            System.out.println(atom.getProperty("number") + ": " + atom.getProperty("descriptor"));
        }
    }

    @Test public void ligand_priorities() {
        String path = "daniel.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
        for (IAtom atom : container.atoms())
            System.out.println(atom.getProperty("descriptor"));
        assertThat(container.getAtom(1).getProperty("descriptor"),
                   CoreMatchers.<Object>is(Descriptor.R));
    }

    @Test public void chebi_17268() throws Exception {

        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream("CHEBI_17268.xml"));

        assertNotNull("molecule was not loaded", container);

        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        Isotopes.getInstance().configureAtoms(container);
        
        int i = 0;
        AtomContainerAtomPermutor acap = new AtomContainerAtomPermutor(container);
        while (acap.hasNext()) {
            container = acap.next();
            
            perceptor.perceive(container);

            System.out.print(i++ + " ");
            System.out.print(container.getAtom(0).getProperty("descriptor") + ", ");
            System.out.print(container.getAtom(1).getProperty("descriptor") + ", ");
            System.out.print(container.getAtom(2).getProperty("descriptor") + ", ");
            System.out.print(container.getAtom(3).getProperty("descriptor") + ", ");
            System.out.print(container.getAtom(4).getProperty("descriptor") + ", ");
            System.out.println(container.getAtom(5).getProperty("descriptor"));

            assertEquals(uk.ac.ebi.centres.Descriptor.R, container.getAtom(0).getProperty("descriptor"));
            assertEquals(uk.ac.ebi.centres.Descriptor.s, container.getAtom(1).getProperty("descriptor"));
            assertEquals(uk.ac.ebi.centres.Descriptor.S, container.getAtom(2).getProperty("descriptor"));
            assertEquals(uk.ac.ebi.centres.Descriptor.R, container.getAtom(3).getProperty("descriptor"));
            assertEquals(uk.ac.ebi.centres.Descriptor.r, container.getAtom(4).getProperty("descriptor"));
            assertEquals(uk.ac.ebi.centres.Descriptor.S, container.getAtom(5).getProperty("descriptor"));
        }

    }

    @Test public void hydroxycyclobutane() throws Exception {

        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream("hydroxy-cyclobutane.cml"));

        assertNotNull("molecule was not loaded", container);

        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        Isotopes.getInstance().configureAtoms(container);

        perceptor.perceive(container);
        for (IAtom atom : container.atoms()) {
            System.out.println(atom.getProperty("descriptor"));
        }

        int i = 0;
        AtomContainerAtomPermutor acap = new AtomContainerAtomPermutor(container);
        while (acap.hasNext()) {
            IAtomContainer permuted = acap.next();

            perceptor.perceive(permuted);

            System.out.print(i++ + " ");
            System.out.print(container.getAtom(0).getProperty("descriptor") + ", ");
            System.out.print(container.getAtom(2).getProperty("descriptor"));
            System.out.println();
        }

    }

    @Test
    public void testBobExample() throws Exception {
        SmilesParser smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());
        IAtomContainer mol = smipar.parseSmiles("[13C@@H]12C3C1.C2=CC3");
        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        sdg.generateCoordinates(mol);
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
        perceptor.perceive(mol);
        for (IAtom atom : mol.atoms()) {
            //if (atom.getProperty("descriptor") != null && atom.getProperty("descriptor") != General.UNKNOWN)
            System.err.println(mol.getAtomNumber(atom) + " : " + atom.getProperty("descriptor") );
        }
    }

    @Test
    public void testHandBook() throws Exception {
        SmilesParser              smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());
        String                    smi = "C[C@H]1[C@@H](C)[C@@H](C)[C@@H](C)[C@H](C)[C@H](C)[C@H](C)[C@@H]1C";
        label(smipar, smi);

    }

    @Test
    public void testCHEBI17521() throws Exception {
        SmilesParser              smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());
        String                    smi = "O[C@@H]1C[C@@](O)(C[C@@H](O)[C@H]1O)C(O)=O\n";
        label(smipar, smi);
    }

    private void label(SmilesParser smipar, String smi1) throws CDKException
    {
        IAtomContainer            mol = smipar.parseSmiles(smi1);
        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        sdg.generateCoordinates(mol);
        AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
        perceptor.perceive(mol);
        for (IAtom atom : mol.atoms()) {
            if (atom.getProperty("descriptor") != null && atom.getProperty("descriptor") != Descriptor.Unknown)
                System.out.println(mol.getAtomNumber(atom) + " : " + atom.getProperty("descriptor"));
        }
    }

    @Test
    public void mancudeRingSystems() throws Exception {
        IAtomContainer mol1 = MolLoader.loadMolfile(getClass().getResourceAsStream("Daniel_Macude_1.mol"));
        IAtomContainer mol2 = MolLoader.loadMolfile(getClass().getResourceAsStream("Daniel_Macude_2.mol"));
        IAtomContainer mol3 = MolLoader.loadMolfile(getClass().getResourceAsStream("Daniel_Macude_3.mol"));
        IAtomContainer mol4 = MolLoader.loadMolfile(getClass().getResourceAsStream("Daniel_Macude_4.mol"));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol1);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol2);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol3);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol4);


        perceptor.perceive(mol1);
        perceptor.perceive(mol2);
        perceptor.perceive(mol3);
        perceptor.perceive(mol4);

        System.err.println("1:");
        for (IAtom atom : mol1.atoms()) {
            if (atom.getProperty("descriptor") != null && atom.getProperty("descriptor") != Descriptor.Unknown)
                System.err.println(mol1.getAtomNumber(atom) + " : " + atom.getProperty("descriptor") );
        }
        System.err.println("2:");
        for (IAtom atom : mol2.atoms()) {
            if (atom.getProperty("descriptor") != null && atom.getProperty("descriptor") != Descriptor.Unknown)
                System.err.println(mol2.getAtomNumber(atom) + " : " + atom.getProperty("descriptor"));
        }
        System.err.println("3:");
        for (IAtom atom : mol3.atoms()) {
            if (atom.getProperty("descriptor") != null && atom.getProperty("descriptor") != Descriptor.Unknown)
                System.err.println(mol3.getAtomNumber(atom) + " : " + atom.getProperty("descriptor"));
        }
        System.err.println("4:");
        for (IAtom atom : mol4.atoms()) {
            if (atom.getProperty("descriptor") != null && atom.getProperty("descriptor") != Descriptor.Unknown)
                System.err.println(mol4.getAtomNumber(atom) + " : " + atom.getProperty("descriptor"));
        }
    }
}
