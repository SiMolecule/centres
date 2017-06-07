package com.simolecule.centres.config;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Descriptor;
import com.simolecule.centres.Digraph;
import com.simolecule.centres.Node;
import com.simolecule.centres.rules.SequenceRule;

import java.util.Map;

public final class ExtendedTetrahedral<A,B> extends Configuration<A,B> {

  public static final int LEFT  = 0x1;
  public static final int RIGHT = 0x2;

  public ExtendedTetrahedral(A[] foci, A[] carriers, int cfg)
  {
    super(foci, carriers, cfg);
    if (foci.length != 3)
      throw new IllegalArgumentException();
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc)
  {

  }

  @Override
  public Descriptor label(SequenceRule<A, B> comp)
  {
    return null;
  }

  @Override
  public void labelAux(Map<Node<A, B>, Descriptor> map, Digraph<A, B> digraph, SequenceRule<A, B> comp)
  {

  }
}
