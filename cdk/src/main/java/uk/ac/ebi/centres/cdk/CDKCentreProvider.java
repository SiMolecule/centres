/*
 * Copyright (c) 2012. John May
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package uk.ac.ebi.centres.cdk;

import org.openscience.cdk.graph.SpanningTree;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IDoubleBondStereochemistry;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.interfaces.ITetrahedralChirality;
import uk.ac.ebi.centres.Centre;
import uk.ac.ebi.centres.ConnectionTable;
import uk.ac.ebi.centres.DescriptorManager;
import uk.ac.ebi.centres.graph.ConnectionTableDigraph;
import uk.ac.ebi.centres.ligand.CisTrans;
import uk.ac.ebi.centres.ligand.Tetrahedral;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author John May
 */
public class CDKCentreProvider {

  private final IAtomContainer         container;
  private final ConnectionTable<IAtom> table;
  private       IAtomContainer         cyclicFragments;


  public CDKCentreProvider(IAtomContainer container)
  {
    this.container = container;
    this.table = new CDKConnectionTable(container);
  }

  public Collection<Centre<IAtom>> getCentres(DescriptorManager<IAtom> manager)
  {

    List<Centre<IAtom>> centres = new ArrayList<Centre<IAtom>>(container.getAtomCount());

    for (IStereoElement se : container.stereoElements()) {
      if (se instanceof ITetrahedralChirality) {
        IAtom   focus    = ((ITetrahedralChirality) se).getChiralAtom();
        IAtom[] carriers = ((ITetrahedralChirality) se).getLigands();
        int     config   = ((ITetrahedralChirality) se).getStereo() == ITetrahedralChirality.Stereo.ANTI_CLOCKWISE ? 1 : 2;

        Tetrahedral<IAtom> centre = new Tetrahedral<IAtom>(manager.getDescriptor(focus),
                                                           focus,
                                                           carriers,
                                                           config);
        centre.setProvider(new ConnectionTableDigraph<IAtom>(centre, manager, table));
        centres.add(centre);
      } else if (se instanceof IDoubleBondStereochemistry) {
        IBond bond = ((IDoubleBondStereochemistry) se).getStereoBond();
        IAtom[] carriers = new IAtom[2];
        carriers[0] = ((IDoubleBondStereochemistry) se).getBonds()[0].getOther(bond.getBegin());
        carriers[1] = ((IDoubleBondStereochemistry) se).getBonds()[1].getOther(bond.getEnd());
        int cfg = ((IDoubleBondStereochemistry) se).getStereo() == IDoubleBondStereochemistry.Conformation.TOGETHER ?
                CisTrans.TOGETHER  : CisTrans.OPPOSITE;
        CisTrans<IAtom> centre = new CisTrans<IAtom>(bond.getBegin(), bond.getEnd(),
                                                     carriers,
                                                     cfg,
                                                     manager.getDescriptor(bond.getBegin(),
                                                                           bond.getEnd()));
        centre.setProvider(new ConnectionTableDigraph<IAtom>(centre, manager, table));
        centres.add(centre);
      }
    }

    return centres;

  }

  /**
   * stops tandem double bonds being provided
   * C=C=C
   * \
   * <p>
   * being provided. see. unit test of 2-iminoethen-1-ol (testIminoethenol)
   *
   * @param bond
   * @param container
   * @return
   */
  private boolean onlyConnectedToSingleBonds(IBond bond, IAtomContainer container)
  {
    return onlyConnectedToSingleBonds(bond, bond.getAtom(0), container)
           && onlyConnectedToSingleBonds(bond, bond.getAtom(1), container);
  }

  private boolean onlyConnectedToSingleBonds(IBond bond, IAtom atom, IAtomContainer container)
  {
    for (IBond connected : container.getConnectedBondsList(atom))
      if (!IBond.Order.SINGLE.equals(connected.getOrder()) && !connected.equals(bond))
        return Boolean.FALSE;
    return Boolean.TRUE;
  }


  private IAtomContainer getCyclicFragments()
  {
    if (cyclicFragments == null) {
      cyclicFragments = new SpanningTree(container).getCyclicFragmentsContainer();
    }
    return cyclicFragments;
  }


  private boolean hasVariableBond(IAtomContainer container, IAtom atom)
  {
    for (IBond bond : container.getConnectedBondsList(atom)) {
      IBond.Stereo stereo = bond.getStereo();
      if (IBond.Stereo.UP_OR_DOWN.equals(stereo)
          || IBond.Stereo.UP_OR_DOWN_INVERTED.equals(stereo)) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }


  private boolean hasStereoBonds(IAtomContainer container, IAtom atom)
  {
    for (IBond bond : container.getConnectedBondsList(atom)) {
      IBond.Stereo stereo = bond.getStereo();
      if (IBond.Stereo.UP.equals(stereo)
          || IBond.Stereo.DOWN.equals(stereo)) {
        if (bond.getAtom(0) == atom)
          return true;
      }
    }
    return false;
  }


}
