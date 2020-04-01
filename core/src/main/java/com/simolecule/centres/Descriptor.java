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
  ns,
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
