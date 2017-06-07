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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.simolecule.centres.rules;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Edge;

public final class Rule1a<A, B> extends SequenceRule<A, B> {

  private BaseMol<A, B> mol;

  public Rule1a(BaseMol<A, B> mol)
  {
    super(mol);
    this.mol = mol;
  }

  @Override
  public int compare(Edge<A, B> a, Edge<A, B> b)
  {
    int atomNum1 = mol.getAtomicNum(a.getEnd().getAtom());
    int atomNum2 = mol.getAtomicNum(b.getEnd().getAtom());
    if (atomNum1 == 0 || atomNum2 == 0)
      return 0;
    return Integer.compare(atomNum1, atomNum2);
  }
}
