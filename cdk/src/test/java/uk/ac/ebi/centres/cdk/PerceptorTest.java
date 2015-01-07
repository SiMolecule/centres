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

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.AtomContainerAtomPermutor;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import uk.ac.ebi.centres.Descriptor;
import uk.ac.ebi.centres.descriptor.General;
import uk.ac.ebi.centres.descriptor.Planar;
import uk.ac.ebi.centres.descriptor.Tetrahedral;

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
                     Tetrahedral.R,
                     container.getAtom(6).getProperty("descriptor"));
        assertEquals("invariance due to lone pair was not perceived",
                     Tetrahedral.S,
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

        assertEquals(Tetrahedral.r, container.getAtom(0).getProperty("descriptor"));
        assertEquals(Tetrahedral.R, container.getAtom(1).getProperty("descriptor"));
        assertEquals(Tetrahedral.S, container.getAtom(2).getProperty("descriptor"));
        assertEquals(Tetrahedral.s, container.getAtom(3).getProperty("descriptor"));
        assertEquals(Tetrahedral.R, container.getAtom(4).getProperty("descriptor"));
        assertEquals(Tetrahedral.S, container.getAtom(5).getProperty("descriptor"));

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

        assertEquals(Tetrahedral.r, container.getAtom(0).getProperty("descriptor"));
        assertEquals(Tetrahedral.S, container.getAtom(1).getProperty("descriptor")); // maps to atom 6 non inverse
        assertEquals(Tetrahedral.R, container.getAtom(2).getProperty("descriptor")); // maps to atom 5 non inverse
        assertEquals(Tetrahedral.s, container.getAtom(3).getProperty("descriptor"));
        assertEquals(Tetrahedral.S, container.getAtom(4).getProperty("descriptor")); // maps to atom 3 non inverse
        assertEquals(Tetrahedral.R, container.getAtom(5).getProperty("descriptor")); // maps to atom 2 non inverse

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
                     Planar.E, container.getBond(6).getProperty("descriptor"));

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
                     Planar.Z, container.getBond(6).getProperty("descriptor"));

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
                     Tetrahedral.S, container.getAtom(0).getProperty("descriptor"));


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
                     Tetrahedral.S, container.getAtom(0).getProperty("descriptor"));


    }


    @Test public void testVomifoliol() {

        String path = "(6R)-vomifoliol.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));

        assertNotNull("molecule was not loaded", container);

        perceptor.perceive(container);

        assertEquals("expected R conformation",
                     Tetrahedral.R, container.getAtom(12).getProperty("descriptor"));


    }

    @Test public void testCHEBI_73215() {

        String path = "CHEBI_73215.xml";
        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));

        perceptor.perceive(container);

        assertThat(container.getBond(48).getProperty("descriptor", Planar.class),
                   is(Planar.Z));

        assertThat(container.getAtom(0).getProperty("descriptor", Tetrahedral.class),
                   is(Tetrahedral.R));
        assertThat(container.getAtom(1).getProperty("descriptor", Tetrahedral.class),
                   is(Tetrahedral.S));
        assertThat(container.getAtom(2).getProperty("descriptor", Tetrahedral.class),
                   is(Tetrahedral.R));
        assertThat(container.getAtom(3).getProperty("descriptor", Tetrahedral.class),
                   is(Tetrahedral.R));
        assertThat(container.getAtom(4).getProperty("descriptor", Tetrahedral.class),
                   is(Tetrahedral.S));
        assertThat(container.getAtom(5).getProperty("descriptor", Tetrahedral.class),
                   is(Tetrahedral.S));

        assertThat(container.getAtom(17).getProperty("descriptor", Tetrahedral.class),
                   is(Tetrahedral.R));

    }

    @Test public void testIntradependants() throws CDKException {
        String path = "intradependants.xml";
        for (int i = 0; i < 20; i++) {
            IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream(path));
            
            //new MDLV2000Writer(System.out).write(container);

            perceptor.perceive(container);

            int j = 0;
            for (IAtom a : container.atoms()) {
                Descriptor descriptor = a.getProperty("descriptor");
                if (descriptor != General.UNKNOWN) {
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
                Descriptor descriptor = a.getProperty("descriptor");
                if (descriptor != General.UNKNOWN) {
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
                Descriptor descriptor = a.getProperty("descriptor");
                if (descriptor != General.UNKNOWN) {
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
                Descriptor descriptor = a.getProperty("descriptor");
                if (descriptor != General.UNKNOWN) {
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
        assertThat(container.getAtom(0).getProperty("descriptor"), CoreMatchers.<Object>is(Tetrahedral.R));
        System.out.println(container.getAtom(1).getProperty("descriptor")); // check this one
        assertThat(container.getAtom(3).getProperty("descriptor"), CoreMatchers.<Object>is(Tetrahedral.S));
    }

    @Test public void mixed_h_representation() {
        String path = "mixed_h_representation.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        System.out.println(container.getAtom(1).getProperty("descriptor"));
        assertThat(container.getAtom(1).getProperty("descriptor"), CoreMatchers.<Object>is(General.NONE));
    }

    @Test public void bad_h_representation() {
        String path = "bad_h_labels.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        assertThat(container.getAtom(1).getProperty("descriptor"), CoreMatchers.<Object>is(General.NONE));
    }

    @Test public void r_sarin() {
        String path = "r_sarin.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        assertThat(container.getAtom(4).getProperty("descriptor"), CoreMatchers.<Object>is(Tetrahedral.R));
    }

    @Test public void s_sarin() {
        String path = "s_sarin.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        perceptor.perceive(container);
        assertThat(container.getAtom(4).getProperty("descriptor"), CoreMatchers.<Object>is(Tetrahedral.S));
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
        assertThat(container.getAtom(1).getProperty("descriptor"), CoreMatchers.<Object>is(Tetrahedral.S));
    }

    // ChEBI 4991 throws an exception
    @Test public void chebi_4991() {
        String path = "CHEBI_4991.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
    }

    @Test public void chebi_66261() {
        String path = "CHEBI_66261.mol";
        IAtomContainer container = MolLoader.loadMolfile(getClass().getResourceAsStream(path));
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(container);
        perceptor.perceive(container);
        assertThat(container.getAtom(0).getProperty("descriptor"), CoreMatchers.<Object>is(Tetrahedral.R));
        assertThat(container.getAtom(2).getProperty("descriptor"), CoreMatchers.<Object>is(Tetrahedral.S));
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

            assertEquals(Tetrahedral.R, container.getAtom(0).getProperty("descriptor"));
            assertEquals(Tetrahedral.s, container.getAtom(1).getProperty("descriptor"));
            assertEquals(Tetrahedral.S, container.getAtom(2).getProperty("descriptor"));
            assertEquals(Tetrahedral.R, container.getAtom(3).getProperty("descriptor"));
            assertEquals(Tetrahedral.r, container.getAtom(4).getProperty("descriptor"));
            assertEquals(Tetrahedral.S, container.getAtom(5).getProperty("descriptor"));
        }

    }
}
