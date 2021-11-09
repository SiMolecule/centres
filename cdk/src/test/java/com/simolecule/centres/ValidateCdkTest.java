/*
 * Copyright (c) 2020 John Mayfield
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

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
      String configIdx = atom.getProperty(BaseMol.CONF_INDEX);
      if (configIdx != null)
        atom.setProperty(CDKConstants.COMMENT, configIdx);
      else if (descriptor != null && descriptor != Descriptor.Unknown)
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
