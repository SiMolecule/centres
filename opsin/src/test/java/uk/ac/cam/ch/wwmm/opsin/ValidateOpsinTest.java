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

package uk.ac.cam.ch.wwmm.opsin;

import centres.AbstractValidationSuite;
import com.simolecule.centres.BaseMol;
import org.junit.Assert;
import org.junit.Test;

public class ValidateOpsinTest extends AbstractValidationSuite {
  @Test
  public void testAssignment() throws Exception
  {
    try {
      IDManager             idManager   = new IDManager();
      SMILESFragmentBuilder fragbuilder = new SMILESFragmentBuilder(idManager);
      FragmentManager       manager     = new FragmentManager(fragbuilder, idManager);
      Fragment              fragment    = manager.buildSMILES(expected.getSmiles());
      manager.makeHydrogensExplicit();

      BaseMol<Atom, Bond> mol = new OpsinMol(fragment);

      new OpsinLabeller().label(mol, OpsinLabeller.createCfgs(fragment));

      check(mol,
            new GenSmiles() {
              @Override
              public String generate(BaseMol mol)
              {
                try {
                  return SMILESWriter.generateSmiles(((OpsinMol) mol).getBaseImpl());
                } catch (Exception ex) {
                  return "ERROR: " + ex.getMessage();
                }
              }
            });
      //System.out.println(expected.getSmiles());
    } catch (StructureBuildingException ex) {
      Assert.fail("Could not parse SMILES: " + expected.getSmiles());
    }
  }



}
