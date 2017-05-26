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

import uk.ac.ebi.centres.Descriptor;
import uk.ac.ebi.centres.Node;
import uk.ac.ebi.centres.graph.Edge;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Need to do better testing
 * @author John May
 */
@Deprecated
public class TestNode implements Node<TestAtom> {

    private TestAtom atom;


    @Override
    public boolean isTerminal() {
        return false;
    }


    @Override
    public void reset() {

    }


    @Override
    public boolean isDuplicate() {
        return false;
    }


    @Override
    public boolean isBranching() {
        return false;
    }


    public TestNode(TestAtom atom) {
        this.atom = atom;
    }


    @Override
    public TestAtom getAtom() {
        return atom;
    }


    @Override
    public void setParent(TestAtom atom) {

    }


    @Override
    public TestAtom getParent() {
        return null;
    }


    @Override
    public List<Node<TestAtom>> getNodes() {
        return Collections.EMPTY_LIST;
    }


    @Override
    public Set<TestAtom> getVisited() {
        return Collections.EMPTY_SET;
    }


    @Override
    public int getDistanceFromRoot() {
        return 0;
    }


    @Override
    public Boolean isParent(TestAtom atom) {
        return Boolean.FALSE;
    }


    @Override
    public Boolean isVisited(TestAtom atom) {
        return Boolean.FALSE;
    }


    @Override
    public void setAuxiliary(Descriptor descriptor) {

    }


    @Override
    public Descriptor getAuxiliary() {
        return null;
    }


    @Override
    public void setDescriptor(Descriptor descriptor) {

    }


    @Override
    public Descriptor getDescriptor() {
        return null;
    }


    @Override
    public List<Edge<TestAtom>> getArcs() {
        return null;
    }


    @Override
    public Edge<TestAtom> getParentArc() {
        return null;
    }

            
    @Override
    public int getDepth() {
        return 0;
    }


    @Override
    public String toString() {
        return atom.toString();
    }


    @Override public void markOrderedBy(Class<?> rule) {
        
    }

    @Override public boolean isOrderedBy(Class<?> rule) {
        return false;
    }

    @Override public void clearOrderedBy() {

    }
}
