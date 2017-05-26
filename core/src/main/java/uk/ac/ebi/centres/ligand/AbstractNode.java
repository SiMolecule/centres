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

import com.google.common.collect.Sets;
import org.openscience.cdk.interfaces.IAtom;
import uk.ac.ebi.centres.ConnectionProvider;
import uk.ac.ebi.centres.Descriptor;
import uk.ac.ebi.centres.Node;
import uk.ac.ebi.centres.MutableDescriptor;
import uk.ac.ebi.centres.graph.Edge;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author John May
 */
public abstract class AbstractNode<A> implements Node<A> {

  private Descriptor auxiliary = Descriptor.Unknown;
  private       ConnectionProvider<A> provider;
  private final Set<A>                visited;
  private final MutableDescriptor     descriptor;
  private final int                   distance;
  private       boolean               duplicate;
  private       List<Node<A>>         nodes;
  private       Descriptor            descriptorCache;
  private Set<Class<?>> orderedBy = new HashSet<Class<?>>();


  public AbstractNode(ConnectionProvider<A> provider,
                      Set<A> visited,
                      MutableDescriptor descriptor,
                      int distance)
  {

    this.provider = provider;
    this.descriptor = descriptor;
    this.distance = distance;

    // optimise size for a load factor of 0.75
    this.visited = Sets.newHashSet(visited);

  }


  public AbstractNode(Set<A> visited,
                      MutableDescriptor descriptor,
                      int distance)
  {

    this.descriptor = descriptor;
    this.distance = distance;

    // optimise size for a load factor of 0.75
    this.visited = Sets.newHashSet(visited);

  }


  public AbstractNode(MutableDescriptor descriptor,
                      int distance)
  {

    this.descriptor = descriptor;
    this.distance = distance;

    this.visited = Collections.EMPTY_SET;

  }


  public boolean isDuplicate()
  {
    return duplicate;
  }


  public void setDuplicate(boolean duplicate)
  {
    this.duplicate = duplicate;
  }


  public ConnectionProvider<A> getProvider()
  {
    return provider;
  }


  public void setProvider(ConnectionProvider<A> provider)
  {
    this.provider = provider;
  }


  @Override
  public Boolean isVisited(A atom)
  {
    return visited.contains(atom);
  }


  @Override
  public Set<A> getVisited()
  {
    return visited;
  }


  @Override
  public void setDescriptor(Descriptor descriptor)
  {
    this.descriptor.set(descriptor);
  }


  @Override
  public Descriptor getDescriptor()
  {
    if (descriptorCache == null) {
      Descriptor descriptor = this.descriptor.get();
      if (descriptor == Descriptor.None)  // cache access to NONE descriptors
        descriptorCache = descriptor;
      return descriptor;
    }
    return descriptorCache;
  }


  /**
   * @inheritDoc
   */
  @Override
  public List<Node<A>> getNodes()
  {
    if (nodes == null)
      nodes = provider.getLigands(this);
    return nodes;
  }

  public void reset()
  {
    nodes = null;
  }


  @Override
  public List<Edge<A>> getArcs()
  {
    return provider.getArcs(this);
  }


  @Override
  public Edge<A> getParentArc()
  {
    return provider.getParentArc(this);
  }


  @Override
  public int getDistanceFromRoot()
  {
    return distance;
  }


  /**
   * @inheritDoc
   */
  @Override
  public Descriptor getAuxiliary()
  {
    return auxiliary;
  }


  /**
   * @inheritDoc
   */
  @Override
  public void setAuxiliary(Descriptor descriptor)
  {
    this.auxiliary = descriptor;
  }


  @Override
  public int getDepth()
  {
    Edge<A> edge = getParentArc();
    return edge == null ? 0 : edge.getDepth();
  }


  @Override
  public boolean isBranching()
  {
    return Boolean.FALSE;
  }


  @Override
  public boolean isTerminal()
  {
    return Boolean.FALSE;
  }

  @Override
  public void markOrderedBy(Class<?> rule)
  {
    orderedBy.add(rule);
  }

  @Override
  public boolean isOrderedBy(Class<?> rule)
  {
    return false; // return orderedBy.contains(rule);
  }

  @Override
  public void clearOrderedBy()
  {
    orderedBy.clear();
  }

  @Override
  public String toString()
  {
    IAtom iatom = (IAtom) getAtom();
    int   num   = (iatom.getProperty("num", Integer.class) + 1);
    return iatom.getSymbol()
                .toLowerCase() + num + (isDuplicate() ? "_dup" : "") + "_" + System.identityHashCode(this) + (getDescriptor() != Descriptor.Unknown ? "_" + getDescriptor() : "") + (getAuxiliary() != Descriptor.Unknown ? "_" + getAuxiliary() + "aux" : "");
  }
}
