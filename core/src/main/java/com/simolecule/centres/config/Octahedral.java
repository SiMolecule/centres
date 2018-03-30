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

public final class Octahedral<A,B> extends Configuration<A,B> {

  private final Map<A,String> cache = new HashMap<>();

  // the index of the carrier in the trans position
  private final int[] TRANS_INDEX = new int[]{5,3,4,1,2,0};

  public Octahedral(A focus, A[] carriers)
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
           parts.get(0).size() != 1 && parts.get(0).size() != 5;
  }

  private Descriptor label(Node<A, B> root, SequenceRule<A, B> comp) {
    List<Edge<A,B>>       edges    = root.getEdges();
    Priority              priority = comp.sort(root, edges);
    List<List<Edge<A,B>>> parts    = comp.getSorter().getGroups(edges);

    if (!hasConfiguration(parts))
      return Descriptor.None; // maybe return unknown?

    A fstAxisBeg = null, fstAxisEnd = null;
    int n1  = 7;
    int n2  = 7;
    int n3  = 7;

    for (Edge<A,B> edge : parts.get(0)) {
      A beg = edge.getEnd().getAtom();
      A end = findTransAtom(beg);
      int num = getPriorityNumber(parts, end);
      if (num < n1) {
        n1 = num;
        fstAxisBeg = beg;
        fstAxisEnd = end;
      }
    }

    for (List<Edge<A,B>> part : parts) {
      for (Edge<A, B> edge : part) {
        A   beg = edge.getEnd().getAtom();
        A   end = findTransAtom(beg);
        if (beg.equals(fstAxisBeg) || beg.equals(fstAxisEnd) ||
            end.equals(fstAxisBeg) || end.equals(fstAxisEnd))
          continue;
        int numBeg = getPriorityNumber(parts, beg);
        int numEnd = getPriorityNumber(parts, end);
        if (numBeg < n2) {
          n2 = numBeg;
          n3 = numEnd;
        } else if (numBeg == n2 && numEnd < n3) {
          n3 = numEnd;
        }
      }
    }

    if (n1 < 7 && n3 < 7) {
      cache.put(getFocus(), "OC-6-" + n1 + "" + n3);
      return Descriptor.OC_6;
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



  @Override
  public void labelAux(Map<Node<A, B>, Descriptor> map, Digraph<A, B> digraph,
                       SequenceRule<A, B> comp) {

  }
}
