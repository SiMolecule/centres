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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Tetrahedral<A, B> extends Configuration<A, B> {

  public static final int LEFT  = 0x1;
  public static final int RIGHT = 0x2;

  public Tetrahedral(A focus, A[] carriers, int cfg)
  {
    super(focus, carriers, cfg);
  }

  private boolean visitRing(BaseMol<A,B> mol, A spiro, A atom, B prev, int[] visit, int depth) {
    if (atom.equals(spiro))
      return true;
    visit[mol.getAtomIdx(atom)] = depth;
    boolean res = false;
    for (B bond : mol.getBonds(atom)) {
      if (!mol.isInRing(bond) || bond == prev)
        continue;
      A other = mol.getOther(bond, atom);
      if (visit[mol.getAtomIdx(other)] != 0)
        continue;
      if (visitRing(mol, spiro, other, bond, visit, depth+1))
        res = true;
    }
    return res;
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc)
  {
    mol.setAtomProp(getFocus(), BaseMol.CIP_LABEL_KEY, desc);
  }

  @Override
  public Descriptor label(SequenceRule<A, B> comp)
  {
    Digraph<A, B> digraph = getDigraph();
    Node<A, B>    root    = digraph.getRoot();
    if (root == null)
      root = digraph.init(getFocus());
    else
      digraph.changeRoot(digraph.getRoot());

    return label(root, comp);
  }

  private Descriptor label(Node<A, B> node, SequenceRule<A, B> comp)
  {
    A focus = getFocus();
    final List<Edge<A, B>> edges = new ArrayList<>(node.getEdges());

    // something not right!?! bad creation
    if (edges.size() < 3)
      return Descriptor.None;

    Priority priority = comp.sort(node, edges);

    boolean isUnique = priority.isUnique();
    if (!isUnique && edges.size() == 4) {

      BaseMol<A, B> mol = comp.getMol();

      // check for spiro cases
      for (Edge<A, B> edge : edges) {
        if (edge.getBond() != null && !mol.isInRing(edge.getBond()))
          return Descriptor.Unknown;
      }

      List<List<Edge<A,B>>> partition = comp.getSorter().getGroups(edges);
      
      // expect a,a',b,b'!
      if (partition.size() != 2 || partition.get(0).size() != 2)
        return Descriptor.Unknown;

      int[] visit = new int[mol.getNumAtoms()];
      A first = edges.get(0).getEnd().getAtom();
      visit[mol.getAtomIdx(focus)] = 1;
      visit[mol.getAtomIdx(first)] = 1;
      visitRing(mol, focus, first, edges.get(0).getBond(), visit, 2);

      // if with our spiro traversal we don't reach either atom in the
      // the second partition then there it is not stereogenic
      int a2 = mol.getAtomIdx(edges.get(2).getEnd().getAtom());
      int a3 = mol.getAtomIdx(edges.get(3).getEnd().getAtom());
      if (visit[a2] == 0 &&
          visit[a3] == 0)
        return Descriptor.Unknown;

      // make sure we go the 'right' way around
      if (visit[a3] > 0 && (visit[a3] < visit[a2] || visit[a2] == 0)) {
        Collections.swap(edges, 2, 3);
      }

    } else if (!isUnique) {
      return Descriptor.Unknown;
    }

    Object[] ordered = new Object[4];
    int      idx     = 0;
    for (Edge<A, B> edge : edges) {
      if (edge.getEnd().isSet(Node.BOND_DUPLICATE) ||
          edge.getEnd().isSet(Node.IMPL_HYDROGEN))
        continue;
      ordered[idx++] = edge.getEnd().getAtom();
    }
    if (idx < 4)
      ordered[idx] = focus;


    if (node.getDigraph().getRoot() == node)
      Stats.INSTANCE.countRule(priority.getRuleIdx());

    int parity = parity4(ordered, getCarriers());

    if (parity == 0)
      throw new RuntimeException("Could not calculate parity! Carrier mismatch");

    int config = this.getConfig();
    if (parity == 1)
      config ^= 0x3;

    if (config == 0x1) {
      if (priority.isPseduoAsymettric())
        return Descriptor.s;
      else
        return Descriptor.S;
    } else if (config == 0x2) {
      if (priority.isPseduoAsymettric())
        return Descriptor.r;
      else
        return Descriptor.R;
    }

    return Descriptor.Unknown;
  }

  @Override
  public void labelAux(Map<Node<A, B>, Descriptor> map,
                       Digraph<A, B> digraph,
                       SequenceRule<A, B> comp)
  {
    A                focus = getFocus();
    List<Node<A, B>> nodes = digraph.getNodes(focus);
    for (Node<A, B> node : nodes) {
      if (map.containsKey(node))
        continue;
      digraph.changeRoot(node);
      Descriptor label = label(node, comp);
      if (label != Descriptor.Unknown)
        map.put(node, label);
    }
  }
}
