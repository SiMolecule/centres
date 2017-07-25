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
import com.simolecule.centres.Mancude;

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
    final int anum = a.getEnd().getAtomicNumNumerator();
    final int aden = a.getEnd().getAtomicNumDenominator();
    final int bnum = b.getEnd().getAtomicNumNumerator();
    final int bden = b.getEnd().getAtomicNumDenominator();
    if (anum == 0 || bnum == 0)
      return 0;
    if (aden == 1 && bden == 1)
      return Integer.compare(anum, bnum);
    return Mancude.Fraction.compare(anum, aden, bnum, bden);
  }
}
