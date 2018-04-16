package com.simolecule.centres;

import com.simolecule.centres.config.Configuration;
import com.simolecule.centres.rules.Rule1a;
import com.simolecule.centres.rules.Rule1b;
import com.simolecule.centres.rules.Rule2;
import com.simolecule.centres.rules.Rule3;
import com.simolecule.centres.rules.Rule4a;
import com.simolecule.centres.rules.Rule4bNew;
import com.simolecule.centres.rules.Rule4c;
import com.simolecule.centres.rules.Rule5;
import com.simolecule.centres.rules.Rules;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Labeller<A, B> {

  public void label(BaseMol<A, B> mol, List<Configuration<A, B>> configs)
  {
    // constitutional rules
    final Rules<A, B> begRules = new Rules<A, B>(new Rule1a<A, B>(mol),
                                              new Rule1b<A, B>(mol),
                                              new Rule2<A, B>(mol)
    );
    // all rules
    final Rules<A, B> allRules = new Rules<A, B>(new Rule1a<A, B>(mol),
                                                 new Rule1b<A, B>(mol),
                                                 new Rule2<A, B>(mol),
                                                 new Rule3<A, B>(mol),
                                                 new Rule4a<A, B>(mol),
                                                 new Rule4bNew<A, B>(mol),
                                                 new Rule4c<A, B>(mol),
                                                 new Rule5<A, B>(mol)
    );

    // Stats.INSTANCE.countNumCenters(configs.size());

    Map<Configuration<A, B>, Descriptor> finalLabels = new HashMap<>();
    for (Configuration<A, B> conf : configs) {
      conf.setDigraph(new Digraph<A, B>(mol));
      Descriptor desc = conf.label(begRules);
      if (desc != null && desc != Descriptor.Unknown) {
        conf.setPrimaryLabel(mol, desc);
      } else {

        //System.out.println("C"+(mol.getAtomIdx(conf.getFocus())+1));
        if (labelAux(configs, allRules, conf)) {

        //  Stats.INSTANCE.numAuxCalculated.incrementAndGet();
          desc = conf.label(allRules);

          // System.out.println(mol.dumpDigraph(conf.getDigraph()));
          if (desc != null && desc != Descriptor.Unknown) {
      //      Stats.INSTANCE.numAuxLabelled.incrementAndGet();
            conf.setPrimaryLabel(mol, desc);
          }
        }
      }
      //Stats.INSTANCE.measureDigraph(conf.getDigraph());
    }
    //Stats.INSTANCE.numConfigLabelled.addAndGet(finalLabels.size());
  }

  private boolean labelAux(List<Configuration<A, B>> configs,
                           Rules<A, B> rules,
                           Configuration<A, B> center)
  {
    List<Map.Entry<Node<A,B>,Configuration<A,B>>> aux = new ArrayList<>();

    Digraph<A,B> digraph = center.getDigraph();
    for (Configuration<A,B> config : configs) {
      if (config.equals(center))
        continue;
      A[] foci = config.getFoci();
      for (Node<A, B> node : digraph.getNodes(foci[0])) {
        if (node.isDuplicate())
          continue;
        Node<A,B> low = node;
        if (foci.length == 2) {
          for (Edge<A,B> edge : node.getEdges(foci[1])) {
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

    for (Map.Entry<Node<A,B>,Configuration<A,B>> e : aux) {
      Node<A,B>          node   = e.getKey();
      Configuration<A,B> config = e.getValue();
      Descriptor         label  = config.label(node, digraph, rules);
      node.setAux(label);
    }
    return true;
  }
}
