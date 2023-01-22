package com.simolecule.centres;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShuffleTest {

  public static final int NUM_TRIALS = 100;

  private static void shuffleBonds(IAtomContainer mol) {
    List<IBond> bondList = Arrays.asList(AtomContainerManipulator.getBondArray(mol));
    Collections.shuffle(bondList);
    mol.setBonds(bondList.toArray(new IBond[0]));
  }

  // we do not have enough information to assign a label here, see
  // GitHub Issue #9
  @Test public void test_alwaysUndefined() throws InvalidSmilesException {
    SmilesParser smilesParser = new SmilesParser(SilentChemObjectBuilder.getInstance());
    IAtomContainer mol = smilesParser.parseSmiles("O[C@H](C(*)C)C(C)O");
    for (int i = 0; i < NUM_TRIALS; i++) {
      shuffleBonds(mol);
      CdkLabeller.label(mol);
      Assert.assertNull(mol.getAtom(1).getProperty("cip.label"));
    }
  }

  @Test public void test_alwaysUndefined_OH() throws InvalidSmilesException {
    SmilesParser smilesParser = new SmilesParser(SilentChemObjectBuilder.getInstance());
    IAtomContainer mol = smilesParser.parseSmiles("O[Co@OH1](C)(C)(C)(*)O");
    for (int i = 0; i < NUM_TRIALS; i++) {
      shuffleBonds(mol);
      CdkLabeller.label(mol);
      Assert.assertNull(mol.getAtom(1).getProperty("cip.label"));
    }
  }

  // similar to the above but there is enough info since we split ties before
  // reaching the *
  @Test public void test_alwaysDefined() throws InvalidSmilesException {
    SmilesParser smilesParser = new SmilesParser(SilentChemObjectBuilder.getInstance());
    IAtomContainer mol = smilesParser.parseSmiles("O[C@H](C(C*)C)C(C)O");
    for (int i = 0; i < NUM_TRIALS; i++) {
      shuffleBonds(mol);
      CdkLabeller.label(mol);
      Assert.assertEquals(Descriptor.R, mol.getAtom(1).getProperty("cip.label"));
    }
  }

  // See GitHub Issue #6
  @Test public void test_consistent() throws InvalidSmilesException {
    SmilesParser smilesParser = new SmilesParser(SilentChemObjectBuilder.getInstance());
    IAtomContainer mol = smilesParser.parseSmiles("O[C@H]1O[C@@]2(O[C@H]1O)O[C@H]([C@H](O2)O)O");
    for (int i = 0; i < NUM_TRIALS; i++) {
      shuffleBonds(mol);
      CdkLabeller.label(mol);
      Assert.assertEquals(Descriptor.S, mol.getAtom(1).getProperty("cip.label"));
      Assert.assertEquals(Descriptor.R, mol.getAtom(3).getProperty("cip.label"));
      Assert.assertEquals(Descriptor.R, mol.getAtom(5).getProperty("cip.label"));
      Assert.assertEquals(Descriptor.R, mol.getAtom(8).getProperty("cip.label"));
      Assert.assertEquals(Descriptor.S, mol.getAtom(9).getProperty("cip.label"));
    }
  }
}
