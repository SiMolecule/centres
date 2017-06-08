package com.simolecule.centres;

import com.simolecule.centres.config.Configuration;
import com.simolecule.centres.config.Sp2Bond;
import com.simolecule.centres.config.Tetrahedral;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.stereo.Atropisomeric;
import org.openscience.cdk.stereo.DoubleBondStereochemistry;
import org.openscience.cdk.stereo.ExtendedTetrahedral;
import org.openscience.cdk.stereo.TetrahedralChirality;

import java.util.ArrayList;
import java.util.List;

public final class CdkLabeller extends Labeller<IAtom, IBond> {

  CdkLabeller()
  {
  }

  static List<Configuration<IAtom, IBond>> createConfigs(IAtomContainer mol)
  {
    List<Configuration<IAtom, IBond>> configs = new ArrayList<>();

    for (IStereoElement se : mol.stereoElements()) {
      switch (se.getConfigClass()) {
        case IStereoElement.TH: {
          TetrahedralChirality thSe = (TetrahedralChirality) se;
          configs.add(new Tetrahedral<IAtom, IBond>(thSe.getFocus(),
                                                    thSe.getCarriers().toArray(new IAtom[4]),
                                                    thSe.getConfig()));
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
                                    dbSe.getConfig()));
        }
        break;
        case IStereoElement.AT: {
          Atropisomeric atSe  = (Atropisomeric) se;
          IBond         bond  = atSe.getFocus();
          configs.add(new com.simolecule.centres.config.Atropisomeric<IAtom, IBond>(
                  bond,
                  new IAtom[]{bond.getBegin(), bond.getEnd()},
                  atSe.getCarriers().toArray(new IAtom[4]),
                  atSe.getConfig()));
        }
        break;
        case IStereoElement.AL: {
          ExtendedTetrahedral etSe     = (ExtendedTetrahedral) se;
          IAtom               middle   = etSe.getFocus();
          IAtom[]             endAtoms = ExtendedTetrahedral.findTerminalAtoms(mol, middle);
          IAtom[]             focus    = new IAtom[]{middle, endAtoms[0], endAtoms[1]};
          IAtom[]             carriers = etSe.getCarriers().toArray(new IAtom[4]);
          configs.add(new com.simolecule.centres.config.ExtendedTetrahedral<IAtom, IBond>(focus,
                                                                                          carriers,
                                                                                          etSe.getConfig()));
        }
        break;
      }
    }
    return configs;
  }

  public static void label(IAtomContainer mol)
  {
    new CdkLabeller().label(new CdkMol(mol), createConfigs(mol));
  }
}
