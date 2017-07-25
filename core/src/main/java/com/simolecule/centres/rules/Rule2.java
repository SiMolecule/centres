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

package com.simolecule.centres.rules;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Edge;
import com.simolecule.centres.Isotope;

/**
 * <b>Sequence Rule 1b</b>
 * <i>"A duplicate atom node whose corresponding nonduplicated atom
 * node is the root or is closer to the root ranks higher than
 * a duplicate atom node whose corresponding nonduplicated atom
 * node is farther from the root."</i>
 *
 * @param <A> generic atom class
 */
public class Rule2<A, B> extends SequenceRule<A, B> {

  private final BaseMol<A, B> mol;

  public Rule2(BaseMol<A, B> mol)
  {
    super(mol);
    this.mol = mol;
  }

  @Override
  public int compare(Edge<A, B> a, Edge<A, B> b)
  {
    int aAtomNum = mol.getAtomicNum(a.getEnd().getAtom());
    int bAtomNum = mol.getAtomicNum(b.getEnd().getAtom());
    if (aAtomNum == 0 || bAtomNum == 0)
      return 0;
    int aMassNum = a.getEnd().isDuplicate() ? 0 : mol.getMassNum(a.getEnd().getAtom());
    int bMassNum = b.getEnd().isDuplicate() ? 0 : mol.getMassNum(b.getEnd().getAtom());
    if (aMassNum == 0 && bMassNum == 0)
      return 0;
    Isotope aiso = Isotope.find(aAtomNum, aMassNum);
    Isotope biso = Isotope.find(bAtomNum, bMassNum);

    double aweight, bweight;
    if (aiso == null)
      aweight = aMassNum;
    else
      aweight = aiso.getWeight();
    if (biso == null)
      bweight = bMassNum;
    else
      bweight = biso.getWeight();

    return Double.compare(aweight, bweight);
  }
}
