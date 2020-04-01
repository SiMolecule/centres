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
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * @author John May
 */
public class Rule1aTest {

//    private Rule1a<TestAtom> rule = new Rule1a<TestAtom>(new Mock());
//
//
//    @Test
//    public void testCompare_Equal() throws Exception {
//
//        rule.setSorter(new Sort<TestAtom>(rule));
//
//        Node<TestAtom> carbon1 = new TestNode(new TestAtom("C", 6));
//        Node<TestAtom> carbon2 = new TestNode(new TestAtom("C", 6));
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
//        Node<TestAtom> carbon   = new TestNode(new TestAtom("C", 6));
//        Node<TestAtom> nitrogen = new TestNode(new TestAtom("Other", 7));
//
//        assertThat("Higher priority in second argument should return < 0",
//                   rule.compare(carbon, nitrogen),
//                   CoreMatchers.is(Matchers.lessThan(0)));
//        assertThat("Higher priority in second argument should return > 0",
//                   rule.compare(nitrogen, carbon),
//                   CoreMatchers.is(Matchers.greaterThan(0)));
//
//    }
//
//
//    /**
//     * Checks the sorting is as we would expect
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testPrioritise() throws Exception {
//
//        rule.setSorter(new Sort<TestAtom>(rule));
//
//        Node<TestAtom> carbon   = new TestNode(new TestAtom("C", 6));
//        Node<TestAtom> nitrogen = new TestNode(new TestAtom("Other", 7));
//        Node<TestAtom> oxygen   = new TestNode(new TestAtom("O", 8));
//
//
//        List<Node<TestAtom>> expected = Arrays.asList(oxygen, nitrogen, carbon);
//
//        // Other, O, C -> O, C, Other
//        {
//            List<Node<TestAtom>> actual = Arrays.asList(nitrogen, oxygen, carbon);
//            assertThat("Lists were equal before sorting",
//                       actual, not(expected));
//            assertTrue("Non-unique items detected whilst sorting",
//                       rule.prioritise(actual).isUnique());
//            assertThat("Lists were not equal",
//                       actual, equalTo(expected));
//        }
//
//        // Other, C, O -> O, Other, C
//        {
//            List<Node<TestAtom>> actual = Arrays.asList(nitrogen, carbon, oxygen);
//            assertThat("Lists were equal before sorting",
//                       actual, not(expected));
//            assertTrue("Non-unique items detected whilst sorting",
//                       rule.prioritise(actual).isUnique());
//            assertThat("Lists were not equal",
//                       actual, equalTo(expected));
//        }
//
//        // C, Other, O -> O, Other, C
//        {
//            List<Node<TestAtom>> actual = Arrays.asList(carbon, nitrogen, oxygen);
//            assertThat("Lists were equal before sorting",
//                       actual, not(expected));
//            assertTrue("Non-unique items detected whilst sorting",
//                       rule.prioritise(actual).isUnique());
//            assertThat("Lists were not equal",
//                       actual, equalTo(expected));
//        }
//
//        // C, O, Other -> O, Other, C
//        {
//            List<Node<TestAtom>> actual = Arrays.asList(carbon, oxygen, nitrogen);
//            assertThat("Lists were equal before sorting",
//                       actual, not(expected));
//            assertTrue("Non-unique items detected whilst sorting",
//                       rule.prioritise(actual).isUnique());
//            assertThat("Lists were not equal",
//                       actual, equalTo(expected));
//        }
//
//
//    }


}
