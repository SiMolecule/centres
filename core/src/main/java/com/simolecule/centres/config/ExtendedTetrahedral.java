package com.simolecule.centres.config;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Descriptor;
import com.simolecule.centres.Digraph;
import com.simolecule.centres.Edge;
import com.simolecule.centres.Node;
import com.simolecule.centres.rules.Priority;
import com.simolecule.centres.rules.SequenceRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class ExtendedTetrahedral<A, B> extends Configuration<A, B> {

  public static final int LEFT  = 0x1;
  public static final int RIGHT = 0x2;

  public ExtendedTetrahedral(A[] foci, A[] carriers, int cfg)
  {
    super(foci, carriers, cfg);
    if (foci.length != 3)
      throw new IllegalArgumentException("Only X=X=X cumulenes are currently supported.");
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc)
  {
    // we store the label on the middle atom
    mol.setAtomProp(getFocus(), BaseMol.CIP_LABEL_KEY, desc);
  }

  @Override
  public Descriptor label(SequenceRule<A, B> comp)
  {
    Digraph<A, B> digraph = getDigraph();
    Node<A, B>    root;
    if (digraph.getRoot() == null)
      root = digraph.init(getFocus());
    else
      root = digraph.getRoot();
    digraph.changeRoot(root);

    List<Edge<A, B>> edges = root.getEdges();
    if (edges.size() != 2)
      return Descriptor.Unknown;

    Node<A, B> end1 = edges.get(0).getEnd();
    Node<A, B> end2 = edges.get(1).getEnd();

    A atom1 = getFoci()[1];
    A atom2 = getFoci()[2];

    // swap to make things easier when comparing to reference ordering
    if (end1.getAtom() == atom2 && end2.getAtom() == atom1) {
      A tmp = atom1;
      atom1 = atom2;
      atom2 = tmp;
    } else if (end1.getAtom() != atom1 && end2.getAtom() != atom2) {
      System.err.println("Stereo Focus mismatch!");
      return Descriptor.Unknown;
    }

    List<Edge<A, B>> edges1 = new ArrayList<>(end1.getEdges());
    List<Edge<A, B>> edges2 = new ArrayList<>(end2.getEdges());

    edges1.remove(edges.get(0));
    edges2.remove(edges.get(1));

    if (edges1.size() > 2)
      return Descriptor.Unknown;
    if (edges2.size() > 2)
      return Descriptor.Unknown;

    Priority priority1 = comp.prioritise(end1, edges1);
    if (!priority1.isUnique())
      return Descriptor.Unknown;
    Priority priority2 = comp.prioritise(end2, edges2);
    if (!priority2.isUnique())
      return Descriptor.Unknown;

    Object[] ordered = new Object[4];
    ordered[0] = edges1.get(0).getEnd().getAtom();
    ordered[1] = edges1.size() > 0 ? edges1.get(1).getEnd().getAtom() : atom1;
    ordered[2] = edges2.get(0).getEnd().getAtom();
    ordered[3] = edges2.size() > 0 ? edges2.get(1).getEnd().getAtom() : atom2;

    if (ordered[1] == null)
      ordered[1] = atom1;
    if (ordered[3] == null)
      ordered[3] = atom2;

    int parity = parity4(ordered, getCarriers());

    if (parity == 0)
      throw new RuntimeException("Could not calculate parity! Carrier mismatch");

    int config = this.getConfig();
    if (parity == 1)
      config ^= 0x3;

    if (config == RIGHT) {
      if (priority1.isPseduoAsymettric() || priority2.isPseduoAsymettric())
        return Descriptor.m;
      else
        return Descriptor.M;
    } else if (config == LEFT) {
      if (priority1.isPseduoAsymettric() || priority2.isPseduoAsymettric())
        return Descriptor.p;
      else
        return Descriptor.P;
    }

    return Descriptor.Unknown;
  }

  @Override
  public void labelAux(Map<Node<A, B>, Descriptor> map, Digraph<A, B> digraph, SequenceRule<A, B> comp)
  {
    // not yet supported
  }
}
