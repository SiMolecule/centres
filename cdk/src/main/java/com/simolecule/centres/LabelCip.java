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

import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class LabelCip {

  private static String fmt;
  // the file name
  private static String fname;
  // the SDfile property with expected values in it
  private static String cipKey;

  private static boolean processCommandLine(String[] args) {
    int j = 0;
    for (int i = 0; i < args.length; i++) {
      if (args[i].charAt(0) == '-') {
        if (args[i].equals("--expected") ||
            args[i].equals("-e")) {
          ++i;
          if (i >= args.length)
            return false;
          cipKey = args[i];
        } else if (args[i].equals("-i")) {
          ++i;
          if (i >= args.length)
            return false;
          fmt = args[i];
        }
      } else {
        switch (j++) {
          case 0:
            fname = args[i];
            break;
          default:
            return false;
        }
      }
    }
    return fname != null;
  }

  private static String getCipKeys(IAtomContainer mol) {
    Map<Integer,String> actual = new TreeMap<>();
    for (IStereoElement se : mol.stereoElements()) {
      switch (se.getConfigClass()) {
        case IStereoElement.TH:
          Descriptor desc = se.getFocus().getProperty(BaseMol.CIP_LABEL_KEY);
          if (desc != null)
            actual.put(mol.indexOf((IAtom) se.getFocus()) + 1, desc.name());
          break;
        case IStereoElement.AL:
          desc = se.getFocus().getProperty(BaseMol.CIP_LABEL_KEY);
          IAtom[] ends = org.openscience.cdk.stereo.ExtendedTetrahedral.findTerminalAtoms(mol, (IAtom) se.getFocus());
          if (desc != null) {
            actual.put(mol.indexOf(ends[0]) + 1, desc
                    .name());
            actual.put(mol.indexOf(ends[1]) + 1, desc
                    .name());
          }
          break;
        case IStereoElement.CU:
          desc = se.getFocus().getProperty(BaseMol.CIP_LABEL_KEY);
          ends = org.openscience.cdk.stereo.ExtendedCisTrans.findTerminalAtoms(mol, (IBond) se.getFocus());
          if (desc != null) {
            actual.put(mol.indexOf(ends[0]) + 1, desc
                    .name());
            actual.put(mol.indexOf(ends[1]) + 1, desc
                    .name());
          }
          break;
        case IStereoElement.CT:
        case IStereoElement.AT:
          String label = null;
          desc = se.getFocus().getProperty(BaseMol.CIP_LABEL_KEY);
          if (desc == Descriptor.seqTrans)
            label = "e";
          else if (desc == Descriptor.seqCis)
            label = "z";
          else if (desc != null)
            label = desc.name();
          if (label != null) {
            actual.put(mol.indexOf(((IBond) se.getFocus()).getBegin()) + 1, label);
            actual.put(mol.indexOf(((IBond) se.getFocus()).getEnd()) + 1, label);
          }
          break;
      }
    }
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<Integer,String> e : actual.entrySet()) {
      if (sb.length() != 0)
        sb.append(' ');
      sb.append(e.getKey()).append(e.getValue());
    }
    return sb.toString();
  }

  private static void processMol(IAtomContainer mol) {
    CdkLabeller.label(mol);
    String actual = getCipKeys(mol);
    if (cipKey != null) {
      String expected = mol.getProperty(cipKey);
      if (expected == null)
        expected = "";
      if (!expected.equals(actual)) {
        StringBuilder diff = new StringBuilder();
        for (int i = 0; i < Math.max(expected.length(),
                                     actual.length()); i++) {
          if (i >= expected.length() || i >= actual.length() ||
              expected.charAt(i) != actual.charAt(i))
            diff.append('^');
          else
            diff.append(' ');
        }
        System.out.println(mol.getTitle() + "\n" +
                           "  expected=" + expected + "\n" +
                           "       was=" + actual + "\n" +
                           "           " + diff);
      }
    } else {
      System.out.println(actual + "\t" + mol.getTitle());
    }
  }

  private static void processSDfile(InputStream in) {
    IChemObjectBuilder bldr = SilentChemObjectBuilder.getInstance();
    try (IteratingSDFReader sdfr = new IteratingSDFReader(in, bldr, true)) {
      while (sdfr.hasNext()) {
        processMol(sdfr.next());
      }
    } catch (IOException e) {
      System.err.println("ERROR - IO Error, " + e.getMessage());
    }
  }

  private static void processSMIfile(InputStream in) {
    IChemObjectBuilder bldr   = SilentChemObjectBuilder.getInstance();
    SmilesParser       smipar = new SmilesParser(bldr);
    try (Reader rdr = new InputStreamReader(in);
         BufferedReader brdr = new BufferedReader(rdr)) {
      String line;
      while ((line = brdr.readLine()) != null) {
        try {
          IAtomContainer mol = smipar.parseSmiles(line);
          String[] cols = mol.getTitle().split("\t");
          for (int i = 0; i < cols.length; i++)
            mol.setProperty(Integer.toString(i), cols[i]);
          processMol(mol);
        } catch (InvalidSmilesException e) {
          System.err.println("ERROR: BAD SMILES\n" + e.getMessage());
        }
      }
    } catch (IOException e) {
      System.err.println("ERROR - IO Error, " + e.getMessage());
    }
  }

  private static String getSuffix(String fname) {
    int i = fname.lastIndexOf(".");
    return i < 0 ? "" : fname.substring(i+1);
  }

  private static boolean isSmilesFile(String fname) {
    switch (getSuffix(fname).toLowerCase(Locale.ROOT)) {
      case "smi":
      case "csmi":
      case "cxsmi":
      case "can":
      case "ism":
        return true;
    }
    return false;
  }

  private static boolean isSDfile(String fname) {
    switch (getSuffix(fname).toLowerCase(Locale.ROOT)) {
      case "sdf":
      case "mol":
      case "mdl":
        return true;
    }
    return false;
  }

  public static void main(String[] args) {
    if (!processCommandLine(args)) {
      System.err.println("Usage: label {input.sdf}");
      return;
    }

    if (fname.equals("-"))
      processSDfile(System.in);
    else {
      try (InputStream in = new FileInputStream(fname)) {
        if (isSDfile(fname))
          processSDfile(in);
        else if (isSmilesFile(fname))
          processSMIfile(in);
      } catch (IOException e) {
        System.err.println("Could not open file: " + fname + ", " + e.getMessage());
      }
    }
  }


}
