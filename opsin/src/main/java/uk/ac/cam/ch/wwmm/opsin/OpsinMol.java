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

package uk.ac.cam.ch.wwmm.opsin;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Descriptor;
import com.simolecule.centres.Digraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class OpsinMol extends BaseMol<Atom, Bond> {

  private final Fragment   fragment;
  private final List<Atom> atoms;
  private final List<Bond> bonds;
  private final Map<Object, Map<String, Object>> props = new HashMap<>();

  public OpsinMol(Fragment fragment)
  {
    this.fragment = fragment;
    this.atoms = new ArrayList<>(fragment.getAtomList());
    this.bonds = new ArrayList<>(fragment.getBondSet());
  }

  @Override
  public Fragment getBaseImpl()
  {
    return fragment;
  }

  @Override
  public int getNumAtoms()
  {
    return atoms.size();
  }

  @Override
  public int getNumBonds()
  {
    return bonds.size();
  }

  @Override
  public Atom getAtom(int idx)
  {
    return atoms.get(idx);
  }

  @Override
  public int getAtomIdx(Atom atom)
  {
    return atoms.indexOf(atom);
  }

  @Override
  public Bond getBond(int idx)
  {
    return bonds.get(idx);
  }

  @Override
  public int getBondIdx(Bond bond)
  {
    return bonds.indexOf(bond);
  }

  @Override
  public Iterable<Bond> getBonds(Atom atom)
  {
    return atom.getBonds();
  }

  @Override
  public Atom getOther(Bond bond, Atom atom)
  {
    return bond.getOtherAtom(atom);
  }

  @Override
  public Atom getBeg(Bond bond)
  {
    return bond.getFromAtom();
  }

  @Override
  public Atom getEnd(Bond bond)
  {
    return bond.getToAtom();
  }

  @Override
  public boolean isInRing(Bond bond)
  {
    // TODO check with daniel
    return false;
  }

  @Override
  public int getAtomicNum(Atom atom)
  {
    return atom.getElement().ATOMIC_NUM;
  }

  @Override
  public int getNumHydrogens(Atom atom)
  {
    return StructureBuildingMethods.calculateSubstitutableHydrogenAtoms(atom);
  }

  @Override
  public int getCharge(Atom atom)
  {
    return atom.getCharge();
  }

  @Override
  public int getMassNum(Atom atom)
  {
    Integer isotope = atom.getIsotope();
    return isotope == null ? 0 : isotope;
  }

  @Override
  public int getBondOrder(Bond bond)
  {
    return bond.getOrder();
  }

  @Override
  public void setAtomProp(Atom atom, String key, Object val)
  {
    Map<String,Object> map = props.get(atom);
    if (map == null)
      props.put(atom, map = new TreeMap<>());
    map.put(key, val);
  }

  @Override
  public <V> V getAtomProp(Atom atom, String key)
  {
    Map<String,Object> map = props.get(atom);
    if (map == null)
      return null;
    else
      return (V) map.get(key);
  }

  @Override
  public void setBondProp(Bond bond, String key, Object val)
  {
    Map<String,Object> map = props.get(bond);
    if (map == null)
      props.put(bond, map = new TreeMap<>());
    map.put(key, val);
  }

  @Override
  public <V> V getBondProp(Bond bond, String key)
  {
    Map<String,Object> map = props.get(bond);
    if (map == null)
      return null;
    else
      return (V) map.get(key);
  }

  @Override
  public String dumpDigraph(Digraph<Atom, Bond> digraph)
  {
    throw new UnsupportedOperationException();
  }
}
