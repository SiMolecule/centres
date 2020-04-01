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

package com.simolecule.centres.rules;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Descriptor;
import com.simolecule.centres.Edge;

/**
 * <b>Sequence Rule 4a</b>
 * <i>Chiral stereogenic units precede pseudoasymmetric stereogenic
 *    units and these precede nonstereogenic units.</i>
 *
 * @param <A> generic atom class
 */
public final class Rule4a<A, B> extends SequenceRule<A, B> {

  public Rule4a(BaseMol<A, B> mol)
  {
    super(mol);
  }

  private static int ord(Descriptor lab) {
    if (lab == null)
      return 0;
    switch (lab) {
      case Unknown:
      case ns:
        return 0;
      case r:
      case s:
      case m:
      case p:
      case E:
      case Z:
        return 1;
      case R:
      case S:
      case M:
      case P:
      case seqTrans:
      case seqCis:
        return 2;
      default:
        throw new IllegalArgumentException("New label?");
    }
  }

  @Override
  public int compare(Edge<A, B> a, Edge<A, B> b)
  {
    int aOrdinal = ord(getBondLabel(a));
    int bOrdinal = ord(getBondLabel(b));
    int cmp = Integer.compare(aOrdinal, bOrdinal);
    if (cmp != 0) return cmp;
    aOrdinal = ord(a.getEnd().getAux());
    bOrdinal = ord(b.getEnd().getAux());
    return Integer.compare(aOrdinal, bOrdinal);
  }
}
