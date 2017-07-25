package com.simolecule.centres;

import com.simolecule.centres.Mancude.Fraction;

import java.util.Iterator;

/**
 * Defines how we can access the properties and connections
 * of a molecule.
 *
 * @param <A> atom type
 * @param <B> bond type
 */
public abstract class BaseMol<A, B> {

  private Fraction[] atomnums;

  public static final String CIP_LABEL_KEY = "cip.label";

  public abstract Object getBaseImpl();

  public abstract int getNumAtoms();

  public abstract int getNumBonds();

  public abstract A getAtom(int idx);

  public abstract int getAtomIdx(A atom);

  public Iterable<A> atoms() {
    return new Iterable<A>() {
      @Override
      public Iterator<A> iterator()
      {
        return new Iterator<A>() {
          private int pos = 0;

          @Override
          public boolean hasNext()
          {
            return pos < getNumAtoms();
          }

          @Override
          public A next()
          {
            return getAtom(pos++);
          }

          @Override
          public void remove()
          {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public abstract B getBond(int idx);

  public abstract int getBondIdx(B bond);

  public abstract Iterable<B> getBonds(A atom);

  public abstract A getOther(B bond, A atom);

  public abstract A getBeg(B bond);

  public abstract A getEnd(B bond);

  public abstract boolean isInRing(B bond);

  public abstract int getAtomicNum(A atom);

  public Fraction getFractionalAtomicNum(A atom) {
    if (atomnums == null)
      atomnums = Mancude.CalcFracAtomNums(this);
    return atomnums[getAtomIdx(atom)];
  }

  public abstract int getNumHydrogens(A atom);

  public abstract int getMassNum(A atom);

  public abstract int getCharge(A atom);

  public abstract int getBondOrder(B bond);

  public abstract void setAtomProp(A atom, String key, Object val);

  public abstract <V> V getAtomProp(A atom, String key);

  public abstract void setBondProp(B bond, String key, Object val);

  public abstract <V> V getBondProp(B bond, String key);

  public abstract String dumpDigraph(Digraph<A,B> digraph);


}
