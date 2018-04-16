package com.simolecule.centres;

import com.sun.org.apache.bcel.internal.generic.FREM;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;

public final class Mancude {

  public enum Type {
    Cv4D4, // =CH-
    Nv3D2, // =N-
    Nv4D3Plus, // =[N+]<
    Nv2D2Minus, // -[N-]-
    Cv3D3Minus, // -[CH-]-
    Ov3D2Plus,  // -[O+]=
    Other
  }

  public static <A, B> boolean SeedTypes(Type[] types, BaseMol<A, B> mol)
  {
    boolean result = false;
    for (A atom : mol.atoms()) {
      final int aidx = mol.getAtomIdx(atom);
      types[aidx] = Type.Other;

      // check ring
      int     btypes = mol.getNumHydrogens(atom);
      boolean ring   = false;
      for (B bond : mol.getBonds(atom)) {
        switch (mol.getBondOrder(bond)) {
          case 1:
            btypes += 0x00000001;
            break;
          case 2:
            btypes += 0x00000100;
            break;
          case 3:
            btypes += 0x00010000;
            break;
          default:
            btypes += 0x01000000;
            break;
        }
        if (mol.isInRing(bond))
          ring = true;
      }
      if (ring) {
        int q = mol.getCharge(atom);
        switch (mol.getAtomicNum(atom)) {
          case 6:  // C
          case 14: // Si
          case 32: // Ge
            if (q == 0 && btypes == 0x0102)
              types[aidx] = Type.Cv4D4;
            else if (q == -1 && btypes == 0x0003) {
              types[aidx] = Type.Cv3D3Minus;
              result = true;
            }
            break;
          case 7:  // N
          case 15: // P
          case 33: // As
            if (q == 0 && btypes == 0x0101) {
              types[aidx] = Type.Nv3D2;
              result = true;
            } else if (q == -1 && btypes == 0x0002) {
              types[aidx] = Type.Nv2D2Minus;
              result = true;
            } else if (q == +1 && btypes == 0x0102) {
              types[aidx] = Type.Nv4D3Plus;
              result = true;
            }
            break;
          case 8: // O
            if (q == 1 && btypes == 0x0101) {
              types[aidx] = Type.Ov3D2Plus;
              result = true;
            }
            break;
        }
      }
    }
    return result;
  }

  public static <A, B> void RelaxTypes(Type[] types, BaseMol<A, B> mol)
  {
    int[]    counts = new int[mol.getNumAtoms()];
    Deque<A> queue  = new ArrayDeque<>();
    for (A atom : mol.atoms()) {
      int aidx = mol.getAtomIdx(atom);
      for (B bond : mol.getBonds(atom)) {
        A nbr = mol.getOther(bond, atom);
        if (types[mol.getAtomIdx(nbr)] != Type.Other)
          counts[aidx]++;
      }
      if (counts[aidx] == 1)
        queue.add(atom);
    }
    while (!queue.isEmpty()) {
      A   atom = queue.poll();
      int aidx = mol.getAtomIdx(atom);
      if (types[aidx] != Type.Other) {
        types[aidx] = Type.Other;
        for (B bond : mol.getBonds(atom)) {
          A   nbr    = mol.getOther(bond, atom);
          int nbridx = mol.getAtomIdx(nbr);
          if (--counts[nbridx] == 1) {
            queue.add(nbr);
          }
        }
      }
    }
  }

  private static <A, B> void VisitPart(int[] parts, Type[] types, int part, A atom, BaseMol<A, B> mol)
  {
    A next;
    do {
      next = null;
      for (B bond : mol.getBonds(atom)) {
        if (!mol.isInRing(bond))
          continue;
        A   nbr  = mol.getOther(bond, atom);
        int aidx = mol.getAtomIdx(nbr);
        if (parts[aidx] == 0 && types[aidx] != Type.Other) {
          parts[aidx] = part;
          if (next != null)
            VisitPart(parts, types, part, nbr, mol);
          else
            next = nbr;
        }
      }
      atom = next;
    } while (atom != null);
  }

  public static <A, B> int VisitParts(int[] parts, Type[] types, BaseMol<A, B> mol)
  {
    int numparts = 0;
    for (A atom : mol.atoms()) {
      int aidx = mol.getAtomIdx(atom);
      if (parts[aidx] == 0 && types[aidx] != Type.Other) {
        parts[aidx] = ++numparts;
        VisitPart(parts, types, parts[aidx], atom, mol);
      }
    }
    return numparts;
  }

  public static <A, B> Fraction[] CalcFracAtomNums(BaseMol<A, B> mol)
  {
    Fraction[] fractions = new Fraction[mol.getNumAtoms()];

    for (int i = 0; i < mol.getNumAtoms(); i++)
      fractions[i] = new Fraction(mol.getAtomicNum(mol.getAtom(i)), 1);

    Type[] types = new Type[mol.getNumAtoms()];
    if (Mancude.SeedTypes(types, mol)) {
      Mancude.RelaxTypes(types, mol);

      int[] parts    = new int[mol.getNumAtoms()];
      int   numparts = Mancude.VisitParts(parts, types, mol);

      int[] resparts = new int[numparts];
      int   numres   = 0;

      if (numparts > 0) {
        for (int i = 0; i < mol.getNumAtoms(); i++) {
          if (parts[i] == 0)
            continue;
          A atom = mol.getAtom(i);

          if (types[i] == Type.Cv3D3Minus ||
              types[i] == Type.Nv2D2Minus) {
            int j = 0;
            for (; j < numres; j++)
              if (resparts[j] == parts[i])
                break;
            if (j >= numres)
              resparts[numres++] = parts[i];
          }

          fractions[i].num = 0;
          fractions[i].den = 0;
          for (B bond : mol.getBonds(atom)) {
            A nbr = mol.getOther(bond, atom);
            if (parts[mol.getAtomIdx(nbr)] == parts[i]) {
              fractions[i].num += mol.getAtomicNum(nbr);
              fractions[i].den++;
            }
          }
        }
      }

      if (numres > 0) {
        for (int j = 0; j < numres; j++) {
          Fraction frac = new Fraction(0, 0);
          int part = resparts[j];
          for (int i = 0; i < mol.getNumAtoms(); i++) {
            if (parts[i] == part) {
              fractions[i] = frac;
              frac.den++;
              A atom = mol.getAtom(i);
              for (B bond : mol.getBonds(atom)) {
                A   nbr  = mol.getOther(bond, atom);
                int bord = mol.getBondOrder(bond);
                if (bord > 1 &&
                    parts[mol.getAtomIdx(nbr)] == part) {
                  frac.num += (bord - 1) * mol.getAtomicNum(nbr);
                }
              }
            }
          }
        }
      }
    }

    return fractions;
  }

  public static class Fraction implements Comparable<Fraction> {
    private int num;
    private int den;

    public Fraction(int num, int den)
    {
      this.num = num;
      this.den = den;
    }

    @Override
    public String toString()
    {
      return num + "/" + den + " (" + (num / (double) den) + ")";
    }

    @Override
    public int compareTo(Fraction o)
    {
      return compare(this.num, this.den, o.num, o.den);
    }

    public static int compare(int anum, int aden, int bnum, int bden)
    {
      return Double.compare(anum / (double) aden, bnum / (double) bden);
    }

    public int getNum()
    {
      return num;
    }

    public int getDen()
    {
      return den;
    }
  }
}
