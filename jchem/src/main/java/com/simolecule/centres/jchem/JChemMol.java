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

package com.simolecule.centres.jchem;

import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.struc.Molecule;
import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Digraph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JChemMol extends BaseMol<MolAtom,MolBond> {

  private Molecule mol;
  private Set<MolBond> rbonds = null;

  public JChemMol(Molecule mol) {
    this.mol = mol;
  }

  @Override
  public Molecule getBaseImpl() {
    return mol;
  }

  @Override
  public int getNumAtoms() {
    return mol.getAtomCount();
  }

  @Override
  public int getNumBonds() {
    return mol.getBondCount();
  }

  @Override
  public MolAtom getAtom(int idx) {
    return mol.getAtom(idx);
  }

  @Override
  public int getAtomIdx(MolAtom atom) {
    return mol.indexOf(atom);
  }

  @Override
  public MolBond getBond(int idx) {
    return mol.getBond(idx);
  }

  @Override
  public int getBondIdx(MolBond bond) {
    return mol.indexOf(bond);
  }

  @Override
  public Iterable<MolBond> getBonds(MolAtom atom) {
    return Arrays.asList(atom.getBondArray());
  }

  @Override
  public MolAtom getOther(MolBond bond, MolAtom atom) {
    return bond.getOtherAtom(atom);
  }

  @Override
  public MolAtom getBeg(MolBond bond) {
    return bond.getAtom1();
  }

  @Override
  public MolAtom getEnd(MolBond bond) {
    return bond.getAtom2();
  }

  @Override
  public boolean isInRing(MolBond bond) {
    if (rbonds == null)
      findRingBonds();
    return rbonds.contains(bond);
  }

  private synchronized void findRingBonds() {
    // XXX: There is an O(n) algorithm for this but couldn't int in the API
    final int res[][][] = mol.getAromaticAndAliphaticRings(0, false, false, 18, 1000);
    rbonds = new HashSet<>();
    for (int[][] rset : res)
      for (int[] ring : rset)
        for (int i = 0; i < ring.length; i++)
          rbonds.add(mol.getAtom(ring[i])
                        .getBondTo(mol.getAtom(ring[(i + 1) % ring.length])));
  }

  @Override
  public int getAtomicNum(MolAtom atom) {
    return atom != null ? atom.getAtno() : 1;
  }

  @Override
  public int getNumHydrogens(MolAtom atom) {
    return atom.getImplicitHCount(true);
  }

  @Override
  public int getMassNum(MolAtom atom) {
    return atom != null ? atom.getMassno() : 0;
  }

  @Override
  public int getCharge(MolAtom atom) {
    return atom.getCharge();
  }

  @Override
  public int getBondOrder(MolBond bond) {
    switch (bond.getBondType()) {
      case SINGLE:
        return 1;
      case DOUBLE:
        return 2;
      case TRIPLE:
        return 3;
      case AROMATIC:
        throw new IllegalArgumentException("Structure must be Kekulé!");
      default:
        throw new IllegalArgumentException("Unsupported bond type: " + bond.getBondType());
    }
  }

  @Override
  public void setAtomProp(MolAtom atom, String key, Object val) {
    atom.putProperty(key, val);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getAtomProp(MolAtom atom, String key) {
    return (V) atom.getProperty(key);
  }

  @Override
  public void setBondProp(MolBond bond, String key, Object val) {
    bond.putProperty(key, val);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V> V getBondProp(MolBond bond, String key) {
    return (V) bond.getProperty(key);
  }

  @Override
  public String dumpDigraph(Digraph<MolAtom, MolBond> digraph) {
    throw new UnsupportedOperationException("Not implemented");
  }
}
