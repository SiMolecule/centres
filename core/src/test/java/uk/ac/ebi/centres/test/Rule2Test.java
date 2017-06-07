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

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Test;
import com.simolecule.centres.rules.Sort;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;


/**
 * @author John May
 */
public class Rule2Test {
//
//    private Rule2<TestAtom> rule = new Rule2<TestAtom>(new Mock());
//
//    @Test
//    public void testCompare_Equal() throws Exception {
//
//        rule.setSorter(new Sort<TestAtom>(rule));
//
//        Node<TestAtom> carbon1 = new TestNode(new TestAtom("C", 6, 12));
//        Node<TestAtom> carbon2 = new TestNode(new TestAtom("C", 6, 12));
//
//        assertEquals(0, rule.compare(carbon1, carbon2));
//        assertFalse(rule.prioritise(Arrays.asList(carbon1, carbon2)).isUnique());
//
//    }
//
//
//    @Test
//    public void testCompare_Different() throws Exception {
//
//        rule.setSorter(new Sort<TestAtom>(rule));
//
//        Node<TestAtom> carbon12 = new TestNode(new TestAtom("C", 6, 12));
//        Node<TestAtom> carbon13 = new TestNode(new TestAtom("C", 6, 13));
//
//        assertThat("Higher priority in second argument should return < 0",
//                   rule.compare(carbon12, carbon13),
//                   CoreMatchers.is(Matchers.lessThan(0)));
//        assertThat("Higher priority in second argument should return > 0",
//                   rule.compare(carbon13, carbon12),
//                   CoreMatchers.is(Matchers.greaterThan(0)));
//
//    }

}
