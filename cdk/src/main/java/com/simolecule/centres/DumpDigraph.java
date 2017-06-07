package com.simolecule.centres;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

public class DumpDigraph {



  public static void main(String[] args) throws CDKException
  {
    String         smiles = "O[C@H](C1=CC(O)=CC=C1)C2=CC=CC(O)=C2";
    SmilesParser   smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());
    IAtomContainer mol    = smipar.parseSmiles(smiles);

    for (IStereoElement tc : mol.stereoElements()) {
      Digraph<IAtom, IBond> graph = new Digraph<IAtom, IBond>(new CdkMol(mol), (IAtom) tc.getFocus());
      System.out.println(new CdkMol(mol).dumpDigraph(graph));
    }
  }
}
