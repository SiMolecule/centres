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

package com.simolecule.centres.config;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Descriptor;
import com.simolecule.centres.Digraph;
import com.simolecule.centres.Edge;
import com.simolecule.centres.Node;
import com.simolecule.centres.rules.SequenceRule;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;

public abstract class Configuration<A, B> {

  private A[] foci;
  private A[] carriers;
  private int cfg;

  Digraph<A, B> digraph;

  public Configuration()
  {
  }

  public Configuration(A[] foci, A[] carriers, int cfg)
  {
    this.foci = foci;
    this.carriers = carriers;
    this.cfg = cfg;
  }

  public Configuration(A focus, A[] carriers, int cfg)
  {
    this.foci = (A[]) Array.newInstance(focus.getClass(), 1);
    this.foci[0] = focus;
    this.carriers = carriers;
    this.cfg = cfg;
  }

  public A getFocus()
  {
    return foci[0];
  }

  public A[] getFoci()
  {
    return foci;
  }

  public int getConfig()
  {
    return cfg;
  }

  public A[] getCarriers()
  {
    return carriers;
  }

  public abstract void setPrimaryLabel(BaseMol<A, B> mol, Descriptor desc);

  public void setDigraph(Digraph<A,B> digraph) {
    this.digraph = digraph;
  }

  public Digraph<A,B> getDigraph() {
    if (digraph == null)
      throw new IllegalArgumentException("Digraph has not been set!");
    return digraph;
  }

  static int parity4(Object[] trg, Object[] ref)
  {
    if (ref[0] == trg[0]) {
      if (ref[1] == trg[1]) {
        // a,b,c,d -> a,b,c,d
        if (ref[2] == trg[2] && ref[3] == trg[3]) return 2;
        // a,b,c,d -> a,b,d,c
        if (ref[2] == trg[3] && ref[3] == trg[2]) return 1;
      } else if (ref[1] == trg[2]) {
        // a,b,c,d -> a,c,b,d
        if (ref[2] == trg[1] && ref[3] == trg[3]) return 1;
        // a,b,c,d -> a,c,d,b
        if (ref[2] == trg[3] && ref[3] == trg[1]) return 2;
      } else if (ref[1] == trg[3]) {
        // a,b,c,d -> a,d,c,b
        if (ref[2] == trg[2] && ref[3] == trg[1]) return 1;
        // a,b,c,d -> a,d,b,c
        if (ref[2] == trg[1] && ref[3] == trg[2]) return 2;
      }
    } else if (ref[0] == trg[1]) {
      if (ref[1] == trg[0]) {
        // a,b,c,d -> b,a,c,d
        if (ref[2] == trg[2] && ref[3] == trg[3]) return 1;
        // a,b,c,d -> b,a,d,c
        if (ref[2] == trg[3] && ref[3] == trg[2]) return 2;
      } else if (ref[1] == trg[2]) {
        // a,b,c,d -> b,c,a,d
        if (ref[2] == trg[0] && ref[3] == trg[3]) return 2;
        // a,b,c,d -> b,c,d,a
        if (ref[2] == trg[3] && ref[3] == trg[0]) return 1;
      } else if (ref[1] == trg[3]) {
        // a,b,c,d -> b,d,c,a
        if (ref[2] == trg[2] && ref[3] == trg[0]) return 2;
        // a,b,c,d -> b,d,a,c
        if (ref[2] == trg[0] && ref[3] == trg[2]) return 1;
      }
    } else if (ref[0] == trg[2]) {
      if (ref[1] == trg[1]) {
        // a,b,c,d -> c,b,a,d
        if (ref[2] == trg[0] && ref[3] == trg[3]) return 1;
        // a,b,c,d -> c,b,d,a
        if (ref[2] == trg[3] && ref[3] == trg[0]) return 2;
      } else if (ref[1] == trg[0]) {
        // a,b,c,d -> c,a,b,d
        if (ref[2] == trg[1] && ref[3] == trg[3]) return 2;
        // a,b,c,d -> c,a,d,b
        if (ref[2] == trg[3] && ref[3] == trg[1]) return 1;
      } else if (ref[1] == trg[3]) {
        // a,b,c,d -> c,d,a,b
        if (ref[2] == trg[0] && ref[3] == trg[1]) return 2;
        // a,b,c,d -> c,d,b,a
        if (ref[2] == trg[1] && ref[3] == trg[0]) return 1;
      }
    } else if (ref[0] == trg[3]) {
      if (ref[1] == trg[1]) {
        // a,b,c,d -> d,b,c,a
        if (ref[2] == trg[2] && ref[3] == trg[0]) return 1;
        // a,b,c,d -> d,b,a,c
        if (ref[2] == trg[0] && ref[3] == trg[2]) return 2;
      } else if (ref[1] == trg[2]) {
        // a,b,c,d -> d,c,b,a
        if (ref[2] == trg[1] && ref[3] == trg[0]) return 2;
        // a,b,c,d -> d,c,a,b
        if (ref[2] == trg[0] && ref[3] == trg[1]) return 1;
      } else if (ref[1] == trg[0]) {
        // a,b,c,d -> d,a,c,b
        if (ref[2] == trg[2] && ref[3] == trg[1]) return 2;
        // a,b,c,d -> d,a,b,c
        if (ref[2] == trg[1] && ref[3] == trg[2]) return 1;
      }
    }
    return 0;
  }

  public abstract Descriptor label(SequenceRule<A, B> comp);

  protected Edge<A, B> findInternalEdge(List<Edge<A, B>> edges, A f1, A f2)
  {
    for (Edge<A, B> edge : edges) {
      if (edge.getBeg().isDuplicate() || edge.getEnd().isDuplicate())
        continue;
      if (isInternalEdge(edge, f1, f2)) {
        return edge;
      }
    }
    return null;
  }

  protected boolean isInternalEdge(Edge<A, B> edge, A f1, A f2)
  {
    Node<A,B> beg = edge.getBeg();
    Node<A,B> end = edge.getEnd();
    if (f1.equals(beg.getAtom()) && f2.equals(end.getAtom()))
      return true;
    else if (f1.equals(end.getAtom()) && f2.equals(beg.getAtom()))
      return true;
    return false;
  }

  protected void removeInternalEdges(List<Edge<A,B>> edges, A f1, A f2) {
    Iterator<Edge<A,B>> iter = edges.iterator();
    while (iter.hasNext()) {
      Edge<A,B> e = iter.next();
      if (isInternalEdge(e, f1, f2))
        iter.remove();
    }
  }

  protected void removeDuplicatedEdges(List<Edge<A,B>> edges) {
    Iterator<Edge<A,B>> iter = edges.iterator();
    while (iter.hasNext()) {
      Edge<A,B> e = iter.next();
      if (e.getEnd().isDuplicate())
        iter.remove();
    }
  }

  public Descriptor label(Node<A, B> node,
                          Digraph<A, B> digraph,
                          SequenceRule<A, B> comp) {
    return Descriptor.Unknown;
  }
}
