package com.simolecule.centres;

public class Edge<A, B> {

  private Node<A, B> beg, end;
  private final B bond;
  private Descriptor aux;

  public Edge(Node<A, B> beg, Node<A, B> end, B bond)
  {
    this.beg = beg;
    this.end = end;
    this.bond = bond;
  }

  public Node<A, B> getOther(Node<A, B> node)
  {
    if (node.equals(getBeg()))
      return getEnd();
    else if (node.equals(getEnd()))
      return getBeg();
    else
      throw new IllegalArgumentException("Not an end-point of this edge!");
  }

  public Node<A, B> getBeg()
  {
    return beg;
  }

  public Node<A, B> getEnd()
  {
    return end;
  }

  public Descriptor getAux()
  {
    return aux;
  }

  public void setAux(Descriptor aux)
  {
    this.aux = aux;
  }

  public void flip()
  {
    Node<A, B> tmp = end;
    end = beg;
    beg = tmp;
  }

  public B getBond()
  {
    return bond;
  }

  public boolean isBeg(Node<A, B> node)
  {
    return node.equals(beg);
  }

  public boolean isEnd(Node<A, B> node)
  {
    return node.equals(end);
  }

  @Override
  public String toString()
  {
    return beg.toString() + "->" + end.toString();
  }
}
