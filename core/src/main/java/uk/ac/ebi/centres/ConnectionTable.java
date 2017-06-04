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

import java.util.Collection;

/**
 * @author John May
 */
public interface ConnectionTable<A> {

    public void addExpandedHydrogen(A atom);

    public boolean isExpandedHydrogen(A atom);

    public Collection<A> getConnected(A atom);

    public Integer getDepth(A first, A second);

    public int getOrder(A first, A second);


    public Integer getAtomCount();



}
