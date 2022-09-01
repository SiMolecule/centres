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
