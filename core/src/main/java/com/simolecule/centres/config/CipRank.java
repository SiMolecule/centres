/*
 * Copyright (c) 2026 John Mayfield
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

import java.util.Objects;

/**
 * The class captures a CIP-rank and an optional prime indication. This is
 * useful for inorganic configurations with bidentate and tridentate ligands.
 */
final class CipRank implements Comparable<CipRank> {
  private final int rank;
  private final int prime;

  CipRank(int rank, Integer prime) {
    this.rank = rank;
    this.prime = prime != null ? prime : 0;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    CipRank rank = (CipRank) o;
    return this.rank == rank.rank && prime == rank.prime;
  }

  @Override
  public int hashCode() {
    return Objects.hash(rank, prime);
  }

  @Override
  public int compareTo(CipRank o) {
    int cmp = Integer.compare(rank, o.rank);
    if (cmp != 0)
      return cmp;
    return Integer.compare(prime, o.prime);
  }

  @Override
  public String toString() {
    switch (prime) {
      case 1:
        return rank + "′";
      case 2:
        return rank + "ʺ";
      case 3:
        return rank + "‴";
      case 4:
        return rank + "⁗";
      case 0:
        return Integer.toString(rank);
      default:
        return rank + "?";
    }
  }
}
