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
