package com.simolecule.centres;

import centres.AbstractValidationSuite;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import uk.ac.ebi.centres.Descriptor;
import uk.ac.ebi.centres.cdk.CDKManager;
import uk.ac.ebi.centres.cdk.CDKPerceptor;

import java.util.HashSet;
import java.util.Set;

public class ValidateCdkTest extends AbstractValidationSuite {

  private static final SmilesParser smigen = new SmilesParser(SilentChemObjectBuilder.getInstance());

  @Test
  public void testAssignment() throws Exception
  {
    IAtomContainer mol       = smigen.parseSmiles(expected.getSmiles());
    CDKPerceptor   perceptor = new CDKPerceptor();

    perceptor.perceive(mol);

    Set<IChemObject> checked = new HashSet<>();

    int atomIter = 0;
    int bondIter = 0;

    for (CipLabel label : expected.getLabels()) {
      switch (label.getCtx()) {
        case Atom:

          if (label.getIdx() < 0) {
            while (atomIter < mol.getAtomCount()) {
              IAtom      atom   = mol.getAtom(atomIter);
              Descriptor actual = atom.getProperty(CDKManager.PROP_KEY);
              if (actual != null &&
                  actual != Descriptor.Unknown) {
                checked.add(atom);
                Assert.assertThat("Atom idx=" + atomIter + " expected=" + label.getExp() + " was=" + actual +
                                  "\n" + toSmiles(mol),
                                  actual, CoreMatchers.is(label.getExp()));
                atomIter++;
                break;
              }
              atomIter++;
            }
            Assert.assertTrue("Label not found, expected " + label.getExp() + "\n" + toSmiles(mol),
                       atomIter < mol.getAtomCount());
          } else {
            IAtom atom = mol.getAtom(label.getIdx());
            checked.add(atom);
            Assert.assertNotNull("No atom at index " + label.getIdx(), atom);
            Descriptor actual = atom.getProperty(CDKManager.PROP_KEY);
            Assert.assertThat("Atom idx=" + label.getIdx() + " expected=" + label.getExp() + " was=" + actual +
                              "\n" + toSmiles(mol),
                                     actual,
                                     CoreMatchers.is(label.getExp()));
          }
          break;
        case Bond:

          if (label.getIdx() < 0) {
            while (bondIter < mol.getBondCount()) {
              IBond bond = mol.getBond(bondIter);
              checked.add(bond);
              Descriptor actual = bond.getProperty(CDKManager.PROP_KEY);
              if (actual != null &&
                  actual != Descriptor.Unknown) {
                Assert.assertThat("Bond idx=" + bondIter + " expected=" + label.getExp() + " was=" + actual +
                                  "\n" + toSmiles(mol),
                                         actual, CoreMatchers.is(label.getExp()));
                bondIter++;
                break;
              }
              bondIter++;
            }
            Assert.assertTrue("Label not found, expected " + label.getExp() + "\n" + toSmiles(mol),
                       bondIter < mol.getBondCount());
          } else {
            IBond bond = mol.getBond(label.getIdx());
            checked.add(bond);
            Assert.assertNotNull("No atom at index " + label.getIdx(), bond);
            Descriptor actual = bond.getProperty(CDKManager.PROP_KEY);
            Assert.assertThat("Bond idx=" + label.getIdx() + " expected=" + label.getExp() + " was=" + actual +
                              "\n" + toSmiles(mol),
                                     actual,
                                     CoreMatchers.is(label.getExp()));
          }
          break;

      }
    }

    for (IAtom atom : mol.atoms()) {
      if (checked.contains(atom))
        continue;
      Descriptor desc = atom.getProperty(CDKManager.PROP_KEY);
      if (desc != null && desc != Descriptor.Unknown)
        Assert.fail("No expected value for Atom idx=" + mol.indexOf(atom) + " was=" + desc + "\n" + toSmiles(mol));
    }
    for (IBond bond : mol.bonds()) {
      if (checked.contains(bond))
        continue;
      Descriptor desc = bond.getProperty(CDKManager.PROP_KEY);
      if (desc != null && desc != Descriptor.Unknown)
        Assert.fail("No expected value for Bond idx=" + mol.indexOf(bond) + " was=" + desc + "\n" + toSmiles(mol));
    }

    if (expected.getLabels().isEmpty()) {
      System.err.println(toSmiles(mol));
    }
  }

  private String toSmiles(IAtomContainer mol) throws CDKException
  {
    for (IAtom atom : mol.atoms()) {
      Descriptor descriptor = atom.getProperty(CDKManager.PROP_KEY);
      if (descriptor != null && descriptor != Descriptor.Unknown)
        atom.setProperty(CDKConstants.COMMENT, descriptor);
    }
    for (IBond bond : mol.bonds()) {
      Descriptor descriptor = bond.getProperty(CDKManager.PROP_KEY);
      if (descriptor != null && descriptor != Descriptor.Unknown)
        bond.getBegin().setProperty(CDKConstants.COMMENT, descriptor);
    }
    return new SmilesGenerator(SmiFlavor.CxAtomValue | SmiFlavor.Isomeric).create(mol);
  }
}
