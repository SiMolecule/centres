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
