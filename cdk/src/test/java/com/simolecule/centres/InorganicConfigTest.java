/*
 * Copyright (c) 2026 John Mayfield
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

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * Currently these are omitted from the regression set as often a toolkit will
 * not handle inorganic configurations.
 */
public class InorganicConfigTest {

  static String getConfig(String smi) {
    IChemObjectBuilder bldr = SilentChemObjectBuilder.getInstance();
    SmilesParser smipar = new SmilesParser(bldr);
    IAtomContainer mol;
    try {
      mol = smipar.parseSmiles(smi);
    } catch (InvalidSmilesException e) {
      return null;
    }
    CdkLabeller.label(mol);
    StringBuilder configs = new StringBuilder();
    for (IAtom atom : mol.atoms()) {
      Object property = atom.getProperty("conf.index");
      if (property != null) {
        configs.append(property);
      }
    }
    return configs.toString();
  }

  static void assertConfig(String smi, String config) {
    Assert.assertEquals(config, getConfig(smi));
  }

  @Test
  public void testSimpleSP() {
    assertConfig("[Pt@SP1](Cl)(Cl)([NH3])[NH3]", "SP-4-2");
    assertConfig("[Pt@SP2](Cl)(Cl)([NH3])[NH3]", "SP-4-1");
    assertConfig("[Pt@SP3](Cl)(Cl)([NH3])[NH3]", "SP-4-2");
  }

  @Test
  public void testSimpleOCasSP() {
    assertConfig("[Pt@OH25](Cl)(Cl)([NH3])[NH3]", "SP-4-2");
    assertConfig("[Pt@OH27](Cl)(Cl)([NH3])[NH3]", "SP-4-1");
  }

  @Test
  public void testTiedSP() {
    assertConfig("[Pt@SP1](Cl)(Cl)([N]1=CC=CC=C1)[N]#CC", "SP-4-3");
    assertConfig("[Pt@SP2](Cl)(Cl)([N]1=CC=CC=C1)[N]#CC", "SP-4-1");
    assertConfig("[Pt@SP3](Cl)(Cl)([N]1=CC=CC=C1)[N]#CC", "SP-4-2");
  }

  @Test
  public void testBisBidentateSP() {
    assertConfig("[Ru@SP1]1(OCCO1)1(OCCO1)", "SP-4-1′");
    assertConfig("[Ru@SP2]1(OCCO1)1(OCCO1)", "SP-4-1");
    assertConfig("[Ru@SP3]1(OCCO1)1(OCCO1)", "SP-4-1′");
  }

  @Test
  public void testBisBidentateOC() {
    assertConfig("[Mo@OH1](C)(1SCCO1)(1SCCO1)[H]", "OC-6-1′2′-C");
    assertConfig("[Mo@OH2](C)(1SCCO1)(1SCCO1)[H]", "OC-6-1′2′-A");
  }

  @Test
  public void testBisBidentateTBPY() {
    assertConfig("[Mo@TB1](C)(1SCCO1)1SCCO1", "TBPY-5-1′3-A");
    assertConfig("[Mo@TB2](C)(1SCCO1)1SCCO1", "TBPY-5-1′3-C");
  }

  @Test
  public void testTrisBidentateOC() {
    assertConfig("[W@]123(OCCO1)(OCCO2)OCCO3 Δ", "OC-6-1ʺ1′-A");
    assertConfig("O=C1O[Fe@OH12-3]23(OC(C(O3)=O)=O)(OC(C(O2)=O)=O)OC1=O Δ", "OC-6-1ʺ1′-A");
    assertConfig("Cl[Co@OH29+]12([NH2]CC[NH2]2)([NH2]CC[NH2]1)Cl Δ-cis", "OC-6-2′2-A");

    assertConfig("[W@@]123(OCCO1)(OCCO2)OCCO3 Λ", "OC-6-1ʺ1′-C");
    assertConfig("O=C1O[Fe@OH13-3]23(OC(C(O3)=O)=O)(OC(C(O2)=O)=O)OC1=O Λ", "OC-6-1ʺ1′-C");
    assertConfig("Cl[Co@OH26+]12([NH2]CC[NH2]2)([NH2]CC[NH2]1)Cl Λ-cis", "OC-6-2′2-C");
  }
}
