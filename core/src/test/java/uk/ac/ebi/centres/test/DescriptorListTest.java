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

import com.simolecule.centres.rules.PairList;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import com.simolecule.centres.Descriptor;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author John May
 */
public class DescriptorListTest {

  @Test
  public void testIgnoreConstruction_null() throws Exception
  {
    PairList descriptors = new PairList();
    Assert.assertFalse("Null should be ignored", descriptors.add(null));
  }

  @Test
  public void testIgnoreConstruction_Pseudo() throws Exception
  {

    PairList descriptors = new PairList();

    Assert.assertFalse("r should be ignored", descriptors.add(Descriptor.r));
    Assert.assertFalse("s should be ignored", descriptors.add(Descriptor.s));
    Assert.assertTrue("R should not be ignored", descriptors.add(Descriptor.R));
    Assert.assertTrue("S should not be ignored", descriptors.add(Descriptor.S));
  }


  @Test
  public void testIgnoreConstruction_Pairing() throws Exception
  {

    PairList descriptors = new PairList();

    Assert.assertEquals(0, descriptors.getPairing());
    Assert.assertEquals("0", Integer.toBinaryString(descriptors.getPairing()));
    descriptors.add(Descriptor.R);
    Assert.assertEquals("0", Integer.toBinaryString(descriptors.getPairing()));
    // like
    descriptors.add(Descriptor.R);
    Assert.assertEquals("1000000000000000000000000000000",
                        Integer.toBinaryString(descriptors.getPairing()));
    // like
    descriptors.add(Descriptor.R);
    Assert.assertEquals("1100000000000000000000000000000",
                        Integer.toBinaryString(descriptors.getPairing()));
    // unlike
    descriptors.add(Descriptor.S);
    Assert.assertEquals("1100000000000000000000000000000",
                        Integer.toBinaryString(descriptors.getPairing()));
    // like
    descriptors.add(Descriptor.R);
    Assert.assertEquals("1101000000000000000000000000000",
                        Integer.toBinaryString(descriptors.getPairing()));
    // like
    descriptors.add(Descriptor.R);
    Assert.assertEquals("1101100000000000000000000000000",
                        Integer.toBinaryString(descriptors.getPairing()));
    // like
    descriptors.add(Descriptor.R);
    Assert.assertEquals("1101110000000000000000000000000",
                        Integer.toBinaryString(descriptors.getPairing()));
    // unlike
    descriptors.add(Descriptor.S);
    Assert.assertEquals("1101110000000000000000000000000",
                        Integer.toBinaryString(descriptors.getPairing()));
    // like
    descriptors.add(Descriptor.R);
    Assert.assertEquals("1101110100000000000000000000000",
                        Integer.toBinaryString(descriptors.getPairing()));

  }


  @Test
  public void testAppend_empty()
  {
    PairList head = new PairList();
    head.add(Descriptor.R);
    head.add(Descriptor.R);
    head.add(Descriptor.S);
    head.add(Descriptor.R);
    List<PairList> lists = head.append(Arrays.asList(new PairList()));
    Assert.assertEquals(1, lists.size());
    Assert.assertEquals(head.getPairing(), lists.get(0).getPairing());

  }


  @Test
  public void testAppend()
  {

    PairList head = new PairList();
    head.add(Descriptor.R);
    head.add(Descriptor.R);
    head.add(Descriptor.S);
    head.add(Descriptor.R);

    PairList tail1 = new PairList();
    tail1.add(Descriptor.R);
    tail1.add(Descriptor.S);
    tail1.add(Descriptor.R);

    PairList tail2 = new PairList();
    tail2.add(Descriptor.S);
    tail2.add(Descriptor.S);
    tail2.add(Descriptor.R);

    List<PairList> created = head.append(Arrays.asList(tail1, tail2));

    Assert.assertEquals(2, created.size());

    Assert.assertEquals("1011010000000000000000000000000",
                        Integer.toBinaryString(created.get(0).getPairing()));
    Assert.assertEquals("1010010000000000000000000000000",
                        Integer.toBinaryString(created.get(1).getPairing()));

  }

  @Test
  public void pairRM() throws Exception {
    PairList list1 = new PairList();
    PairList list2 = new PairList();
    list1.add(Descriptor.R);
    list1.add(Descriptor.M);
    list1.add(Descriptor.R);
    list1.add(Descriptor.S);
    list2.add(Descriptor.R);
    list2.add(Descriptor.P);
    list2.add(Descriptor.S);
    list2.add(Descriptor.M);
    assertThat(list1.toString(), is("R:llu"));
    assertThat(list2.toString(), is("R:uul"));
  }


  @Test
  public void testClear() throws Exception
  {

    PairList descriptors = new PairList();
    descriptors.add(Descriptor.R);
    descriptors.add(Descriptor.R);
    descriptors.add(Descriptor.S);
    descriptors.add(Descriptor.R);
    descriptors.add(Descriptor.S);
    Assert.assertThat(descriptors.getPairing(), CoreMatchers.is(Matchers.greaterThan(0)));
    descriptors.clear();
    Assert.assertThat(descriptors.getPairing(), is(0));

  }
}
