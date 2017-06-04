package com.simolecule.centres;

/**
 * Connection Table API.
 * @param <M> molecule type
 * @param <A> atom type
 * @param <B> bond type
 */
public interface MolApi<M,A,B> {

  int getNumAtoms(M mol);

  int getNumBonds(M mol);

  A getAtom(M mol, int idx);

  int getAtomIdx(M mol, A atom);

  B getBond(M mol, int idx);

  int getBondIdx(M mol, B bond);

  Iterable<B> getBonds(M mol, A atom);

  A getOther(M mol, B bond, A atom);

  int getAtomicNum(M mol, A atom);

  int getNumHydrogens(M mol, A atom);

  int getMassNum(M mol, A atom);

  int getBondOrder(M mol, B bond);

  void setAtomProp(M mol, A atom, String key, Object val);

  <V> V getAtomProp(M mol, A atom, String key);

  void setBondProp(M mol, B bond, String key, Object val);

  <V> V getBondProp(M mol, B bond, String key);

}
