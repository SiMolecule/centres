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

import com.simolecule.centres.config.Configuration;
import com.simolecule.centres.config.ExtendedCisTrans;
import com.simolecule.centres.config.Octahedral;
import com.simolecule.centres.config.Sp2Bond;
import com.simolecule.centres.config.SquarePlanar;
import com.simolecule.centres.config.Tetrahedral;
import com.simolecule.centres.config.TrigonalBipyramidal;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObject;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.sgroup.Sgroup;
import org.openscience.cdk.sgroup.SgroupType;
import org.openscience.cdk.stereo.Atropisomeric;
import org.openscience.cdk.stereo.DoubleBondStereochemistry;
import org.openscience.cdk.stereo.ExtendedTetrahedral;
import org.openscience.cdk.stereo.TetrahedralChirality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
          configs.add(new com.simolecule.centres.config.Atropisomeric<>(
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
          configs.add(new ExtendedCisTrans<>(bond,
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

    List<Sgroup> sgroups = mol.getProperty(CDKConstants.CTAB_SGROUPS);
    if (sgroups != null && !sgroups.isEmpty()) {
      if (handleSruRepeat(mol, sgroups))
        return;
    }

    new CdkLabeller().label(new CdkMol(mol),
                            createConfigs(mol));
  }

  // Handle molecules with structural repeat units (SRU)
  private static boolean handleSruRepeat(IAtomContainer mol, List<Sgroup> sgroups) {
    Sgroup repeatGroup = null;
    for (Sgroup sgroup : sgroups) {
      if (sgroup.getType() == SgroupType.CtabStructureRepeatUnit &&
          sgroup.getBonds().size() == 2 &&
          sgroup.getAtoms().size() + 2 == mol.getAtomCount()) {
        if (repeatGroup != null) {
          repeatGroup = null;
          break;
        }
        repeatGroup = sgroup;
      } else if (sgroup.getType() == SgroupType.CtabAbbreviation ||
                 sgroup.getType() == SgroupType.CtabMultipleGroup ||
                 sgroup.getType() == SgroupType.CtabData) {
        // display shortcut or data is OK
      } else {
        // something else more complex?
        repeatGroup = null;
        break;
      }
    }

    if (repeatGroup != null) {
      IChemObjectBuilder builder = mol.getBuilder();
      IAtomContainer copy = builder.newAtomContainer();

      Set<IBond> xbonds = repeatGroup.getBonds();
      Set<IAtom> outerAtoms = new HashSet<>();
      List<IAtom> innerAtoms = new ArrayList<>();
      for (IBond bond : xbonds) {
        for (IAtom atom : bond.atoms())
          if (repeatGroup.getAtoms().contains(atom)) {
            innerAtoms.add(atom);
          } else {
            outerAtoms.add(atom);
          }
      }

      List<Map<IChemObject,IChemObject>> remaps = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
        Map<IChemObject, IChemObject> remap = new HashMap<>();
        for (IAtom atom : mol.atoms()) {
          if (outerAtoms.contains(atom))
            continue;
          IAtom atomCopy = builder.newAtom();
          atomCopy.setAtomicNumber(atom.getAtomicNumber());
          atomCopy.setMassNumber(atom.getMassNumber());
          atomCopy.setFormalCharge(atom.getFormalCharge());
          atomCopy.setImplicitHydrogenCount(atom.getImplicitHydrogenCount());
          copy.addAtom(atomCopy);
          remap.put(atom, copy.getAtom(copy.getAtomCount() - 1));
        }
        for (IBond bond : mol.bonds()) {
          if (xbonds.contains(bond))
            continue;
          IBond bondCopy = builder.newBond();
          bondCopy.setAtoms(new IAtom[]{
                  (IAtom) remap.get(bond.getBegin()),
                  (IAtom) remap.get(bond.getEnd())});
          bondCopy.setOrder(bond.getOrder());
          copy.addBond(bondCopy);
          remap.put(bondCopy, copy.getBond(copy.getBondCount() - 1));
        }
        for (IStereoElement se : mol.stereoElements())
          copy.addStereoElement(se.map(remap));

        remaps.add(remap);
        if (remaps.size() > 1) {
          IAtom beg = (IAtom)remaps.get(remaps.size()-2).get(innerAtoms.get(1));
          IAtom end = (IAtom)remaps.get(remaps.size()-1).get(innerAtoms.get(0));
          // always single bond?
          IBond bond = builder.newBond();
          bond.setAtoms(new IAtom[]{beg, end});
          bond.setOrder(IBond.Order.SINGLE);
          copy.addBond(bond);
        }
      }

      IAtom beg = (IAtom)remaps.get(0).get(innerAtoms.get(0));
      IAtom end = (IAtom)remaps.get(remaps.size()-1).get(innerAtoms.get(1));
      IAtom wildcard1 = builder.newAtom();
      wildcard1.setAtomicNumber(0);
      wildcard1.setImplicitHydrogenCount(0);
      copy.addAtom(wildcard1);
      IAtom wildcard2 = builder.newAtom();
      wildcard2.setAtomicNumber(0);
      wildcard2.setImplicitHydrogenCount(0);
      copy.addAtom(wildcard2);
      // always single bond?
      IBond bond = builder.newBond();
      bond.setAtoms(new IAtom[]{beg, wildcard1});
      bond.setOrder(IBond.Order.SINGLE);
      copy.addBond(bond);
      bond.setAtoms(new IAtom[]{end, wildcard2});
      bond.setOrder(IBond.Order.SINGLE);
      copy.addBond(bond);

      new CdkLabeller().label(new CdkMol(copy),
                              createConfigs(copy));

      for (Map.Entry<IChemObject,IChemObject> e : remaps.get(1).entrySet()) {
        e.getKey().setProperty("cip.label", e.getValue().getProperty("cip.label"));
      }
      return true;
    }
    return false;
  }
}
