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
import com.simolecule.centres.Digraph;
import com.simolecule.centres.Edge;

/**
 * <b>Sequence Rule 6 (proposed)</b>
 * @param <A> generic atom class
 */
public final class Rule6<A, B> extends SequenceRule<A, B> {

  public Rule6(BaseMol<A, B> mol)
  {
    super(mol);
  }

  @Override
  public boolean isPseudoAsymmetric()
  {
    return true; // comes after Rule 5 so must be true
  }

  @Override
  public int compare(Edge<A, B> a, Edge<A, B> b)
  {
    Digraph<A,B> digraph = a.getBeg().getDigraph();
    A ref = digraph.getRule6Ref();
    if (ref == null)
      return 0;
    A aAtom = a.getEnd().getAtom();
    A bAtom = b.getEnd().getAtom();
    if (ref.equals(aAtom) && !ref.equals(bAtom))
      return +1; // a is ref (has priority)
    else if (!ref.equals(aAtom) && ref.equals(bAtom))
      return -1; // b is ref (has priority)
    return 0;
  }
}
