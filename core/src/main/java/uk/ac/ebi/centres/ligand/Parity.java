package uk.ac.ebi.centres.ligand;

public class Parity {

  static int parity4(Object[] trg, Object[] ref)
  {
    if (ref[0] == trg[0]) {
      if (ref[1] == trg[1]) {
        // a,b,c,d -> a,b,c,d
        if(ref[2] == trg[2] && ref[3] == trg[3]) return 2;
        // a,b,c,d -> a,b,d,c
        if(ref[2] == trg[3] && ref[3] == trg[2]) return 1;
      }
      else if (ref[1] == trg[2]) {
        // a,b,c,d -> a,c,b,d
        if(ref[2] == trg[1] && ref[3] == trg[3]) return 1;
        // a,b,c,d -> a,c,d,b
        if(ref[2] == trg[3] && ref[3] == trg[1]) return 2;
      }
      else if (ref[1] == trg[3]) {
        // a,b,c,d -> a,d,c,b
        if(ref[2] == trg[2] && ref[3] == trg[1]) return 1;
        // a,b,c,d -> a,d,b,c
        if(ref[2] == trg[1] && ref[3] == trg[2]) return 2;
      }
    }
    else if (ref[0] == trg[1]) {
      if (ref[1] == trg[0]) {
        // a,b,c,d -> b,a,c,d
        if(ref[2] == trg[2] && ref[3] == trg[3]) return 1;
        // a,b,c,d -> b,a,d,c
        if(ref[2] == trg[3] && ref[3] == trg[2]) return 2;
      }
      else if (ref[1] == trg[2]) {
        // a,b,c,d -> b,c,a,d
        if(ref[2] == trg[0] && ref[3] == trg[3]) return 2;
        // a,b,c,d -> b,c,d,a
        if(ref[2] == trg[3] && ref[3] == trg[0]) return 1;
      }
      else if (ref[1] == trg[3]) {
        // a,b,c,d -> b,d,c,a
        if(ref[2] == trg[2] && ref[3] == trg[0]) return 2;
        // a,b,c,d -> b,d,a,c
        if(ref[2] == trg[0] && ref[3] == trg[2]) return 1;
      }
    }
    else if (ref[0] == trg[2]) {
      if (ref[1] == trg[1]) {
        // a,b,c,d -> c,b,a,d
        if(ref[2] == trg[0] && ref[3] == trg[3]) return 1;
        // a,b,c,d -> c,b,d,a
        if(ref[2] == trg[3] && ref[3] == trg[0]) return 2;
      }
      else if (ref[1] == trg[0]) {
        // a,b,c,d -> c,a,b,d
        if(ref[2] == trg[1] && ref[3] == trg[3]) return 2;
        // a,b,c,d -> c,a,d,b
        if(ref[2] == trg[3] && ref[3] == trg[1]) return 1;
      }
      else if (ref[1] == trg[3]) {
        // a,b,c,d -> c,d,a,b
        if(ref[2] == trg[0] && ref[3] == trg[1]) return 2;
        // a,b,c,d -> c,d,b,a
        if(ref[2] == trg[1] && ref[3] == trg[0]) return 1;
      }
    }
    else if (ref[0] == trg[3]) {
      if (ref[1] == trg[1]) {
        // a,b,c,d -> d,b,c,a
        if(ref[2] == trg[2] && ref[3] == trg[0]) return 1;
        // a,b,c,d -> d,b,a,c
        if(ref[2] == trg[0] && ref[3] == trg[2]) return 2;
      }
      else if (ref[1] == trg[2]) {
        // a,b,c,d -> d,c,b,a
        if(ref[2] == trg[1] && ref[3] == trg[0]) return 2;
        // a,b,c,d -> d,c,a,b
        if(ref[2] == trg[0] && ref[3] == trg[1]) return 1;
      }
      else if (ref[1] == trg[0]) {
        // a,b,c,d -> d,a,c,b
        if(ref[2] == trg[2] && ref[3] == trg[1]) return 2;
        // a,b,c,d -> d,a,b,c
        if(ref[2] == trg[1] && ref[3] == trg[2]) return 1;
      }
    }
    return 0;
  }

}
