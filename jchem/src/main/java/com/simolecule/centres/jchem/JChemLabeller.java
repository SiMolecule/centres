package com.simolecule.centres.jchem;

import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.struc.Molecule;
import chemaxon.struc.StereoConstants;
import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Labeller;
import com.simolecule.centres.config.Configuration;
import com.simolecule.centres.config.Sp2Bond;
import com.simolecule.centres.config.Tetrahedral;

import java.util.ArrayList;
import java.util.List;

public class JChemLabeller extends Labeller<MolAtom, MolBond> {

  private static List<Configuration<MolAtom, MolBond>> findConfigs(
          Molecule mol) {
    List<Configuration<MolAtom, MolBond>> configs = new ArrayList<>();
    for (int i = 0; i < mol.getAtomCount(); i++) {
      switch (mol.getParityType(i)) {
        case StereoConstants.PARITY_TETRAHEDRAL:
          MolAtom focus = mol.getAtom(i);
          MolAtom[] ligands = focus.getLigands();
          if (ligands.length != 4) {
            System.err.println("Please provided hydrogen expanded graph for JChem");
            continue;
//            not correct
//            ligands = Arrays.copyOf(ligands, 4);
//            ligands[3] = focus;
//            int sign = MolAtom.paritySign(mol.indexOf(ligands[0]),
//                                          mol.indexOf(ligands[1]),
//                                          mol.indexOf(ligands[2]),
//                                          mol.indexOf(ligands[3]));
          }

          configs.add(new Tetrahedral<MolAtom, MolBond>(focus,
                                                        ligands,
                                                        mol.getParity(i)));
          break;
      }
    }
    for (int i = 0; i < mol.getBondCount(); i++) {
      MolBond bond = mol.getBond(i);
      MolAtom a1   = bond.getCTAtom1();
      MolAtom a4   = bond.getCTAtom4();
      if (a1 != null && a4 != null) {
        int cfg = 0;
        switch (bond.getFlags() & StereoConstants.CTUMASK) {
          case StereoConstants.CIS:
            cfg = 2;
            break;
          case StereoConstants.TRANS:
            cfg = 1;
            break;
        }
        if (cfg != 0) {
          configs.add(new Sp2Bond<>(bond,
                                    new MolAtom[]{bond.getAtom1(),
                                                  bond.getAtom2()},
                                    new MolAtom[]{
                                            a1, a4
                                    },
                                    cfg));
        }
      }
    }
    return configs;
  }

  public static void label(BaseMol<MolAtom, MolBond> mol) {
    new JChemLabeller().label(mol, findConfigs(((Molecule) mol.getBaseImpl())));
  }
}
