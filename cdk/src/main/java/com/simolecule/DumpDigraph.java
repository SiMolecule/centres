package com.simolecule;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Digraph;
import com.simolecule.centres.Edge;
import com.simolecule.centres.Node;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.interfaces.ITetrahedralChirality;
import org.openscience.cdk.silent.Atom;
import org.openscience.cdk.silent.Bond;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

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
