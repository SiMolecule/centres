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
  public void testIgnoreConstruction_null() {
    PairList descriptors = new PairList();
    Assert.assertFalse("Null should be ignored", descriptors.add(null));
  }

  @Test
  public void testIgnoreConstruction_Pseudo() {

    PairList descriptors = new PairList();

    Assert.assertFalse("r should be ignored", descriptors.add(Descriptor.r));
    Assert.assertFalse("s should be ignored", descriptors.add(Descriptor.s));
    Assert.assertTrue("R should not be ignored", descriptors.add(Descriptor.R));
    Assert.assertTrue("S should not be ignored", descriptors.add(Descriptor.S));
  }


  @Test
  public void testIgnoreConstruction_Pairing() {

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
  public void pairRM() {
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
  public void testClear() {

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
