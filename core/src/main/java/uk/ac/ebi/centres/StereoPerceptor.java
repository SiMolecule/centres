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

package uk.ac.ebi.centres;


/**
 * @author John May
 */
public class StereoPerceptor<M, A, B> {

//  private final CentrePerceptor<A> mainPerceptor;
//  private final CentrePerceptor<A> auxPerceptor;
//  private final BaseMol<A, B>      mol;
//
//  public StereoPerceptor(final BaseMol<A, B> mol)
//  {
//
//    this.mol = mol;
//
//    final Rules<A> rules = new Rules<A>(
//            new Rule1a<A>(mol),
//            new Rule1b<A>(),
//            new Rule2<A>(mol),
//            new Rule3<A>(),
//            new Rule4b<A>(new PrimaryDescriptor<A>()),
//            new Rule4c<A>(new PrimaryDescriptor<A>()),
//            new Rule5<A>(new PrimaryDescriptor<A>())
//    );
//    final Rules<A> auxRule = new Rules<A>(
//            new Rule1a<A>(mol),
//            new Rule1b<A>(),
//            new Rule2<A>(mol),
//            new Rule3<A>(),
//            new Rule4b<A>(new PrimaryOrAuxiliary<A>()),
//            new Rule4c<A>(new PrimaryOrAuxiliary<A>()),
//            new Rule5<A>(new PrimaryOrAuxiliary<A>())
//    );
//
//    // create the main and aux perceptors
//    this.mainPerceptor = new CentrePerceptor<A>(rules) {
//      @Override
//      public Descriptor perceive(Centre<A> centre, Collection<Centre<A>> centres)
//      {
//        return centre.perceive(rules);
//      }
//    };
//    this.auxPerceptor = new CentrePerceptor<A>(auxRule) {
//      @Override
//      public Descriptor perceive(Centre<A> centre, Collection<Centre<A>> centres)
//      {
//        if (centre.perceiveAuxiliary(centres, rules) > 0) {
//          // only attempt re-perception if there were auxiliary labels defined
//          return centre.perceive(auxRule);
//        }
//        return Descriptor.Unknown;
//      }
//    };
//  }
//
//
//  private List<Centre<A>> _perceive(Collection<Centre<A>> unperceived,
//                                    CentrePerceptor<A> perceptor,
//                                    CentrePerceptor<A> auxperceptor)
//  {
//
//    List<Centre<A>>            perceived = new ArrayList<Centre<A>>();
//    Map<Centre<A>, Descriptor> map       = new LinkedHashMap<Centre<A>, Descriptor>();
//
//    OUTER:
//    do {
//      do {
//        map.clear();
//
//        for (Centre<A> centre : unperceived) {
//          Descriptor descriptor = perceptor.perceive(centre, unperceived);
//          if (descriptor != Descriptor.Unknown)
//            map.put(centre, descriptor);
//        }
//        transferLabels(unperceived, perceived, map);
//        if (unperceived.isEmpty())
//          break OUTER;
//      } while (!map.isEmpty());
//
//      if (!unperceived.isEmpty()) {
//        for (Centre<A> centre : unperceived) {
//          Descriptor descriptor = auxperceptor.perceive(centre, unperceived);
//          if (descriptor != Descriptor.Unknown)
//            map.put(centre, descriptor);
//        }
//        transferLabels(unperceived, perceived, map);
//      }
//    } while (!map.isEmpty());
//
//    return perceived;
//
//  }
//
//  private void transferLabels(Collection<Centre<A>> unperceived, List<Centre<A>> perceived,
//                              Map<Centre<A>, Descriptor> map)
//  {
//    // transfer descriptors
//    for (Map.Entry<Centre<A>, Descriptor> entry : map.entrySet()) {
//      unperceived.remove(entry.getKey());
//      perceived.add(entry.getKey());
//      entry.getKey().dispose();
//      entry.getKey().setPrimaryLabel(entry.getValue());
//    }
//  }
//
//
//  public void perceive(final Collection<Centre<A>> centres, final DescriptorManager<A> manager)
//  {
//
//    // timeout fo the centre provider incase we have a huge molecule and the spanning tree can't
//    // be constructed
//    Collection<Centre<A>> unperceived = new ArrayList<>(centres);
//
//    if (unperceived.isEmpty())
//      return;
//
//    _perceive(unperceived, mainPerceptor, auxPerceptor);
//
//    // set all unperceived centres to 'none'
//    for (Centre<A> centre : unperceived) {
//      centre.setPrimaryLabel(Descriptor.Other);
//      centre.dispose();
//    }
//
//    unperceived.clear();
//    unperceived = null;
//    manager.clear();
//
//  }
//
//  abstract class CentrePerceptor<A> {
//
//    private SequenceRule<A> rules;
//
//
//    protected CentrePerceptor(SequenceRule<A> rules)
//    {
//      this.rules = rules;
//    }
//
//
//    public abstract Descriptor perceive(Centre<A> centre, Collection<Centre<A>> centres);
//  }


}
