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
