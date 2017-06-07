package com.simolecule.centres;

/**
 * Defines how we can access the properties and connections
 * of a molecule.
 *
 * @param <A> atom type
 * @param <B> bond type
 */
public abstract class BaseMol<A, B> {

  public static final String CIP_LABEL_KEY = "cip.label";

  public abstract Object getBaseImpl();

  public abstract int getNumAtoms();

  public abstract int getNumBonds();

  public abstract A getAtom(int idx);

  public abstract int getAtomIdx(A atom);

  public abstract B getBond(int idx);

  public abstract int getBondIdx(B bond);

  public abstract Iterable<B> getBonds(A atom);

  public abstract A getOther(B bond, A atom);

  public abstract A getBeg(B bond);

  public abstract A getEnd(B bond);

  public abstract int getAtomicNum(A atom);

  public abstract int getNumHydrogens(A atom);

  public abstract int getMassNum(A atom);

  public abstract int getBondOrder(B bond);

  public abstract void setAtomProp(A atom, String key, Object val);

  public abstract <V> V getAtomProp(A atom, String key);

  public abstract void setBondProp(B bond, String key, Object val);

  public abstract <V> V getBondProp(B bond, String key);

  public abstract String dumpDigraph(Digraph<A,B> digraph);

  void markMancudeRings() {

  }
}
