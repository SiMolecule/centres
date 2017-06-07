package com.simolecule.centres;

import java.util.ArrayList;
import java.util.List;

public final class Node<A, B> {

  /**
   * Flag indicates whether the node has been expanded.
   */
  public static final int EXPANDED = 0x1;

  /**
   * Flag indicates whether the node was duplicated
   * at a ring closure.
   */
  public static final int RING_DUPLICATE = 0x2;

  /**
   * Flag indicates whether the node was duplicated
   * at a bond with order > 1.
   */
  public static final int BOND_DUPLICATED = 0x4;

  /**
   * Mask to check if a node is duplicated.
   */
  public static final int DUPLICATED = 0x6;

  /**
   * Node was created for an implicit hydrogen,
   * the 'atom' value will be null.
   */
  public static final int IMPL_HYDROGEN = 0x8;


  private final Digraph<A, B> g;
  private final A             atom;
  private final int           dist;
  private       Descriptor    aux;
  private int flags = 0;


  final         short[]          visit;
  private final List<Edge<A, B>> edges;

  public Node(Digraph<A, B> g,
              short[] visit,
              A atom,
              int dist,
              int flags)
  {
    this.g = g;
    this.visit = visit;
    this.atom = atom;
    this.dist = dist;
    this.flags = flags;
    this.edges = (flags & DUPLICATED) != 0
            ? new ArrayList<Edge<A, B>>()
            : new ArrayList<Edge<A, B>>(4);
    if (g == null || (flags & DUPLICATED) != 0)
      this.flags |= EXPANDED;
  }

  Node<A, B> newChild(int idx, A atom)
  {
    final short[] visit = this.visit.clone();
    visit[idx] = (short) (dist + 1);
    return new Node<>(g, visit, atom, dist + 1, 0);
  }

  Node<A, B> newTerminalChild(int idx, A atom, int flags)
  {
    short dist = (short) ((flags & DUPLICATED) != 0
            ? visit[idx]
            : this.dist + 1);
    return new Node<>(null, null, atom, dist, flags);
  }

  void add(Edge<A, B> e)
  {
    this.edges.add(e);
  }

  public int getDistance()
  {
    return dist;
  }

  public A getAtom()
  {
    return atom;
  }

  public Descriptor getAux()
  {
    return aux;
  }

  public void setAux(Descriptor desc)
  {
    this.aux = desc;
  }

  public List<Edge<A, B>> getEdges()
  {
    if ((flags & EXPANDED) == 0) {
      flags |= EXPANDED;
      g.expand(this);
    }
    return edges;
  }

  public boolean isSet(int mask) {
    return (mask & flags) != 0;
  }

  public boolean isDuplicate()
  {
    return (flags & DUPLICATED) != 0;
  }

  public boolean isTerminal()
  {
    return g == null || ((flags & EXPANDED) != 0 && edges.size() == 1);
  }

  String getSymbol(int elem)
  {
    switch (elem) {
      case 6:
        return "C";
      case 7:
        return "N";
      case 8:
        return "O";
      default:
        return "#" + elem;
    }
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    if (isDuplicate())
      sb.append('(');
    if (g != null && atom != null) {
      sb.append(getSymbol(g.getMol().getAtomicNum(atom)) + ":" + g.getMol().getAtomIdx(atom));
    } else if (atom != null) {

    } else {
      sb.append("H");
    }
    if (isDuplicate())
      sb.append(')');
    return sb.toString();
  }
}
