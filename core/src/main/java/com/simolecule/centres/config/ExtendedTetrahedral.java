package com.simolecule.centres.config;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Descriptor;
import com.simolecule.centres.Digraph;
import com.simolecule.centres.Edge;
import com.simolecule.centres.Node;
import com.simolecule.centres.Stats;
import com.simolecule.centres.rules.Priority;
import com.simolecule.centres.rules.Rules;
import com.simolecule.centres.rules.SequenceRule;

import java.util.ArrayList;
import java.util.Iterator;
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

    return label2(comp, end1, end2, atom1, atom2, edges1, edges2);
  }

  private void removeDuplicates(List<Edge<A,B>> e) {
    Iterator<Edge<A,B>> iter = e.iterator();
    while (iter.hasNext()) {
      if (iter.next().getEnd().isDuplicate())
        iter.remove();
    }
  }

  @Override
  public Descriptor label(Node<A, B>    node,
                          Digraph<A, B> digraph,
                          Rules<A, B>   comp) {

    A[] foci = getFoci();
    A focus = foci[0];

    if (focus != node.getAtom())
      return Descriptor.Unknown;

    Node<A,B> root = node;
    digraph.changeRoot(root);
    List<Edge<A, B>> edges = root.getEdges();
    removeDuplicates(edges);
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
    removeDuplicates(edges1);
    removeDuplicates(edges2);
    edges1.remove(edges.get(0));
    edges2.remove(edges.get(1));

    return label2(comp, end1, end2, atom1, atom2, edges1, edges2);
  }

  private Descriptor label2(SequenceRule<A, B> comp,
                            Node<A, B> end1, Node<A, B> end2,
                            A atom1, A atom2,
                            List<Edge<A, B>> edges1,
                            List<Edge<A, B>> edges2) {
    if (edges1.size() > 2)
      return Descriptor.Unknown;
    if (edges2.size() > 2)
      return Descriptor.Unknown;

    BaseMol<A,B> mol = end1.getDigraph().getMol();

    Priority priority1 = comp.sort(end1, edges1);
    Priority priority2 = comp.sort(end2, edges2);
    if (!priority1.isUnique() && !priority2.isUnique() &&
        mol.isInRing(edges1.get(0).getBond()) &&
        mol.isInRing(edges2.get(0).getBond())) {
      end1.getDigraph().setRule6Ref(edges1.get(0).getEnd().getAtom());
      priority1 = comp.sort(end1, edges1);
      priority2 = comp.sort(end2, edges2);
      end1.getDigraph().setRule6Ref(null);
    }
    if (!priority1.isUnique() || !priority2.isUnique())
      return Descriptor.Unknown;

    Object[] ordered = new Object[4];
    ordered[0] = edges1.get(0).getEnd().getAtom();
    ordered[1] = edges1.size() > 1 ? edges1.get(1).getEnd().getAtom() : atom1;
    ordered[2] = edges2.get(0).getEnd().getAtom();
    ordered[3] = edges2.size() > 1 ? edges2.get(1).getEnd().getAtom() : atom2;

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

    Stats.INSTANCE.countRule(Math.max(priority1.getRuleIdx(),
                                      priority2.getRuleIdx()));

    if (config == RIGHT) {
      if (priority1.isPseduoAsymettric() != priority2.isPseduoAsymettric())
        return Descriptor.m;
      else
        return Descriptor.M;
    } else if (config == LEFT) {
      if (priority1.isPseduoAsymettric() != priority2.isPseduoAsymettric())
        return Descriptor.p;
      else
        return Descriptor.P;
    }
    return null;
  }
}
