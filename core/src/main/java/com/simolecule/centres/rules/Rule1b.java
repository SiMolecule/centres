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
import com.simolecule.centres.Node;

/**
 * <b>Sequence Rule 1b</b>
 * <i>"A duplicate atom node whose corresponding nonduplicated atom
 * node is the root or is closer to the root ranks higher than
 * a duplicate atom node whose corresponding nonduplicated atom
 * node is farther from the root."</i>
 *
 * @param <A> generic atom class
 */
public class Rule1b<A, B> extends SequenceRule<A, B> {

  /**
   * Flag indicates whether to match the problematic
   * IUPAC 2013 recommendations for Rule 1B.
   */
  private static final boolean IUPAC_2013 = false;

  public Rule1b(BaseMol<A, B> mol) {
    super(mol);
  }

  @Override
  public int compare(Edge<A, B> a, Edge<A, B> b) {
    if (IUPAC_2013) {
      return -Integer.compare(a.getEnd().getDistance(),
                              b.getEnd().getDistance());
    } else {
      if (a.getEnd().isSet(Node.RING_DUPLICATE) &&
          b.getEnd().isSet(Node.RING_DUPLICATE))
        return -Integer.compare(a.getEnd().getDistance(),
                                b.getEnd().getDistance());
      else {
        if (a.getEnd().isSet(Node.RING_DUPLICATE) && !b.getEnd()
                                                       .isSet(Node.RING_DUPLICATE))
          return +1;
        if (!a.getEnd().isSet(Node.RING_DUPLICATE) && b.getEnd()
                                                       .isSet(Node.RING_DUPLICATE))
          return -1;
        return 0;
      }
    }
  }
}
