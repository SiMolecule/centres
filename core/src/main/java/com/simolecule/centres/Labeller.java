package com.simolecule.centres;

import com.simolecule.centres.config.Configuration;
import com.simolecule.centres.rules.Rule1a;
import com.simolecule.centres.rules.Rule1b;
import com.simolecule.centres.rules.Rule2;
import com.simolecule.centres.rules.Rule3;
import com.simolecule.centres.rules.Rule4a;
import com.simolecule.centres.rules.Rule4b;
import com.simolecule.centres.rules.Rule4c;
import com.simolecule.centres.rules.Rule5;
import com.simolecule.centres.rules.Rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Labeller<A, B> {

  private static boolean labelIndependently = true;

  public void label(BaseMol<A, B> mol, List<Configuration<A, B>> configs)
  {
    if (labelIndependently)
      labelIndependent(mol, configs);
    else
      labelIterative(mol, configs);
  }

  public void labelIndependent(BaseMol<A, B> mol, List<Configuration<A, B>> configs)
  {
    // constitutional rules
    final Rules<A, B> begRules = new Rules<A, B>(new Rule1a<A, B>(mol),
                                                   new Rule1b<A, B>(mol),
                                                   new Rule2<A, B>(mol));
    // all rules
    final Rules<A, B> allRules = new Rules<A, B>(new Rule1a<A, B>(mol),
                                                 new Rule1b<A, B>(mol),
                                                 new Rule2<A, B>(mol),
                                                 new Rule3<A, B>(mol),
                                                 new Rule4a<A, B>(mol),
                                                 new Rule4b<A, B>(mol),
                                                 new Rule4c<A, B>(mol),
                                                 new Rule5<A, B>(mol));

    Map<Configuration<A,B>,Descriptor> finalLabels = new HashMap<>();
    for (Configuration<A,B> conf : configs) {
      conf.setDigraph(new Digraph<A, B>(mol));
      Descriptor desc = conf.label(begRules);
      if (desc != null && desc != Descriptor.Unknown) {
        finalLabels.put(conf, desc);
      } else {
        Map<Node<A,B>,Descriptor> auxLabels = new HashMap<>();
        for (Configuration<A,B> confAux : configs) {
          if (confAux.equals(conf))
            continue;
          confAux.labelAux(auxLabels, conf.getDigraph(), begRules);
        }
        if (!auxLabels.isEmpty()) {
          setAuxLabels(auxLabels);
          desc = conf.label(allRules);
          if (desc != null && desc != Descriptor.Unknown)
            finalLabels.put(conf, desc);
        }
      }
    }
    setFinalLabels(mol, finalLabels);
  }

  public void labelIterative(BaseMol<A, B> mol, List<Configuration<A, B>> configs)
  {

    // set primary digraphs
    for (Configuration<A, B> config : configs)
      config.setDigraph(new Digraph<A, B>(mol));

    final Rules<A, B> rules = new Rules<A, B>(new Rule1a<A, B>(mol),
                                              new Rule1b<A, B>(mol),
                                              new Rule2<A, B>(mol),
                                              new Rule3<A, B>(mol),
                                              new Rule4a<A, B>(mol),
                                              new Rule4b<A, B>(mol),
                                              new Rule4c<A, B>(mol),
                                              new Rule5<A, B>(mol)
    );

    List<Configuration<A, B>>            unspec = new ArrayList<>();
    Map<Configuration<A, B>, Descriptor> map    = new HashMap<>();
    unspec.addAll(configs);

    do {
      do {
        map.clear();

        for (Configuration<A, B> cfg : unspec) {
          Descriptor desc = cfg.label(rules);
          if (desc != Descriptor.Unknown) {
            map.put(cfg, desc);
          }
        }

        setFinalLabels(mol, map);
        unspec.removeAll(map.keySet());

      } while (!unspec.isEmpty() && !map.isEmpty());

      // use auxiliary preceptors
      if (!unspec.isEmpty()) {
        for (Configuration<A, B> config : unspec) {

          Map<Node<A, B>, Descriptor> aux = new HashMap<>();
          for (Configuration<A, B> other : unspec) {
            if (other == config)
              continue;
            other.labelAux(aux, config.getDigraph(), rules);
          }
          if (!aux.isEmpty()) {
            setAuxLabels(aux);
            Descriptor desc = config.label(rules);
            if (desc != Descriptor.Unknown) {
              map.put(config, desc);
            }
          }
        }

        setFinalLabels(mol, map);
        unspec.removeAll(map.keySet());
      }

    } while (!unspec.isEmpty() && !map.isEmpty());
  }

  private void setFinalLabels(BaseMol<A, B> mol, Map<Configuration<A, B>, Descriptor> map)
  {
    for (Map.Entry<Configuration<A, B>, Descriptor> e : map.entrySet())
      e.getKey().setPrimaryLabel(mol, e.getValue());
  }

  private void setAuxLabels(Map<Node<A, B>, Descriptor> map)
  {
    for (Map.Entry<Node<A, B>, Descriptor> e : map.entrySet())
      e.getKey().setAux(e.getValue());
  }
}
