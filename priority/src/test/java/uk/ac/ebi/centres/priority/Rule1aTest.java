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

package uk.ac.ebi.centres.priority;

import org.junit.Test;
import uk.ac.ebi.centres.Ligand;
import uk.ac.ebi.centres.priority.access.AtomicNumberAccessor;
import uk.ac.ebi.centres.test.TestAtom;
import uk.ac.ebi.centres.test.TestLigand;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * @author John May
 */
public class Rule1aTest {

    private AtomicNumberAccessor<TestAtom> accessor = new AtomicNumberAccessor<TestAtom>() {
        @Override
        public int getAtomicNumber(TestAtom atom) {
            return atom.getAtomicNumber();
        }
    };

    private Rule1a<TestAtom> rule = new Rule1a<TestAtom>(accessor);


    @Test
    public void testCompare_Equal() throws Exception {

        rule.setSorter(new InsertionSorter<TestAtom>(rule));

        Ligand<TestAtom> carbon1 = new TestLigand(new TestAtom("C", 6));
        Ligand<TestAtom> carbon2 = new TestLigand(new TestAtom("C", 6));

        assertEquals(0, rule.compare(carbon1, carbon2));
        assertFalse(rule.prioritise(Arrays.asList(carbon1, carbon2)).isUnique());

    }


    @Test
    public void testCompare_Different() throws Exception {

        rule.setSorter(new InsertionSorter<TestAtom>(rule));

        Ligand<TestAtom> carbon = new TestLigand(new TestAtom("C", 6));
        Ligand<TestAtom> nitrogen = new TestLigand(new TestAtom("N", 7));

        assertThat("Higher priority in second argument should return < 0",
                   rule.compare(carbon, nitrogen),
                   is(lessThan(0)));
        assertThat("Higher priority in second argument should return > 0",
                   rule.compare(nitrogen, carbon),
                   is(greaterThan(0)));

    }


    /**
     * Checks the sorting is as we would expect
     *
     * @throws Exception
     */
    @Test
    public void testPrioritise() throws Exception {

        rule.setSorter(new InsertionSorter<TestAtom>(rule));

        Ligand<TestAtom> carbon = new TestLigand(new TestAtom("C", 6));
        Ligand<TestAtom> nitrogen = new TestLigand(new TestAtom("N", 7));
        Ligand<TestAtom> oxygen = new TestLigand(new TestAtom("O", 8));


        List<Ligand<TestAtom>> expected = Arrays.asList(oxygen, nitrogen, carbon);

        // N, O, C -> O, C, N
        {
            List<Ligand<TestAtom>> actual = Arrays.asList(nitrogen, oxygen, carbon);
            assertThat("Lists were equal before sorting",
                       actual, not(expected));
            assertTrue("Non-unique items detected whilst sorting",
                       rule.prioritise(actual).isUnique());
            assertThat("Lists were not equal",
                       actual, equalTo(expected));
        }

        // N, C, O -> O, N, C
        {
            List<Ligand<TestAtom>> actual = Arrays.asList(nitrogen, carbon, oxygen);
            assertThat("Lists were equal before sorting",
                       actual, not(expected));
            assertTrue("Non-unique items detected whilst sorting",
                       rule.prioritise(actual).isUnique());
            assertThat("Lists were not equal",
                       actual, equalTo(expected));
        }

        // C, N, O -> O, N, C
        {
            List<Ligand<TestAtom>> actual = Arrays.asList(carbon, nitrogen, oxygen);
            assertThat("Lists were equal before sorting",
                       actual, not(expected));
            assertTrue("Non-unique items detected whilst sorting",
                       rule.prioritise(actual).isUnique());
            assertThat("Lists were not equal",
                       actual, equalTo(expected));
        }

        // C, O, N -> O, N, C
        {
            List<Ligand<TestAtom>> actual = Arrays.asList(carbon, oxygen, nitrogen);
            assertThat("Lists were equal before sorting",
                       actual, not(expected));
            assertTrue("Non-unique items detected whilst sorting",
                       rule.prioritise(actual).isUnique());
            assertThat("Lists were not equal",
                       actual, equalTo(expected));
        }


    }


}
