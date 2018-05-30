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

    // Stats.INSTANCE.countNumCenters(configs.size());

    Map<Configuration<A, B>, Descriptor> finalLabels = new HashMap<>();
    for (Configuration<A, B> conf : configs) {
      conf.setDigraph(new Digraph<>(mol));
      try {
        Descriptor desc = conf.label(begRules);
        if (desc != null && desc != Descriptor.Unknown) {
          conf.setPrimaryLabel(mol, desc);
        } else {
          if (labelAux(configs, allRules, conf)) {

            //  Stats.INSTANCE.numAuxCalculated.incrementAndGet();
            desc = conf.label(allRules);

            if (desc != null && desc != Descriptor.Unknown) {
              //      Stats.INSTANCE.numAuxLabelled.incrementAndGet();
              conf.setPrimaryLabel(mol, desc);
            }
          }
        }
      } catch (Exception e) {
        System.err.println(e.getMessage());
//        throw new RuntimeException(e);
      }
      //Stats.INSTANCE.measureDigraph(conf.getDigraph());
    }
    //Stats.INSTANCE.numConfigLabelled.addAndGet(finalLabels.size());
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
