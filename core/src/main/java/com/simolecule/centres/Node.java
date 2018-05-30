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
   * at a bond with order &gt; 1.
   */
  public static final int BOND_DUPLICATE = 0x4;

  /**
   * Mask to check if a node is duplicated.
   */
  public static final int DUPLICATE = 0x6;

  /**
   * Node was created for an implicit hydrogen,
   * the 'atom' value will be null.
   */
  public static final int IMPL_HYDROGEN = 0x8;


  private final Digraph<A, B> g;
  private final A             atom;
  private final int           dist;
  private final short         atomicnumNum;
  private final short         atomicnumDen;
  private       Descriptor    aux;
  private int flags = 0;


  final         char[]           visit;
  private final List<Edge<A, B>> edges;

  public Node(Digraph<A, B> g,
              char[] visit,
              A atom,
              int anum,
              int aden,
              int dist,
              int flags)
  {
    this.g = g;
    this.visit = visit;
    this.atom = atom;
    this.dist = dist;
    this.flags = flags;
    this.atomicnumNum = (short) anum;
    this.atomicnumDen = (short) aden;
    this.edges = (flags & DUPLICATE) != 0
            ? new ArrayList<Edge<A, B>>()
            : new ArrayList<Edge<A, B>>(4);
    if (visit == null || (flags & DUPLICATE) != 0)
      this.flags |= EXPANDED;
  }

  public Digraph<A,B> getDigraph() {
    return g;
  }

  Node<A, B> newChild(int idx, A atom)
  {
    final char[] visit = this.visit.clone();
    visit[idx] = (char) (dist + 1);
    return new Node<>(g, visit, atom, g.getMol().getAtomicNum(atom), 1, dist + 1, 0);
  }

  Node<A, B> newTerminalChild(int idx, A atom, int flags)
  {
    int dist = (char) (((flags & DUPLICATE) != 0
            ? visit[idx]
            : this.dist + 1));
    int anum;
    int aden;
    if ((flags & BOND_DUPLICATE) != 0) {
      Mancude.Fraction frac = g.getMol().getFractionalAtomicNum(this.atom);
      if (frac.getDen() > 1) {
        anum = frac.getNum();
        aden = frac.getDen();
      } else {
        anum = g.getMol().getAtomicNum(atom);
        aden = 1;
      }
    } else {
      anum = g.getMol().getAtomicNum(atom);
      aden = 1;
    }
    return new Node<>(g, null, atom, anum, aden, dist, flags);
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

  public int getAtomicNumNumerator() {
    return atomicnumNum;
  }

  public int getAtomicNumDenominator() {
    return atomicnumDen;
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

  public List<Edge<A, B>> getEdges(A end)
  {
    List<Edge<A,B>> res = new ArrayList<>();
    for (Edge<A,B> edge : getEdges()) {
      if (edge.getEnd().isDuplicate())
        continue;
      if (end.equals(edge.getBeg().getAtom()) ||
          end.equals(edge.getEnd().getAtom()))
      res.add(edge);
    }
    return res;
  }

  public List<Edge<A, B>> getOutEdges()
  {
    List<Edge<A,B>> edges = new ArrayList<>();
    for (Edge<A,B> edge : getEdges()) {
      if (edge.isBeg(this))
        edges.add(edge);
    }
    return edges;
  }

  public List<Edge<A, B>> getNonTerminalOutEdges()
  {
    List<Edge<A,B>> edges = new ArrayList<>();
    for (Edge<A,B> edge : getEdges()) {
      if (edge.isBeg(this) && !edge.getEnd().isTerminal())
        edges.add(edge);
    }
    return edges;
  }

  public boolean isSet(int mask)
  {
    return (mask & flags) != 0;
  }

  public boolean isDuplicate()
  {
    return (flags & DUPLICATE) != 0;
  }

  public boolean isTerminal()
  {
    return visit == null || ((flags & EXPANDED) != 0 && edges.size() == 1);
  }

  public boolean isExpanded()
  {
    return (flags & EXPANDED) != 0;
  }

  private static String getElementSymbol(int elem)
  {
    switch (elem) {
      case 1:
        return "H";
      case 6:
        return "C";
      case 7:
        return "N";
      case 8:
        return "O";
      case 17:
        return "Cl";
      case 35:
        return "Br";
      case 9:
        return "F";
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
      if (atomicnumDen == 1)
        sb.append(getElementSymbol(atomicnumNum)).append(':').append(1+g.getMol().getAtomIdx(atom));
      else
        sb.append(atomicnumNum).append('/').append(atomicnumDen);
    } else if (atom == null) {
      sb.append("H");
    }
    if (isDuplicate())
      sb.append(')');
    if (aux != null && aux != Descriptor.Unknown)
      sb.append(':').append(aux);
    return sb.toString();
  }
}
