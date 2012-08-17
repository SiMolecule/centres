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

/**
 * Holds some properties that are determined when sorting/prioritising ligands.
 *
 * @author John May
 */
public class Priority {

    private Boolean         unique;
    private Descriptor.Type type;


    public Priority(Boolean unique, Descriptor.Type type) {
        this.unique = unique;
        this.type = type;
    }


    /**
     * Indicates whether the ligands were unique (i.e. could be ordered)
     *
     * @return whether the ligands were unique
     */
    public Boolean isUnique() {
        return unique;
    }


    /**
     * Indicates the descriptor type used to. This allows methods that represent
     * pseudo-asymmetric molecules to indicate that the centre is
     * pseudo-asymmetric.
     *
     * @return The type of the descriptor that should be assigned
     */
    public Descriptor.Type getType() {
        return type;
    }

}
