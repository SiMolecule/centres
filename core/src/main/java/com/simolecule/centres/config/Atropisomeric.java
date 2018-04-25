package com.simolecule.centres.config;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Descriptor;
import com.simolecule.centres.Digraph;
import com.simolecule.centres.Edge;
import com.simolecule.centres.Node;
import com.simolecule.centres.Stats;
import com.simolecule.centres.rules.Priority;
import com.simolecule.centres.rules.SequenceRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Atropisomeric<A, B> extends Configuration<A, B> {

  public static final int LEFT  = 0x1;
  public static final int RIGHT = 0x2;
  private final B bond;

  public Atropisomeric(B bond, A[] foci, A[] carriers, int cfg)
  {
    super(foci, carriers, cfg);
    if (foci.length != 2)
      throw new IllegalArgumentException("Expected two focus atoms!");
    this.bond = bond;
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc)
  {
    // we store the label on the single bond
    mol.setBondProp(bond, BaseMol.CIP_LABEL_KEY, desc);
  }

  @Override
  public Descriptor label(SequenceRule<A, B> comp)
  {
    final Digraph<A, B> digraph = getDigraph();

    final A focus1 = getFoci()[0];
    final A focus2 = getFoci()[1];

    Node<A, B> root1 = digraph.getRoot();
    if (root1 == null)
      root1 = digraph.init(focus1);
    else
      digraph.changeRoot(root1);

    Edge<A,B> internal = findInternalEdge(root1.getEdges(), focus1, focus2);

    List<Edge<A, B>> edges1 = new ArrayList<>(root1.getEdges());
    edges1.remove(internal);

    Priority priority1 = comp.sort(root1, edges1);
    if (!priority1.isUnique())
      return Descriptor.Unknown;

    Node<A,B> root2 = internal.getOther(root1);
    digraph.changeRoot(root2);
    List<Edge<A, B>> edges2 = new ArrayList<>(root2.getEdges());
    edges2.remove(internal);

    Priority priority2 = comp.sort(root2, edges2);
    if (!priority2.isUnique())
      return Descriptor.Unknown;

    Object[] ordered = new Object[4];
    ordered[0] = edges1.get(0).getEnd().getAtom();
    ordered[1] = edges1.get(1).getEnd().getAtom();
    ordered[2] = edges2.get(0).getEnd().getAtom();
    ordered[3] = edges2.get(1).getEnd().getAtom();

    int parity = parity4(ordered, getCarriers());

    if (parity == 0)
      throw new RuntimeException("Could not calculate parity! Carrier mismatch");

    int config = this.getConfig();
    if (parity == 1)
      config ^= 0x3;

    Stats.INSTANCE.countRule(Math.max(priority1.getRuleIdx(),
                                      priority2.getRuleIdx()));

    if (config == RIGHT) {
      if (priority1.isPseduoAsymettric() !=
          priority2.isPseduoAsymettric())
        return Descriptor.m;
      else
        return Descriptor.M;
    } else if (config == LEFT) {
      if (priority1.isPseduoAsymettric() !=
          priority2.isPseduoAsymettric())
        return Descriptor.p;
      else
        return Descriptor.P;
    }

    return Descriptor.Unknown;
  }
}
