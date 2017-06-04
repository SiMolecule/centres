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

package uk.ac.ebi.centres.ligand;

import uk.ac.ebi.centres.Centre;
import uk.ac.ebi.centres.ConnectionTable;
import uk.ac.ebi.centres.Descriptor;
import uk.ac.ebi.centres.Node;
import uk.ac.ebi.centres.MutableDescriptor;
import uk.ac.ebi.centres.Priority;
import uk.ac.ebi.centres.PriorityRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author John May
 */
public class Tetrahedral<A> extends AbstractNode<A> implements Centre<A> {

  public static final int Left  = 0x1;
  public static final int Right = 0x2;

  private final A   focus;
  private       A   parent;
  private final A[] carriers;
  private final int config;

  public Tetrahedral(MutableDescriptor descriptor,
                     A focus,
                     A[] carriers,
                     int config)
  {
    super(descriptor, 0);
    if (carriers.length != 4)
      throw new IllegalArgumentException("Expected 4 carriers for tetrahedral centre!");
    if ((config & 0x3) == 0)
      throw new IllegalArgumentException("Tetrahedral centre does not have a configuration");
    this.config = config;
    this.focus = focus;
    this.parent = focus;
    this.carriers = Arrays.copyOf(carriers, carriers.length);
  }


  @Override
  public A getAtom()
  {
    return focus;
  }


  @Override
  public void setParent(A atom)
  {
    // don't have a parent here
    this.parent = atom;
  }


  @Override
  public A getParent()
  {
    return this.parent;
  }


  @Override
  public Set<A> getFoci()
  {
    return Collections.singleton(focus);
  }


  @Override
  public int perceiveAuxiliary(Collection<Centre<A>> centres,
                               PriorityRule<A> rule)
  {

    Map<Node<A>, uk.ac.ebi.centres.Descriptor> auxiliary = new HashMap<Node<A>, uk.ac.ebi.centres.Descriptor>(centres.size());
    Set<Node<A>>                               done      = new HashSet<Node<A>>();

    // ensure the entire digraph is built
    getProvider().build();

    for (Node<A> node : getProvider().ligands()) {
      node.setAuxiliary(Descriptor.Unknown);
    }

    int size = 0;

    do {
      auxiliary.clear();
      for (Centre<A> centre : centres) {

        // don't do aux perception on self
        if (centre == this)
          continue;

        // can only reroot on single atom centres
        if (centre.getFoci().size() == 1) {

          for (Node<A> node : getProvider().ligandInstancesForAtom(centre.getAtom())) {

            if (done.contains(node)) continue;

            getProvider().reroot(node);

            Descriptor descriptor = centre.perceive(getProvider().getLigands(node),
                                                    rule);

            if (descriptor != Descriptor.Unknown) {
              auxiliary.put(node, descriptor);
              done.add(node);
            }

          }
        }
      }

      // transfer auxiliary descriptors to their respective ligands
      for (Map.Entry<Node<A>, uk.ac.ebi.centres.Descriptor> entry : auxiliary.entrySet())
        entry.getKey().setAuxiliary(entry.getValue());
      size += auxiliary.size();

    } while (!auxiliary.isEmpty());

    // reroot on this
    getProvider().reroot(this);

    return size;

  }

  @Override
  public uk.ac.ebi.centres.Descriptor perceive(List<Node<A>> proximal, PriorityRule<A> rule)
  {

    if (proximal.size() < 3) {
      return Descriptor.None;
    }

    Priority priority = rule.prioritise(proximal);

    if (priority.isUnique()) {

      // remove any H that were added with null coordinates
      proximal = filterAddedHydrogens(proximal);

      if (proximal.size() < 3) {
        return Descriptor.None;
      }

      A[] ordered = carriers.clone();
      ordered[0] = proximal.get(0).getAtom();
      ordered[1] = proximal.get(1).getAtom();
      ordered[2] = proximal.get(2).getAtom();
      if (proximal.size() == 4)
        ordered[3] = proximal.get(3).getAtom();
      else
        ordered[3] = focus;

      final int parity = Parity.parity4(ordered, carriers);


      if (parity == 0)
        throw new RuntimeException("Could not calculate parity! Carrier mismatch");

      // inverted?
      int config = this.config;
      if (parity == 1)
        config ^= 0x3;

      if (config == 0x1) {
        if (priority.isPseduoAsymettric())
          return Descriptor.s;
        else
          return Descriptor.S;
      } else if (config == 0x2) {
        if (priority.isPseduoAsymettric())
          return Descriptor.r;
        else
          return Descriptor.R;
      }
    }

    return Descriptor.Unknown;
  }

  private List<Node<A>> filterAddedHydrogens(List<Node<A>> proximal)
  {
    List<Node<A>> filtered = new ArrayList<Node<A>>();

    ConnectionTable<A> ctab = getProvider().getCtab();

    // remove hydrogen and 'duplicated' double-bond atoms (i.e. sulfoxide)
    for (Node<A> node : new ArrayList<Node<A>>(proximal)) {
      if (!ctab.isExpandedHydrogen(node.getAtom())) {
        filtered.add(node);
      }
    }
    return filtered;
  }

  @Override
  public Descriptor perceive(PriorityRule<A> rule)
  {
    return perceive(getNodes(), rule);
  }

  @Override
  public Boolean isParent(A atom)
  {
    return parent.equals(atom);
  }

  @Override
  public void dispose()
  {
    getProvider().dispose();
    setProvider(null);
  }
}
