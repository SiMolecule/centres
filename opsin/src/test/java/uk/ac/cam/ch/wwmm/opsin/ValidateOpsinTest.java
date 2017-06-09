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
