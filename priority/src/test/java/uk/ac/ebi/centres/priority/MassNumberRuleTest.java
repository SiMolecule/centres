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
import uk.ac.ebi.centres.priority.access.MassNumberAccessor;
import uk.ac.ebi.centres.test.TestAtom;
import uk.ac.ebi.centres.test.TestLigand;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;


/**
 * @author John May
 */
public class MassNumberRuleTest {

    private MassNumberAccessor<TestAtom> accessor = new MassNumberAccessor<TestAtom>() {
        @Override
        public int getMassNumber(TestAtom atom) {
            return atom.getMassNumber();
        }
    };

    private MassNumberRule<TestAtom> rule = new MassNumberRule<TestAtom>(accessor);


    @Test
    public void testCompare_Equal() throws Exception {

        rule.setSorter(new InsertionSorter<TestAtom>(rule));

        Ligand<TestAtom> carbon1 = new TestLigand(new TestAtom("C", 6, 12));
        Ligand<TestAtom> carbon2 = new TestLigand(new TestAtom("C", 6, 12));

        assertEquals(0, rule.compare(carbon1, carbon2));
        assertFalse(rule.prioritise(Arrays.asList(carbon1, carbon2)));

    }


    @Test
    public void testCompare_Different() throws Exception {

        rule.setSorter(new InsertionSorter<TestAtom>(rule));

        Ligand<TestAtom> carbon12 = new TestLigand(new TestAtom("C", 6, 12));
        Ligand<TestAtom> carbon13 = new TestLigand(new TestAtom("C", 6, 13));

        assertThat("Higher priority in second argument should return < 0",
                   rule.compare(carbon12, carbon13),
                   is(lessThan(0)));
        assertThat("Higher priority in second argument should return > 0",
                   rule.compare(carbon13, carbon12),
                   is(greaterThan(0)));

    }

}
