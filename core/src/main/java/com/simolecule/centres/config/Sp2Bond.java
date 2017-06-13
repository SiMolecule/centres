package com.simolecule.centres.config;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Descriptor;
import com.simolecule.centres.Digraph;
import com.simolecule.centres.Edge;
import com.simolecule.centres.Node;
import com.simolecule.centres.rules.Priority;
import com.simolecule.centres.rules.SequenceRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Sp2Bond<A, B> extends Configuration<A, B> {

  public static final int OPPOSITE = 0x1;
  public static final int TOGETHER = 0x2;

  private B bond;

  public Sp2Bond(B bond, A[] foci, A[] carriers, int cfg)
  {
    super(foci, carriers, cfg);
    this.bond = bond;
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc)
  {
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

    A[] carriers = getCarriers();
    int config   = getConfig();

    // swap
    if (edges1.size() > 1 && carriers[0].equals(edges1.get(1).getEnd().getAtom()))
      config ^= 0x3;
    // swap
    if (edges2.size() > 1 && carriers[1].equals(edges2.get(1).getEnd().getAtom()))
      config ^= 0x3;

    if (config == TOGETHER) {
      if (priority1.isPseduoAsymettric() ||
          priority2.isPseduoAsymettric())
        return Descriptor.seqCis;
      else
        return Descriptor.Z;
    } else if (config == OPPOSITE) {
      if (priority1.isPseduoAsymettric() ||
          priority2.isPseduoAsymettric())
        return Descriptor.seqTrans;
      else
        return Descriptor.E;
    }

    return Descriptor.Unknown;
  }

  @Override
  public void labelAux(Map<Node<A, B>, Descriptor> map,
                       Digraph<A, B> digraph,
                       SequenceRule<A, B> comp)
  {
    A focus1 = getFoci()[0];
    A focus2 = getFoci()[1];

    for (Node<A,B> root1 : digraph.getNodes(focus1)) {
      Edge<A,B> internal = findInternalEdge(root1.getEdges(), focus1, focus2);
      if (internal == null)
        continue;
      Node<A,B> root2 = internal.getOther(root1);
      List<Edge<A,B>> edges1 = new ArrayList<>(root1.getEdges());
      List<Edge<A,B>> edges2 = new ArrayList<>(root2.getEdges());
      edges1.remove(internal);
      edges2.remove(internal);
      removeInternalEdges(edges1, focus1, focus2);
      removeInternalEdges(edges2, focus1, focus2);

      digraph.changeRoot(root1);
      Priority priority1 = comp.sort(root1, edges1);
      if (!priority1.isUnique())
        continue;

      digraph.changeRoot(root2);
      Priority priority2 = comp.sort(root2, edges2);
      if (!priority2.isUnique())
        continue;

      A[] carriers = getCarriers();
      int config   = getConfig();

      // swap
      if (edges1.size() > 1 && carriers[0].equals(edges1.get(1).getEnd().getAtom()))
        config ^= 0x3;
      // swap
      if (edges2.size() > 1 && carriers[1].equals(edges2.get(1).getEnd().getAtom()))
        config ^= 0x3;

      if (config == TOGETHER) {
        if (priority1.isPseduoAsymettric() ||
            priority1.isPseduoAsymettric()) {
          map.put(root1, Descriptor.seqCis);
          map.put(root2, Descriptor.seqCis);
        } else {
          map.put(root1, Descriptor.Z);
          map.put(root2, Descriptor.Z);
        }
      } else if (config == OPPOSITE) {
        if (priority2.isPseduoAsymettric() ||
            priority2.isPseduoAsymettric()) {
          map.put(root1, Descriptor.seqTrans);
          map.put(root2, Descriptor.seqTrans);
        } else {
          map.put(root1, Descriptor.E);
          map.put(root2, Descriptor.E);
        }
      }
    }
  }
}
