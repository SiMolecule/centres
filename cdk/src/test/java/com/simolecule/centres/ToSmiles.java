package com.simolecule.centres;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.io.StringReader;

public class ToSmiles {

  public static void main(String[] args) throws CDKException
  {
    String molfile = "CIP:1p 6s 8p; BB:P-93.5.1.4.2.2;\n" +
                     "  Mrv1641806151716283D          \n" +
                     "Jmol version 14.17.2  2017-05-27 15:31 EXTRACT: ({0:28})\n" +
                     " 29 29  0  0  0  0            999 V2000\n" +
                     "   -4.8701   -0.6303    0.1608 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -4.1024   -1.9186    0.1748 C   0  0  2  0  0  0  0  0  0  0  0  0\n" +
                     "   -4.1633    0.6531   -0.1179 C   0  0  2  0  0  0  0  0  0  0  0  0\n" +
                     "   -2.7023   -1.8949   -0.4483 C   0  0  2  0  0  0  0  0  0  0  0  0\n" +
                     "   -2.6802    0.6437    0.1814 C   0  0  2  0  0  0  0  0  0  0  0  0\n" +
                     "   -1.9116   -0.5690   -0.3633 C   0  0  2  0  0  0  0  0  0  0  0  0\n" +
                     "   -6.1672   -0.6155    0.1088 C   0  0  1  0  0  0  0  0  0  0  0  0\n" +
                     "   -7.4143   -0.4426   -0.1779 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -7.8461    0.0075   -1.5304 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -1.1982   -0.1967   -1.6860 C   0  0  2  0  0  0  0  0  0  0  0  0\n" +
                     "    0.1700    0.4056   -1.3824 C   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -4.6781   -2.6897   -0.3510 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -4.0291   -2.2706    1.2075 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -4.6161    1.4572    0.4778 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -4.3255    0.9283   -1.1647 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -2.7378   -2.2936   -1.4675 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -2.1177   -2.6541    0.0789 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -2.5419    0.6920    1.2675 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -2.2729    1.5782   -0.2246 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -1.1179   -0.7638    0.3681 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -8.1587   -0.4953    0.6123 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -7.5586   -0.7301   -2.2901 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -7.3811    0.9647   -1.7858 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -8.9293    0.1345   -1.5696 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -1.7958    0.5255   -2.2573 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "   -1.0620   -1.0591   -2.3435 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "    0.8628   -0.3809   -1.0549 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "    0.1166    1.1496   -0.5832 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "    0.6034    0.8870   -2.2623 H   0  0  0  0  0  0  0  0  0  0  0  0\n" +
                     "  1  2  1  0  0  0  0\n" +
                     "  1  3  1  0  0  0  0\n" +
                     "  1  7  2  0  0  0  0\n" +
                     "  2  4  1  0  0  0  0\n" +
                     "  3  5  1  0  0  0  0\n" +
                     "  4  6  1  0  0  0  0\n" +
                     "  5  6  1  0  0  0  0\n" +
                     "  6 10  1  0  0  0  0\n" +
                     "  7  8  2  0  0  0  0\n" +
                     "  8  9  1  0  0  0  0\n" +
                     " 10 11  1  0  0  0  0\n" +
                     "  2 12  1  0  0  0  0\n" +
                     "  2 13  1  0  0  0  0\n" +
                     "  3 14  1  0  0  0  0\n" +
                     "  3 15  1  0  0  0  0\n" +
                     "  4 16  1  0  0  0  0\n" +
                     "  4 17  1  0  0  0  0\n" +
                     "  5 18  1  0  0  0  0\n" +
                     "  5 19  1  0  0  0  0\n" +
                     "  6 20  1  0  0  0  0\n" +
                     "  8 21  1  0  0  0  0\n" +
                     "  9 22  1  0  0  0  0\n" +
                     "  9 23  1  0  0  0  0\n" +
                     "  9 24  1  0  0  0  0\n" +
                     " 10 25  1  0  0  0  0\n" +
                     " 10 26  1  0  0  0  0\n" +
                     " 11 27  1  0  0  0  0\n" +
                     " 11 28  1  0  0  0  0\n" +
                     " 11 29  1  0  0  0  0\n" +
                     "M  END\n";
    IAtomContainer mol = new MDLV2000Reader(new StringReader(molfile)).read(new AtomContainer());
    AtomContainerManipulator.suppressHydrogens(mol);
    System.out.println(new SmilesGenerator(SmiFlavor.Isomeric).create(mol));
  }
}
