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
import com.simolecule.centres.Descriptor;
import com.simolecule.centres.Edge;

/**
 * <b>Sequence Rule 5</b>
 * <i>An atom or group with descriptor 'R','M' and 'seqCis'
 * has priority over its enantiomorph 'S', 'P' or 'seqTrans'.
 * </i>
 * @param <A> generic atom class
 */
public final class Rule5<A, B> extends SequenceRule<A, B> {

  public Rule5(BaseMol<A, B> mol)
  {
    super(mol);
  }

  private static int ord(Descriptor lab) {
    if (lab == null)
      return 0;
    switch (lab) {
      case R:
      case M:
      case seqCis:
        return 2;
      case S:
      case P:
      case seqTrans:
        return 1;
      default:
        return 0;
    }
  }

  @Override
  public boolean isPseudoAsymmetric()
  {
    return true;
  }

  @Override
  public int compare(Edge<A, B> a, Edge<A, B> b)
  {
    int aOrdinal = ord(getBondLabel(a));
    int bOrdinal = ord(getBondLabel(b));
    int cmp = Integer.compare(aOrdinal, bOrdinal);
    if (cmp != 0) return cmp;
    aOrdinal = ord(getAtomLabel(a.getEnd()));
    bOrdinal = ord(getAtomLabel(b.getEnd()));
    return Integer.compare(aOrdinal, bOrdinal);
  }
}
