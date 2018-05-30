package com.simolecule.centres;

import com.simolecule.centres.config.Configuration;
import com.simolecule.centres.config.ExtendedCisTrans;
import com.simolecule.centres.config.Octahedral;
import com.simolecule.centres.config.Sp2Bond;
import com.simolecule.centres.config.SquarePlanar;
import com.simolecule.centres.config.Tetrahedral;
import com.simolecule.centres.config.TrigonalBipyramidal;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.stereo.Atropisomeric;
import org.openscience.cdk.stereo.DoubleBondStereochemistry;
import org.openscience.cdk.stereo.ExtendedTetrahedral;
import org.openscience.cdk.stereo.TetrahedralChirality;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CdkLabeller extends Labeller<IAtom, IBond> {

  CdkLabeller() {
  }

  static List<Configuration<IAtom, IBond>> createConfigs(IAtomContainer mol) {
    List<Configuration<IAtom, IBond>> configs = new ArrayList<>();

    for (IStereoElement se : mol.stereoElements()) {
      switch (se.getConfigClass()) {
        case IStereoElement.TH: {
          TetrahedralChirality thSe = (TetrahedralChirality) se;
          configs.add(new Tetrahedral<IAtom, IBond>(thSe.getFocus(),
                                                    thSe.getCarriers()
                                                        .toArray(new IAtom[4]),
                                                    thSe.getConfigOrder()));
        }
        break;
        case IStereoElement.CT: {
          DoubleBondStereochemistry dbSe  = (DoubleBondStereochemistry) se;
          IBond                     bond  = dbSe.getFocus();
          IBond[]                   bonds = dbSe.getBonds();
          configs.add(new Sp2Bond<>(bond,
                                    new IAtom[]{bond.getBegin(), bond.getEnd()},
                                    new IAtom[]{bonds[0].getOther(bond.getBegin()),
                                                bonds[1].getOther(bond.getEnd())},
                                    dbSe.getConfigOrder()));
        }
        break;
        case IStereoElement.AT: {
          Atropisomeric atSe = (Atropisomeric) se;
          IBond         bond = atSe.getFocus();
          configs.add(new com.simolecule.centres.config.Atropisomeric<IAtom, IBond>(
                  bond,
                  new IAtom[]{bond.getBegin(), bond.getEnd()},
                  atSe.getCarriers().toArray(new IAtom[4]),
                  atSe.getConfigOrder()));
        }
        break;
        case IStereoElement.AL: {
          ExtendedTetrahedral etSe     = (ExtendedTetrahedral) se;
          IAtom               middle   = etSe.getFocus();
          IAtom[]             endAtoms = ExtendedTetrahedral.findTerminalAtoms(mol, middle);
          IAtom[]             focus    = new IAtom[]{middle, endAtoms[0], endAtoms[1]};
          IAtom[]             carriers = etSe.getCarriers()
                                             .toArray(new IAtom[4]);
          configs.add(new com.simolecule.centres.config.ExtendedTetrahedral<IAtom, IBond>(focus,
                                                                                          carriers,
                                                                                          etSe.getConfigOrder()));
        }
        break;
        case IStereoElement.CU: {
          org.openscience.cdk.stereo.ExtendedCisTrans ectElem;
          ectElem = (org.openscience.cdk.stereo.ExtendedCisTrans) se;
          IBond   bond  = ectElem.getFocus();
          IBond[] bonds = ectElem.getCarriers().toArray(new IBond[2]);
          IAtom[] ends  = org.openscience.cdk.stereo.ExtendedCisTrans.findTerminalAtoms(mol, bond);
          configs.add(new ExtendedCisTrans<IAtom, IBond>(bond,
                                                         new IAtom[]{ends[0], ends[1]},
                                                         new IAtom[]{bonds[0].getOther(ends[0]),
                                                                     bonds[1].getOther(ends[1])},
                                                         ectElem.getConfigOrder()));
        }
        break;
        case IStereoElement.OC: {
          org.openscience.cdk.stereo.Octahedral ocSe
                  = ((org.openscience.cdk.stereo.Octahedral) se).normalize();
          configs.add(new Octahedral<IAtom, IBond>(ocSe.getFocus(),
                                                   ocSe.getCarriers()
                                                       .toArray(new IAtom[6])));
        }
        break;
        case IStereoElement.SP: {
          org.openscience.cdk.stereo.SquarePlanar spSe
                  = ((org.openscience.cdk.stereo.SquarePlanar) se).normalize();
          configs.add(new SquarePlanar<IAtom, IBond>(spSe.getFocus(),
                                                     spSe.getCarriers()
                                                         .toArray(new IAtom[4])));
        }
        break;
        case IStereoElement.TBPY: {
          org.openscience.cdk.stereo.TrigonalBipyramidal tbpySe
                  = ((org.openscience.cdk.stereo.TrigonalBipyramidal) se).normalize();
          configs.add(new TrigonalBipyramidal<IAtom, IBond>(tbpySe.getFocus(),
                                                            tbpySe.getCarriers()
                                                                  .toArray(new IAtom[4])));
        }
        break;
      }
    }
    return configs;
  }

  public static void label(IAtomContainer mol) {
    new CdkLabeller().label(new CdkMol(mol), createConfigs(mol));
  }
}
