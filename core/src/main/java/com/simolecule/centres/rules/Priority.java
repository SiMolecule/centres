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

package com.simolecule.centres.rules;

import java.util.Set;

/**
 * Holds some properties that are determined when sorting/prioritising ligands.
 *
 * @author John May
 */
public class Priority {

  private Boolean           unique;
  private boolean           pseudoAsym;
  private Set<Set<Integer>> duplicates;
  private int               ruleIdx;


  public Priority(Boolean unique, int ruleIdx, boolean pseudoAsym)
  {
    this.unique = unique;
    this.pseudoAsym = pseudoAsym;
    this.ruleIdx = ruleIdx;
  }

  public Priority(Boolean unique, boolean pseudoAsym, Set<Set<Integer>> duplicates)
  {
    this.unique = unique;
    this.pseudoAsym = pseudoAsym;
    this.duplicates = duplicates;
  }


  /**
   * Indicates whether the ligands were unique (i.e. could be ordered)
   *
   * @return whether the ligands were unique
   */
  public Boolean isUnique()
  {
    return unique;
  }

  public int getRuleIdx()
  {
    return ruleIdx;
  }

  /**
   * Indicates the descriptor type used to. This allows methods that represent
   * pseudo-asymmetric molecules to indicate that the centre is
   * pseudo-asymmetric.
   *
   * @return The type of the descriptor that should be assigned
   */
  public boolean isPseduoAsymettric()
  {
    return pseudoAsym;
  }

//  public <A> List<List<Node<A>>> createBins(List<Node<A>> nodes)
//  {
//    if (duplicates == null)
//      throw new IllegalArgumentException("No duplicates stored at time of sort!");
//
//    List<List<Node<A>>> bins = new ArrayList<List<Node<A>>>(nodes.size());
//
//    // now need to place in bins
//    for (int i = 0; i < nodes.size(); i++) {
//      List<Node<A>> bin = new ArrayList<Node<A>>();
//      bin.add(nodes.get(0));
//      bins.add(bin);
//    }
//
//    Set<Integer> removed = new HashSet<Integer>();
//    // and compact (could be doing something wrong
//    for (Set<Integer> pair : duplicates) {
//      Iterator<Integer> it = pair.iterator();
//      int               i  = it.next();
//      int               j  = it.next();
//      if (!removed.contains(i) || !removed.contains(j)) {
//        bins.get(i).addAll(bins.get(j));
//        removed.add(j);
//      }
//    }
//    for (Integer remove : removed)
//      bins.remove(remove);
//
//    return bins;
//
//  }

}
