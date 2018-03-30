/*
 * Copyright (c) 2012. John May
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.simolecule.centres;

/**
 * Defines a descriptor which can be assigned to an atom to indicate the type of
 * chirality (if there is any). Each descriptor defines it's general @{link
 * Type} which can be useful when comparing centres of different geometry.
 *
 * @author John May
 */
public enum Descriptor {
  /**
   * Unknown/Unspecified
   */
  Unknown,
  /**
   * Other
   */
  None,
  /**
   * Tetrahedral
   */
  R,
  S,
  r,
  s,
  /**
   * Cis/Trans
   */
  seqTrans,
  seqCis,
  E,
  Z,
  /* Axial */
  M,
  P,
  m,
  p,

  SP_4,
  TBPY_5,
  OC_6;

  boolean isPseudoAsymmetric()
  {
    switch (this) {
      case r:
      case s:
      case seqCis:
      case seqTrans:
      case m:
      case p:
        return true;
      default:
        return false;
    }
  }

  public static Descriptor parse(String str)
  {
    switch (str) {
      case "R":
        return R;
      case "S":
        return S;
      case "r":
        return r;
      case "s":
        return s;
      case "M":
        return M;
      case "P":
        return P;
      case "m":
        return m;
      case "p":
        return p;
      case "E":
        return E;
      case "Z":
        return Z;
      case "seqTrans":
        return seqTrans;
      case "seqCis":
        return seqCis;
      case "U":
        return Unknown;
      default:
        throw new IllegalArgumentException("Unknown descriptor label: " + str);
    }
  }
}
