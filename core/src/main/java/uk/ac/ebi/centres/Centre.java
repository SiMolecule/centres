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

package uk.ac.ebi.centres;

/**
 * Defines a stereo centre (normally on an atom or bond) that provides access
 * and mutation of the centres descriptor. This centre could plug directly into
 * the molecular object (atom or bond) but would normally be a wrapper around
 * the molecular object which can then be transferred when all centres that can
 * be perceived, have been perceived.
 *
 * @author John May
 * @see Descriptor
 * @see uk.ac.ebi.centres.descriptor.General
 * @see uk.ac.ebi.centres.descriptor.Tetrahedral
 * @see uk.ac.ebi.centres.descriptor.Planar
 * @see uk.ac.ebi.centres.descriptor.Trigonal
 */
public interface Centre {

    /**
     * Sets the descriptor for this centre. This method will throw an illegal
     * argument exception if the descriptor is set to null.
     *
     * @param descriptor the new descriptor for this centre
     *
     * @see uk.ac.ebi.centres.descriptor.General
     * @see uk.ac.ebi.centres.descriptor.Tetrahedral
     * @see uk.ac.ebi.centres.descriptor.Planar
     * @see uk.ac.ebi.centres.descriptor.Trigonal
     */
    public void setDescriptor(Descriptor descriptor);

    /**
     * Access the descriptor for this centre. This descriptor is the primary
     * descriptor for this centre and not an auxiliary descriptor. Auxiliary
     * descriptors should be set on a per ligand basis. This method should not
     * return null and instead return {@link uk.ac.ebi.centres.descriptor.General#UNKNOWN}
     * for unknown/not yet determined centres.
     *
     * @return descriptor for this centre
     *
     * @see uk.ac.ebi.centres.descriptor.General
     * @see uk.ac.ebi.centres.descriptor.Tetrahedral
     * @see uk.ac.ebi.centres.descriptor.Planar
     * @see uk.ac.ebi.centres.descriptor.Trigonal
     */
    public Descriptor getDescriptor();

}
