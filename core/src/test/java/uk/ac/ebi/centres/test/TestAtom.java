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
