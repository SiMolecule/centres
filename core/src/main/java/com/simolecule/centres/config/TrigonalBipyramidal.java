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

public final class TrigonalBipyramidal<A,B> extends Configuration<A,B> {

  private final Map<A,String> cache = new HashMap<>();

  public TrigonalBipyramidal(A focus, A[] carriers)
  {
    super(focus, carriers, 1);
  }

  @Override
  public void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc) {
    mol.setAtomProp(getFocus(), BaseMol.CIP_LABEL_KEY, desc);
    mol.setAtomProp(getFocus(), BaseMol.CONF_INDEX, cache.remove(getFocus()));
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
    // check this!
    return parts.size() >= 2;
  }

  private Descriptor label(Node<A, B> root, SequenceRule<A, B> comp) {
    List<Edge<A,B>>       edges    = root.getEdges();
    Priority              priority = comp.sort(root, edges);
    List<List<Edge<A,B>>> parts    = comp.getSorter().getGroups(edges);

    if (!hasConfiguration(parts))
      return Descriptor.ns; // maybe return unknown?

    A[] carriers = getCarriers();
    for (Edge<A,B> edge : edges) {
      A beg = edge.getEnd().getAtom();
      A end = null;
      if (beg.equals(carriers[0]))
        end = carriers[4];
      else if (beg.equals(carriers[4]))
        end = carriers[0];
      if (end != null) {
        cache.put(getFocus(), "TBPY-5-" + getPriorityNumber(parts, beg)
                              + "" + getPriorityNumber(parts, end));
        return Descriptor.TBPY_5;
      }
    }

    return Descriptor.ns;
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
