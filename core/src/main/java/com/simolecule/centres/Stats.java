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

package com.simolecule.centres;

import com.google.common.base.Joiner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public enum Stats {
  INSTANCE;

  public static String[] RULE_NAMES = new String[]{"1a", "1b", "2", "3", "4a", "4b", "4c", "5", "6"};


  public AtomicInteger         numConfigs        = new AtomicInteger();
  public AtomicInteger         numConfigLabelled = new AtomicInteger();
  public AtomicInteger         numAuxCalculated  = new AtomicInteger();
  public AtomicInteger         numAuxLabelled    = new AtomicInteger();
  public int[]                 numCentresFreq    = new int[256];
  public Map<Integer, Counter> digraphOrder      = new HashMap<>();
  public Map<Integer, Counter> digraphSpheres    = new HashMap<>();
  public int[]                 ruleFreq          = new int[9];
  public int[]                 ruleFreqPrev      = new int[9];

  private Stats()
  {
  }


  public void clear() {
    numConfigs.set(0);
    numConfigLabelled.set(0);
    numAuxCalculated.set(0);
    numAuxLabelled.set(0);
    Arrays.fill(numCentresFreq, 0);
    digraphOrder.clear();
    digraphSpheres.clear();
    Arrays.fill(ruleFreq, 0);
    Arrays.fill(ruleFreqPrev, 0);
  }

  public synchronized void countRule(int ruleIdx)
  {
    this.ruleFreq[ruleIdx]++;
  }

  public synchronized void countNumCenters(int numCentres)
  {
    this.numConfigs.addAndGet(numCentres);
    if (numCentres < numCentresFreq.length) {
      numCentresFreq[numCentres]++;
    } else {
      numCentresFreq = Arrays.copyOf(numCentresFreq, numCentres + 1);
      numCentresFreq[numCentres]++;
    }
  }

  public <A, B> int depth(Node<A, B> node)
  {
    if (!node.isSet(Node.EXPANDED))
      return 1;
    int max = 0;
    int d   = 0;
    for (Edge<A, B> edge : node.getOutEdges()) {
      d = depth(edge.getEnd());
      if (d > max)
        max = d;
    }
    return 1 + max;
  }

  public synchronized <A, B> void measureDigraph(Digraph<A, B> digraph)
  {
    int     numNodes = digraph.getNumNodes();
    Counter count    = digraphOrder.get(numNodes);
    if (count != null)
      count.count++;
    else
      digraphOrder.put(numNodes, new Counter(1));


    digraph.changeRoot(digraph.getRoot());

    int depth = depth(digraph.getRoot());
    count = digraphSpheres.get(depth);
    if (count != null)
      count.count++;
    else
      digraphSpheres.put(depth, new Counter(1));
  }

  public String getRulesUsed() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ruleFreq.length; i++) {
      if (ruleFreq[i] > 0) {
        if (sb.length() != 0)
          sb.append(',');
        sb.append(RULE_NAMES[i]);
      }
    }
    return sb.toString();
  }

  public void write(String dataset, File fname)
  {

    File info   = new File(fname + ".info");
    File sphere = new File(fname + ".sphere");
    File count  = new File(fname + ".count");
    File rules  = new File(fname + ".rules");
    try (FileOutputStream fout = new FileOutputStream(info);
         BufferedWriter bwtr = new BufferedWriter(new OutputStreamWriter(fout))) {
      bwtr.write(Joiner.on('\t').join("Num Centres", Stats.INSTANCE.numConfigs));
      bwtr.newLine();
      bwtr.write(Joiner.on('\t').join("Num Centres Labelled", Stats.INSTANCE.numConfigLabelled));
      bwtr.newLine();
      bwtr.write(Joiner.on('\t').join("Num Aux Required", Stats.INSTANCE.numAuxCalculated));
      bwtr.newLine();
      bwtr.write(Joiner.on('\t').join("Num Aux Used", Stats.INSTANCE.numAuxLabelled));
      bwtr.newLine();
    } catch (IOException e) {
      System.err.println("Could not write info stats: " + info);
    }

    final int numConfigs = Stats.INSTANCE.numConfigs.get();

    try (FileOutputStream fout = new FileOutputStream(sphere);
         BufferedWriter bwtr = new BufferedWriter(new OutputStreamWriter(fout))) {
      bwtr.write(Joiner.on('\t').join("Dataset", "Sphere", "Freq", "Frac"));
      bwtr.newLine();
      for (Map.Entry<Integer, Stats.Counter> e : Stats.INSTANCE.digraphSpheres.entrySet()) {
        bwtr.write(dataset + "\t" + e.getKey() + "\t" + e.getValue() + "\t" + String.format("%.06f", 100 * (e.getValue().count / (double) numConfigs)));
        bwtr.newLine();
      }
    } catch (IOException e) {
      System.err.println("Could not write sphere stats: " + sphere);
    }

    try (FileOutputStream fout = new FileOutputStream(count);
         BufferedWriter bwtr = new BufferedWriter(new OutputStreamWriter(fout))) {
      bwtr.write(Joiner.on('\t').join("Dataset", "Count", "Freq", "Frac"));
      bwtr.newLine();
      int total = 0;
      for (int i = 0; i < Stats.INSTANCE.numCentresFreq.length; i++) {
        total += Stats.INSTANCE.numCentresFreq[i];
      }
      for (int i = 0; i < Stats.INSTANCE.numCentresFreq.length; i++) {
        bwtr.write(dataset + "\t" + i + "\t" + Stats.INSTANCE.numCentresFreq[i] + "\t" + String.format("%.06f",100*(Stats.INSTANCE.numCentresFreq[i]/(double)total)));
        bwtr.newLine();
      }
    } catch (IOException e) {
      System.err.println("Could not write count stats: " + count);
    }

    try (FileOutputStream fout = new FileOutputStream(rules);
         BufferedWriter bwtr = new BufferedWriter(new OutputStreamWriter(fout))) {
      bwtr.write(Joiner.on('\t').join("Dataset", "Rule", "Freq", "Frac"));
      bwtr.newLine();
      for (int i = 0; i < Stats.INSTANCE.ruleFreq.length; i++) {
        bwtr.write(dataset + "\t" + Stats.RULE_NAMES[i] + "\t" + Stats.INSTANCE.ruleFreq[i] + "\t" + String.format("%.06f", 100*(Stats.INSTANCE.ruleFreq[i]/(double)numConfigs)));
        bwtr.newLine();
      }
    } catch (IOException e) {
      System.err.println("Could not write rules stats: " + rules);
    }

  }

  public static final class Counter {
    private int count;

    public Counter(int count)
    {
      this.count = count;
    }

    @Override
    public String toString()
    {
      return Integer.toString(count);
    }
  }
}
