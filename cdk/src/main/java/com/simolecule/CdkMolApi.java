package com.simolecule;

import com.simolecule.centres.MolApi;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

public class CdkMolApi
        implements MolApi<IAtomContainer,IAtom,IBond> {

  @Override
  public int getNumAtoms(IAtomContainer mol)
  {
    return mol.getAtomCount();
  }

  @Override
  public int getNumBonds(IAtomContainer mol)
  {
    return mol.getBondCount();
  }

  @Override
  public IAtom getAtom(IAtomContainer mol, int idx)
  {
    return mol.getAtom(idx);
  }

  @Override
  public int getAtomIdx(IAtomContainer mol, IAtom atom)
  {
    return mol.indexOf(atom);
  }

  @Override
  public IBond getBond(IAtomContainer mol, int idx)
  {
    return mol.getBond(idx);
  }

  @Override
  public int getBondIdx(IAtomContainer mol, IBond bond)
  {
    return mol.indexOf(bond);
  }

  @Override
  public Iterable<IBond> getBonds(IAtomContainer mol, IAtom atom)
  {
    return mol.getConnectedBondsList(atom);
  }

  @Override
  public IAtom getOther(IAtomContainer mol, IBond bond, IAtom atom)
  {
    return bond.getOther(atom);
  }

  @Override
  public int getAtomicNum(IAtomContainer mol, IAtom atom)
  {
    return atom.getAtomicNumber();
  }

  @Override
  public int getNumHydrogens(IAtomContainer mol, IAtom atom)
  {
    return atom.getImplicitHydrogenCount();
  }

  @Override
  public int getMassNum(IAtomContainer mol, IAtom atom)
  {
    Integer mass = atom.getMassNumber();
    return mass == null ? 0 : mass;
  }

  @Override
  public int getBondOrder(IAtomContainer mol, IBond bond)
  {
    return bond.getOrder().numeric();
  }

  @Override
  public void setAtomProp(IAtomContainer mol, IAtom atom, String key, Object val)
  {
    atom.setProperty(key, val);
  }

  @Override
  public <V> V getAtomProp(IAtomContainer mol, IAtom atom, String key)
  {
    return atom.getProperty(key);
  }

  @Override
  public void setBondProp(IAtomContainer mol, IBond bond, String key, Object val)
  {
    bond.setProperty(key, val);
  }

  @Override
  public <V> V getBondProp(IAtomContainer mol, IBond bond, String key)
  {
    return bond.getProperty(key);
  }
}
