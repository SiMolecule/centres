package uk.ac.ebi.centres.test;

import com.simolecule.centres.MolApi;

public class MockApi implements MolApi<Object,TestAtom,Object> {
  @Override
  public int getNumAtoms(Object mol)
  {
    return 0;
  }

  @Override
  public int getNumBonds(Object mol)
  {
    return 0;
  }

  @Override
  public TestAtom getAtom(Object mol, int idx)
  {
    return null;
  }

  @Override
  public int getAtomIdx(Object mol, TestAtom atom)
  {
    return 0;
  }

  @Override
  public Object getBond(Object mol, int idx)
  {
    return null;
  }

  @Override
  public int getBondIdx(Object mol, Object bond)
  {
    return 0;
  }

  @Override
  public Iterable<Object> getBonds(Object mol, TestAtom atom)
  {
    return null;
  }

  @Override
  public TestAtom getOther(Object mol, Object bond, TestAtom atom)
  {
    return null;
  }

  @Override
  public int getAtomicNum(Object mol, TestAtom atom)
  {
    return atom.getAtomicNumber();
  }

  @Override
  public int getNumHydrogens(Object mol, TestAtom atom)
  {
    return 0;
  }

  @Override
  public int getMassNum(Object mol, TestAtom atom)
  {
    return atom.getMassNumber();
  }

  @Override
  public int getBondOrder(Object mol, Object bond)
  {
    return 0;
  }

  @Override
  public void setAtomProp(Object mol, TestAtom atom, String key, Object val)
  {

  }

  @Override
  public <V> V getAtomProp(Object mol, TestAtom atom, String key)
  {
    return null;
  }

  @Override
  public void setBondProp(Object mol, Object bond, String key, Object val)
  {

  }

  @Override
  public <V> V getBondProp(Object mol, Object bond, String key)
  {
    return null;
  }
}
