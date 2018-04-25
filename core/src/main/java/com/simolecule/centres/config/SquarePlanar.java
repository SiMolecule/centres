package com.simolecule.centres.config;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Descriptor;
import com.simolecule.centres.Digraph;
import com.simolecule.centres.Edge;
import com.simolecule.centres.Node;
import com.simolecule.centres.rules.Priority;
import com.simolecule.centres.rules.SequenceRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SquarePlanar<A,B> extends Configuration<A,B> {

  private final Map<A,String> cache = new HashMap<>();

  // the index of the carrier in the trans position
  private final int[] TRANS_INDEX = new int[]{2,3,0,1};

  public SquarePlanar(A focus, A[] carriers)
  {
    super(focus, carriers, 1);
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc) {
    mol.setAtomProp(getFocus(), BaseMol.CIP_LABEL_KEY, desc);
    mol.setAtomProp(getFocus(), BaseMol.CONF_INDEX, cache.remove(getFocus()));
  }

  private A findTransAtom(A atom) {
    A[] carriers = getCarriers();
    for (int i = 0; i < carriers.length; i++) {
      if (carriers[i].equals(atom)) {
        return carriers[TRANS_INDEX[i]];
      }
    }
    throw new IllegalArgumentException();
  }

  private int getPriorityNumber(List<List<Edge<A,B>>> parts, A atom) {
    int num = 1;
    for (List<Edge<A,B>> part : parts) {
      for (Edge<A,B> e : part) {
        if (e.getEnd().getAtom().equals(atom))
          return num;
      }
      num++;
    }
    throw new IllegalArgumentException();
  }

  private boolean hasConfiguration(List<List<Edge<A,B>>> parts) {
    return parts.size() > 2 ||
           parts.size() == 2 &&
           parts.get(0).size() != 1 && parts.get(0).size() != 3;
  }

  private Descriptor label(Node<A, B> root, SequenceRule<A, B> comp) {
    List<Edge<A,B>>       edges    = root.getEdges();
    Priority              priority = comp.sort(root, edges);
    List<List<Edge<A,B>>> parts    = comp.getSorter().getGroups(edges);

    if (!hasConfiguration(parts))
      return Descriptor.None; // maybe return unknown?

    int low = 5;
    for (Edge<A,B> edge : parts.get(0)) {
      A beg = edge.getEnd().getAtom();
      A end = findTransAtom(beg);
      int num = getPriorityNumber(parts, end);
      if (num < low) {
        low = num;
      }
    }

    if (low < 5) {
      cache.put(getFocus(), "SP-4-" + low);
      return Descriptor.SP_4;
    }

    return Descriptor.None;
  }

  @Override
  public Descriptor label(SequenceRule<A, B> comp) {

    Digraph<A, B> digraph = getDigraph();
    Node<A, B>    root    = digraph.getRoot();
    if (root == null)
      root = digraph.init(getFocus());
    else
      digraph.changeRoot(digraph.getRoot());

    return label(root, comp);
  }
}
