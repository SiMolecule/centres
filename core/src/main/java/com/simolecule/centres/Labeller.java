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

import com.simolecule.centres.config.Configuration;
import com.simolecule.centres.rules.*;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Labeller<A, B> {

  public void label(BaseMol<A, B> mol, List<Configuration<A, B>> configs) {
    // constitutional rules
    final Rules<A, B> begRules = new Rules<>(new Rule1a<>(mol),
                                             new Rule1b<>(mol),
                                             new Rule2<>(mol)
    );
    // all rules (require aux calc)
    final Rules<A, B> allRules = new Rules<>(new Rule1a<>(mol),
                                             new Rule1b<>(mol),
                                             new Rule2<>(mol),
                                             new Rule3<>(mol),
                                             new Rule4a<>(mol),
                                             new Rule4b<>(mol),
                                             new Rule4c<>(mol),
                                             new Rule5New<>(mol),
                                             new Rule6<>(mol)
    );

    for (Configuration<A, B> conf : configs) {
      conf.setDigraph(new Digraph<>(mol));
      try {
        Descriptor desc = conf.label(begRules);
        if (desc != null && desc != Descriptor.Unknown) {
          conf.setPrimaryLabel(mol, desc);
        } else {
          if (labelAux(configs, allRules, conf)) {
            desc = conf.label(allRules);
            if (desc != null && desc != Descriptor.Unknown) {
              conf.setPrimaryLabel(mol, desc);
            }
          }
        }
      } catch (Exception e) {
        System.err.println(e.getMessage());
      }
    }
  }

  private boolean labelAux(List<Configuration<A, B>> configs,
                           Rules<A, B> rules,
                           Configuration<A, B> center) {
    List<Map.Entry<Node<A, B>, Configuration<A, B>>> aux = new ArrayList<>();

    Digraph<A, B> digraph = center.getDigraph();
    for (Configuration<A, B> config : configs) {
      if (config.equals(center))
        continue;
      // FIXME: specific to each descriptor
      A[] foci = config.getFoci();
      for (Node<A, B> node : digraph.getNodes(foci[0])) {
        if (node.isDuplicate())
          continue;
        Node<A, B> low = node;
        if (foci.length == 2) {
          for (Edge<A, B> edge : node.getEdges(foci[1])) {
            if (edge.getOther(node).getDistance() < node.getDistance())
              low = edge.getOther(node);
          }
        }
        if (!low.isDuplicate())
          aux.add(new AbstractMap.SimpleImmutableEntry<>(low, config));
      }
    }

    Collections.sort(aux,
                     new Comparator<Map.Entry<Node<A, B>, Configuration<A, B>>>() {
                       @Override
                       public int compare(
                               Map.Entry<Node<A, B>, Configuration<A, B>> a,
                               Map.Entry<Node<A, B>, Configuration<A, B>> b) {
                         return -Integer.compare(a.getKey().getDistance(),
                                                 b.getKey().getDistance());
                       }
                     });

    Map<Node<A, B>, Descriptor> queue = new HashMap<>();
    int                         prev  = Integer.MAX_VALUE;
    for (Map.Entry<Node<A, B>, Configuration<A, B>> e : aux) {
      Node<A, B> node = e.getKey();

      if (node.getDistance() < prev) {
        for (Map.Entry<Node<A, B>, Descriptor> e2 : queue.entrySet())
          e2.getKey().setAux(e2.getValue());
        queue.clear();
        prev = node.getDistance();
      }
      Configuration<A, B> config = e.getValue();
      Descriptor          label  = config.label(node, digraph, rules);
      queue.put(node, label);
    }

    for (Map.Entry<Node<A, B>, Descriptor> e : queue.entrySet())
      e.getKey().setAux(e.getValue());

    return true;
  }
}
