package com.simolecule.centres;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class Main {

  private static final SmilesParser    smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());
  private static final SmilesGenerator smigen = new SmilesGenerator(SmiFlavor.Isomeric | SmiFlavor.CxSmiles);

  static void processMol(IAtomContainer mol)
  {
    CdkLabeler.label(mol);
  }

  static void processSmiles(BufferedWriter wtr, String smi) throws CDKException, IOException
  {
    try {
      IAtomContainer mol = smipar.parseSmiles(smi);
      processMol(mol);
      for (IAtom atom : mol.atoms()) {
        Descriptor desc = atom.getProperty(BaseMol.CIP_LABEL_KEY);
        if (desc != null)
          atom.setProperty(CDKConstants.COMMENT, desc);
      }
      for (IBond bond : mol.bonds()) {
        Descriptor desc = bond.getProperty(BaseMol.CIP_LABEL_KEY);
        if (desc != null)
          bond.getBegin().setProperty(CDKConstants.COMMENT, desc);
      }
      //wtr.write(smigen.create(mol));
      //wtr.newLine();
    } catch (Exception e) {
      System.err.println(smi + " " + e.getMessage());
    }
  }

  static void processInputStream(BufferedWriter wtr, InputStream in)
  {
    int numProcessed = 0;
    long t0 = System.nanoTime();
    try (InputStreamReader rdr = new InputStreamReader(in, StandardCharsets.UTF_8);
         BufferedReader brdr = new BufferedReader(rdr)) {
      String line;
      while ((line = brdr.readLine()) != null) {
        try {
          ++numProcessed;
          processSmiles(wtr, line);
          if (numProcessed % 2500 == 0)
            System.err.printf("\r %d...", numProcessed);
        } catch (CDKException ex) {
          System.err.println("Bad SMILES: " + line);
        }
      }
    } catch (IOException e) {
      System.err.println("IO Error: " + e.getMessage());
    }
    long t1 = System.nanoTime();
    System.err.printf("[INFO] %d processed in %.2f s\n",
                      numProcessed,
                      (t1-t0)/1e9);
  }

  public static void main(String[] args)
  {
    try (OutputStream out = System.out;
         Writer wtr = new OutputStreamWriter(out);
         BufferedWriter bwtr = new BufferedWriter(wtr)) {
      if (args.length == 0)
        processInputStream(bwtr, System.in);
      else if (new File(args[0]).exists()) {
        try (InputStream in = new FileInputStream(new File(args[0]))) {
          processInputStream(bwtr, in);
        }
      } else {
        processSmiles(bwtr, args[0]);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } catch (CDKException e) {
      e.printStackTrace();
    }
  }
}
