package com.simolecule.centres;

import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IStereoElement;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.stereo.Projection;
import org.openscience.cdk.stereo.StereoElementFactory;

import java.io.InputStream;
import java.nio.file.NoSuchFileException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestAtropisomers {

  IAtomContainer loadMolfile(String path) throws Exception
  {
    try (InputStream in = getClass().getResourceAsStream(path)) {
      if (in == null) throw new NoSuchFileException(path);
      IAtomContainer mol = new MDLV2000Reader(in).read(new AtomContainer());
      mol.setStereoElements(StereoElementFactory.using2DCoordinates(mol)
                                                .interpretProjections(Projection.Chair, Projection.Haworth)
                                                .createAll());
      return mol;
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

}
