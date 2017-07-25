package com.simolecule.centres;

import org.junit.Test;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.depict.DepictionGenerator;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.renderer.generators.standard.StandardGenerator;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;

import java.awt.*;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestAtropisomers {

  IAtomContainer loadMolfile(String path) throws Exception
  {
    try (InputStream in = getClass().getResourceAsStream(path)) {
      if (in == null) throw new NoSuchFileException(path);
      return new MDLV2000Reader(in).read(new AtomContainer());
    }
  }

  @Test
  public void M_BiNAP() throws Exception
  {
    IAtomContainer mol = loadMolfile("M_BiNAP.mol");
    CdkLabeller.label(mol);
    for (IStereoElement se : mol.stereoElements()) {
      switch (se.getConfigClass()) {
        case IStereoElement.AT:
          assertThat(se.getFocus().getProperty(BaseMol.CIP_LABEL_KEY, Descriptor.class),
                     is(Descriptor.M));
          break;
      }
    }
  }

  @Test
  public void P_BiNAP() throws Exception
  {
    IAtomContainer mol = loadMolfile("P_BiNAP.mol");
    CdkLabeller.label(mol);
    for (IStereoElement se : mol.stereoElements()) {
      switch (se.getConfigClass()) {
        case IStereoElement.AT:
          Descriptor label = se.getFocus().getProperty(BaseMol.CIP_LABEL_KEY);
          assertThat(label,
                     is(Descriptor.P));
          break;
      }
    }
  }

  @Test
  public void atropisomers() throws Exception
  {
    IAtomContainer mol = loadMolfile("ras_2.mol");
    CdkLabeller.label(mol);
    for (IStereoElement se : mol.stereoElements()) {
      Descriptor label = se.getFocus().getProperty(BaseMol.CIP_LABEL_KEY);
      se.getFocus().setProperty(StandardGenerator.ANNOTATION_LABEL,
                                StandardGenerator.ITALIC_DISPLAY_PREFIX + "(" + label + ")");
    }
    new DepictionGenerator(new Font("Verdana", Font.PLAIN, 24))
            .withAnnotationColor(Color.RED)
            .withZoom(2)
            .withAnnotationScale(0.4)
            .depict(mol)
            .writeTo("~/ras2.png");
  }

  @Test public void CHEBI_61677() throws Exception
  {
    IAtomContainer mol = loadMolfile("CHEBI_61677.mol");
    CdkLabeller.label(mol);
    for (IStereoElement se : mol.stereoElements()) {
      Descriptor label = se.getFocus().getProperty(BaseMol.CIP_LABEL_KEY);
      se.getFocus().setProperty(CDKConstants.COMMENT, label);
      se.getFocus().setProperty(StandardGenerator.ANNOTATION_LABEL,
                                StandardGenerator.ITALIC_DISPLAY_PREFIX + "(" + label + ")");
    }
    System.out.println(new SmilesGenerator(SmiFlavor.Isomeric | SmiFlavor.CxSmiles).create(mol));
    new DepictionGenerator(new Font("Verdana", Font.PLAIN, 24))
            .withAnnotationColor(Color.RED)
            .withZoom(2)
            .withAnnotationScale(0.4)
            .depict(mol)
            .writeTo("~/chebi_61677.png");
  }

}
