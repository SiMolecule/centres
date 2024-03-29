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

package com.simolecule.centres;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.silent.Atom;
import org.openscience.cdk.silent.Bond;
import org.openscience.cdk.silent.PseudoAtom;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class CdkMol extends BaseMol<IAtom, IBond> {

  private final IAtomContainer base;

  public CdkMol(IAtomContainer base)
  {
    this.base = base;
    Cycles.markRingAtomsAndBonds(base);
  }

  @Override
  public IAtomContainer getBaseImpl()
  {
    return base;
  }

  @Override
  public int getNumAtoms()
  {
    return base.getAtomCount();
  }

  @Override
  public int getNumBonds()
  {
    return base.getBondCount();
  }

  @Override
  public IAtom getAtom(int idx)
  {
    return base.getAtom(idx);
  }

  @Override
  public int getAtomIdx(IAtom atom)
  {
    return base.indexOf(atom);
  }

  @Override
  public IBond getBond(int idx)
  {
    return base.getBond(idx);
  }

  @Override
  public int getBondIdx(IBond bond)
  {
    return base.indexOf(bond);
  }

  @Override
  public Iterable<IBond> getBonds(IAtom atom)
  {
    return base.getConnectedBondsList(atom);
  }

  @Override
  public IAtom getOther(IBond bond, IAtom atom)
  {
    return bond.getOther(atom);
  }

  @Override
  public IAtom getBeg(IBond bond)
  {
    return bond.getBegin();
  }

  @Override
  public IAtom getEnd(IBond bond)
  {
    return bond.getEnd();
  }

  @Override
  public boolean isInRing(IBond bond)
  {
    return bond.isInRing();
  }

  @Override
  public int getAtomicNum(IAtom atom)
  {
    if (atom == null)
      return 1;
    return atom.getAtomicNumber();
  }

  @Override
  public int getNumHydrogens(IAtom atom)
  {
    return atom.getImplicitHydrogenCount();
  }

  @Override
  public int getCharge(IAtom atom)
  {
    Integer q = atom.getFormalCharge();
    return q != null ? q : 0;
  }

  @Override
  public int getMassNum(IAtom atom)
  {
    if (atom == null)
      return 0;
    Integer mass = atom.getMassNumber();
    return mass == null ? 0 : mass;
  }

  @Override
  public int getBondOrder(IBond bond)
  {
    return bond.getOrder().numeric();
  }

  @Override
  public void setAtomProp(IAtom atom, String key, Object val)
  {
    atom.setProperty(key, val);
  }

  @Override
  public <V> V getAtomProp(IAtom atom, String key)
  {
    return atom.getProperty(key);
  }

  @Override
  public void setBondProp(IBond bond, String key, Object val)
  {
    bond.setProperty(key, val);
  }

  @Override
  public <V> V getBondProp(IBond bond, String key)
  {
    return bond.getProperty(key);
  }

  private static IAtomContainer toAtomContainer(Digraph<IAtom, IBond> digraph)
  {
    IAtomContainer     mol     = ((CdkMol) digraph.getMol()).getBaseImpl();
    IChemObjectBuilder builder = mol.getBuilder();
    IAtomContainer     res     = builder.newAtomContainer();

    Map<Node, IAtom>          amap  = new HashMap<>();
    Deque<Node<IAtom, IBond>> deque = new ArrayDeque<>();
    deque.add(digraph.getCurrRoot());
    while (!deque.isEmpty()) {
      Node<IAtom, IBond> node = deque.poll();
      IAtom              beg  = createAtom(mol, res, amap, node);

      for (Edge<IAtom, IBond> e : node.getEdges()) {
        if (!e.isBeg(node))
          continue;
        Node<IAtom, IBond> nodeEnd = e.getEnd();
        IAtom              end     = createAtom(mol, res, amap, nodeEnd);
        IBond bond = new Bond(beg, end, IBond.Order.SINGLE);
        res.addBond(bond);
        deque.add(nodeEnd);
      }
    }

    for (IAtom atom : res.atoms())
      atom.setImplicitHydrogenCount(0);
    res.setAtom(0, new PseudoAtom("X"));

    return res;
  }

  private static IAtom createAtom(IAtomContainer org, IAtomContainer res, Map<Node, IAtom> amap,
                                  Node<IAtom, IBond> node)
  {
    IAtom atom = amap.get(node);
    if (atom == null) {
      IAtom base = node.getAtom();
      if (base == null) {
        atom = new Atom("H");
      } else if (node.isDuplicate()) {
        atom = new Atom("*");
//        atom.setProperty(CDKConstants.COMMENT, node.getAtomicNumNumerator() + "/" + node.getAtomicNumDenominator());
        if (node.getAux() != null)
          atom.setProperty(CDKConstants.COMMENT, "(" + (1+org.indexOf(base)) + ") " + node.getAux() + "0");
        else
          atom.setProperty(CDKConstants.COMMENT, "(" + (1+org.indexOf(base)) + ")");
      } else {
        atom = new Atom(base.getSymbol());
        if (node.getAux() != null)
          atom.setProperty(CDKConstants.COMMENT, (1+org.indexOf(base)) + " " + node.getAux() + "0");
        else
          atom.setProperty(CDKConstants.COMMENT, (1+org.indexOf(base)));
      }
//      if (Objects.equals(node.getAtom(), node.getDigraph().getRule6Ref()))
//        atom.setProperty(CDKConstants.COMMENT, "#");
//      else
//        atom.setProperty(CDKConstants.COMMENT, "");
      res.addAtom(atom);
      amap.put(node, atom);
    }
    return atom;
  }

  @Override
  public String dumpDigraph(Digraph<IAtom, IBond> digraph)
  {
    SmilesGenerator smigen = new SmilesGenerator(SmiFlavor.CxSmiles);
    try {
      return smigen.create(toAtomContainer(digraph));
    } catch (CDKException e) {
      System.err.println("ERROR - " + e.getMessage());
      return "";
    }
  }
}
