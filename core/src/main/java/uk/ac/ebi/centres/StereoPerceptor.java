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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package uk.ac.ebi.centres;

import com.simolecule.centres.MolApi;
import uk.ac.ebi.centres.priority.Rule1a;
import uk.ac.ebi.centres.priority.Rule1b;
import uk.ac.ebi.centres.priority.Rule2;
import uk.ac.ebi.centres.priority.Rule3;
import uk.ac.ebi.centres.priority.Rule4b;
import uk.ac.ebi.centres.priority.Rule4c;
import uk.ac.ebi.centres.priority.Rule5;
import uk.ac.ebi.centres.priority.Rules;
import uk.ac.ebi.centres.priority.access.descriptor.PrimaryDescriptor;
import uk.ac.ebi.centres.priority.access.descriptor.PrimaryOrAuxiliary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author John May
 */
public class StereoPerceptor<M, A, B> {

  private final CentrePerceptor<A> mainPerceptor;
  private final CentrePerceptor<A> auxPerceptor;
  private final MolApi<M, A, B> api;

  public StereoPerceptor(final MolApi<M, A, B> api)
  {

    this.api = api;

    final Rules<A> rule = new Rules<A>(
            new Rule1a<A>(api),
            new Rule1b<A>(),
            new Rule2<A>(api),
            new Rule3<A>(),
            new Rule4b<A>(new PrimaryDescriptor<A>()),
            new Rule4c<A>(new PrimaryDescriptor<A>()),
            new Rule5<A>(new PrimaryDescriptor<A>())
    );
    final Rules<A> auxRule = new Rules<A>(
            new Rule1a<A>(api),
            new Rule1b<A>(),
            new Rule2<A>(api),
            new Rule3<A>(),
            new Rule4b<A>(new PrimaryOrAuxiliary<A>()),
            new Rule4c<A>(new PrimaryOrAuxiliary<A>()),
            new Rule5<A>(new PrimaryOrAuxiliary<A>())
    );

    // create the main and aux perceptors
    this.mainPerceptor = new CentrePerceptor<A>(rule) {
      @Override
      public Descriptor perceive(Centre<A> centre, Collection<Centre<A>> centres)
      {
        return centre.perceive(rule);
      }
    };
    this.auxPerceptor = new CentrePerceptor<A>(auxRule) {
      @Override
      public Descriptor perceive(Centre<A> centre, Collection<Centre<A>> centres)
      {
        System.err.println("Perceive aux");
        if (centre.perceiveAuxiliary(centres, rule) > 0) {
          System.err.println("Using Aux for...");
          // only attempt re-perception if there were auxiliary labels defined
          return centre.perceive(auxRule);
        }
        return Descriptor.Unknown;
      }
    };
  }


  private List<Centre<A>> _perceive(Collection<Centre<A>> unperceived,
                                    CentrePerceptor<A> perceptor,
                                    CentrePerceptor<A> auxperceptor)
  {

    List<Centre<A>>            perceived = new ArrayList<Centre<A>>();
    Map<Centre<A>, Descriptor> map       = new LinkedHashMap<Centre<A>, Descriptor>();

    OUTER:
    do {
      do {
        map.clear();

        for (Centre<A> centre : unperceived) {
          Descriptor descriptor = perceptor.perceive(centre, unperceived);
          if (descriptor != Descriptor.Unknown)
            map.put(centre, descriptor);
        }
        transferLabels(unperceived, perceived, map);
        if (unperceived.isEmpty())
          break OUTER;
      } while (!map.isEmpty());

      if (!unperceived.isEmpty()) {
        for (Centre<A> centre : unperceived) {
          Descriptor descriptor = auxperceptor.perceive(centre, unperceived);
          if (descriptor != Descriptor.Unknown)
            map.put(centre, descriptor);
        }
        transferLabels(unperceived, perceived, map);
      }
    } while (!map.isEmpty());

    return perceived;

  }

  private void transferLabels(Collection<Centre<A>> unperceived, List<Centre<A>> perceived,
                              Map<Centre<A>, Descriptor> map)
  {
    // transfer descriptors
    for (Map.Entry<Centre<A>, Descriptor> entry : map.entrySet()) {
      unperceived.remove(entry.getKey());
      perceived.add(entry.getKey());
      entry.getKey().dispose();
      entry.getKey().setDescriptor(entry.getValue());
    }
  }


  public void perceive(final CentreProvider<A> provider, final DescriptorManager<A> manager)
  {

    // timeout fo the centre provider incase we have a huge molecule and the spanning tree can't
    // be constructed
    Collection<Centre<A>> unperceived = provider.getCentres(manager);

    if (unperceived.isEmpty())
      return;

    _perceive(unperceived, mainPerceptor, auxPerceptor);

    // set all unperceived centres to 'none'
    for (Centre<A> centre : unperceived) {
      centre.setDescriptor(Descriptor.None);
      centre.dispose();
    }

    unperceived.clear();
    unperceived = null;
    manager.clear();

  }

  abstract class CentrePerceptor<A> {

    private PriorityRule<A> rule;


    protected CentrePerceptor(PriorityRule<A> rule)
    {
      this.rule = rule;
    }


    public abstract Descriptor perceive(Centre<A> centre, Collection<Centre<A>> centres);
  }


}
