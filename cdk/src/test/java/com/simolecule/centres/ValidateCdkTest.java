package com.simolecule.centres;

import centres.AbstractValidationSuite;
import org.junit.Test;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

public class ValidateCdkTest extends AbstractValidationSuite {

  private static final SmilesParser smigen = new SmilesParser(SilentChemObjectBuilder.getInstance());

  @Test
  public void testAssignment() throws Exception
  {
    IAtomContainer base = smigen.parseSmiles(expected.getSmiles());
    CdkMol         mol  = new CdkMol(base);
    new CdkLabeller().label(mol, CdkLabeller.createConfigs(base));
    check(mol, new GenSmiles() {
      @Override
      public String generate(BaseMol mol)
      {
        try {
          return toSmiles((IAtomContainer) mol.getBaseImpl());
        } catch (CDKException e) {
          return "ERROR: " + e.getMessage();
        }
      }
    });
  }

  private String toSmiles(IAtomContainer mol) throws CDKException
  {
    for (IAtom atom : mol.atoms()) {
      Descriptor descriptor = atom.getProperty(BaseMol.CIP_LABEL_KEY);
      if (descriptor != null && descriptor != Descriptor.Unknown)
        atom.setProperty(CDKConstants.COMMENT, descriptor);
    }
    for (IBond bond : mol.bonds()) {
      Descriptor descriptor = bond.getProperty(BaseMol.CIP_LABEL_KEY);
      if (descriptor != null && descriptor != Descriptor.Unknown)
        bond.getBegin().setProperty(CDKConstants.COMMENT, descriptor);
    }
    return new SmilesGenerator(SmiFlavor.CxAtomValue | SmiFlavor.Isomeric).create(mol);
  }
}
