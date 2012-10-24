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

import junit.framework.Assert;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import uk.ac.ebi.centres.descriptor.Planar;
import uk.ac.ebi.centres.descriptor.Tetrahedral;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertNull;

/**
 * This test suite tests correct perception of several difficult
 * cases.
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

        IAtomContainer container = CMLLoader.loadCML(getClass().getResourceAsStream("alliin.xml"));

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
     * @throws Exception
     */
    @Test
    public void testMyoinositol() throws Exception {

        IAtomContainer container = CMLLoader.loadCML(getClass().getResourceAsStream("myo-inositol.xml"));

        assertNotNull("molecule was not loaded", container);

        perceptor.perceive(container);

        assertEquals(Tetrahedral.r, container.getAtom(0).getProperty("descriptor"));
        assertEquals(Tetrahedral.R, container.getAtom(1).getProperty("descriptor"));
        assertEquals(Tetrahedral.S, container.getAtom(2).getProperty("descriptor"));
        assertEquals(Tetrahedral.s, container.getAtom(3).getProperty("descriptor"));
        assertEquals(Tetrahedral.R, container.getAtom(4).getProperty("descriptor"));
        assertEquals(Tetrahedral.S, container.getAtom(5).getProperty("descriptor"));

    }

    /**
     * Test the symmetric myo-inositol (alternate drawing [wedge/hatch flipped])
     * @throws Exception
     */
    @Test
    public void testMyoinositol_inverse() throws Exception {

        IAtomContainer container = CMLLoader.loadCML(getClass().getResourceAsStream("myo-inositol.xml"));

        assertNotNull("molecule was not loaded", container);

        for(IBond bond : container.bonds() ){
            if(IBond.Stereo.DOWN.equals(bond.getStereo()))
                bond.setStereo(IBond.Stereo.UP);
            else if(IBond.Stereo.UP.equals(bond.getStereo()))
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

        String         path      = "(E)-2-(2-Furyl)-3-(5-nitro-2-furyl)acrylamide.xml";
        IAtomContainer container = CMLLoader.loadCML(getClass().getResourceAsStream(path));

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

        String         path      = "(Z)-2-(2-Furyl)-3-(5-nitro-2-furyl)acrylamide.xml";
        IAtomContainer container = CMLLoader.loadCML(getClass().getResourceAsStream(path));

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

        String         path      = "implicitPseudoCentre.xml";
        IAtomContainer container = CMLLoader.loadCML(getClass().getResourceAsStream(path));

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

        String         path      = "explicitPseudoCentre.xml";
        IAtomContainer container = CMLLoader.loadCML(getClass().getResourceAsStream(path));

        assertNotNull("molecule was not loaded", container);

        perceptor.perceive(container);

        assertEquals("Expected S conformation",
                     Tetrahedral.S, container.getAtom(0).getProperty("descriptor"));


    }


    @Test public void testVomifoliol(){

        String         path      = "(6R)-vomifoliol.xml";
        IAtomContainer container = CMLLoader.loadCML(getClass().getResourceAsStream(path));

        assertNotNull("molecule was not loaded", container);

        perceptor.perceive(container);

        assertEquals("expected R conformation",
                     Tetrahedral.R, container.getAtom(12).getProperty("descriptor"));


    }

}
