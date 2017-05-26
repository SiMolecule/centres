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

package uk.ac.ebi.centres.calculator;

import uk.ac.ebi.centres.Node;

/**
 * @author John May
 */
public abstract class TwoDimensionalSignCalculator<A>
        extends AbstractSignCalculator<A> {


    @Override
    public int getSign(Node<A> centre, Node<A> a1, Node<A> a2, Node<A> a3, Node<A> a4) {

        // unspecified
        if (a1.getDepth() == 0 && a2.getDepth() == 0
                && a3.getDepth() == 0 && a4.getDepth() == 0)
            return 0;

        double[] a = normalise(toVector(centre.getAtom(), a1.getAtom()));
        double[] b = normalise(toVector(centre.getAtom(), a2.getAtom()));
        double[] c = normalise(toVector(centre.getAtom(), a3.getAtom()));
        double[] d = normalise(toVector(centre.getAtom(), a4.getAtom()));

        double[][] matrix = new double[][]{{a[0], a[1], 1, a1.getDepth()},
                                           {b[0], b[1], 1, a2.getDepth()},
                                           {c[0], c[1], 1, a3.getDepth()},
                                           {d[0], d[1], 1, a4.getDepth()},
        };

        return (int) Math.signum(determinant(matrix));


    }

    @Override double magnitude(double[] vector) {
        return Math.sqrt(vector[0] * vector[0] +
                         vector[1] * vector[1]);

    }


    /**
     * Constructs a two dimensional vector from the base atom to the 'atom'
     * @param base 0,0 coordinates
     * @param atom target of the vector
     * @return a double array of length 2
     */
    @Override
    public double[] toVector(A base, A atom) {
        return new double[]{getX(atom) - getX(base),
                            getY(atom) - getY(base)};
    }


    @Override
    public int getSign(A a1, A a2, A a3) {
        double[][] matrix = new double[][]{{getX(a1), getY(a1), 1},
                                           {getX(a2), getY(a2), 1},
                                           {getX(a3), getY(a3), 1}};
        double determinant = determinant(matrix);
        return Math.abs(determinant) < 0.2 ? 0 : (int) Math.signum(determinant);
    }
}
