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

package uk.ac.ebi.centres.test;

/**
 * Simple test atom that holds the properties we need
 *
 * @author John May
 */
public class TestAtom {

    private String symbol       = "";
    private int    atomicNumber = 0;
    private int    massNumber   = 0;


    public TestAtom(String symbol, int atomicNumber) {
        this.symbol = symbol;
        this.atomicNumber = atomicNumber;
    }


    public TestAtom(String symbol, int atomicNumber, int massNumber) {
        this.symbol = symbol;
        this.atomicNumber = atomicNumber;
        this.massNumber = massNumber;
    }


    public String getSymbol() {
        return symbol;
    }


    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }


    public int getAtomicNumber() {
        return atomicNumber;
    }


    public void setAtomicNumber(int atomicNumber) {
        this.atomicNumber = atomicNumber;
    }


    public int getMassNumber() {
        return massNumber;
    }


    public void setMassNumber(int massNumber) {
        this.massNumber = massNumber;
    }


    @Override
    public String toString() {
        return symbol + "" + (massNumber != 0 ? Integer.toString(massNumber)
                                              : "");
    }
}
