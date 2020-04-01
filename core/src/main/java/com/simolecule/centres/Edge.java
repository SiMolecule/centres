/*
 * Copyright (c) 2020 John Mayfield
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

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
