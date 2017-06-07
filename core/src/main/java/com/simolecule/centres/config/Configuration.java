package com.simolecule.centres.config;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Descriptor;
import com.simolecule.centres.Digraph;
import com.simolecule.centres.Node;
import com.simolecule.centres.rules.SequenceRule;

import java.lang.reflect.Array;
import java.util.Map;

public abstract class Configuration<A, B> {

  private A[] foci;
  private A[] carriers;
  private int cfg;

  private Digraph<A, B> digraph;

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

  public void setFoci(A[] foci)
  {
    this.foci = foci;
  }

  public void setCarriers(A[] carriers)
  {
    this.carriers = carriers;
  }

  public void setCfg(int cfg)
  {
    this.cfg = cfg;
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

  public abstract void labelAux(Map<Node<A,B>,Descriptor> map, Digraph<A,B> digraph, SequenceRule<A, B> comp);
}
