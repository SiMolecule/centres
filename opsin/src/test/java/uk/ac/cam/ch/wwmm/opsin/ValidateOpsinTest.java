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
      SMILESFragmentBuilder fragbuilder = new SMILESFragmentBuilder(new IDManager());
      Fragment              fragment    = fragbuilder.build(expected.getSmiles());

      BaseMol<Atom,Bond> mol = new OpsinMol(fragment);

      new OpsinLabeler().label(mol, OpsinLabeler.createCfgs(fragment));

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
