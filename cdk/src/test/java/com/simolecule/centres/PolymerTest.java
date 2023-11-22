package com.simolecule.centres;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PolymerTest {

  @Test public void test_basicPolymer() throws InvalidSmilesException {
    SmilesParser smilesParser = new SmilesParser(SilentChemObjectBuilder.getInstance());
    IAtomContainer mol = smilesParser.parseSmiles("*CC[C@H](O)CO*");
    CdkLabeller.label(mol);
    Assert.assertEquals(Descriptor.S, mol.getAtom(3).getProperty("cip.label"));
  }

  @Test public void test_NeedRepeat() throws InvalidSmilesException {
    // not fixable: *C[C@H](O)CO*
    // but this is: *C[C@H](O)COC*
    SmilesParser smilesParser = new SmilesParser(SilentChemObjectBuilder.getInstance());
    IAtomContainer mol = smilesParser.parseSmiles("*C[C@H](O)COC* |Sg:n:1,2,3,4,5,6::ht|");
    CdkLabeller.label(mol);
    Assert.assertEquals(Descriptor.S, mol.getAtom(2).getProperty("cip.label"));
  }
}
