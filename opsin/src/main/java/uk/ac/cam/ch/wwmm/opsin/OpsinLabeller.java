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

package uk.ac.cam.ch.wwmm.opsin;

import com.simolecule.centres.Labeller;
import com.simolecule.centres.config.Configuration;
import com.simolecule.centres.config.Sp2Bond;
import com.simolecule.centres.config.Tetrahedral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpsinLabeller extends Labeller<Atom, Bond> {

  OpsinLabeller()
  {
  }

  static List<Configuration<Atom, Bond>> createCfgs(Fragment fragment)
  {
    List<Configuration<Atom, Bond>> configs = new ArrayList<>();

    for (Atom atom : fragment.getAtomList()) {

      AtomParity parity = atom.getAtomParity();
      if (parity == null)
        continue;
      Atom[] carriers = Arrays.copyOf(parity.getAtomRefs4(),
                                      4);
      for (int i = 0; i < carriers.length; i++) {
        // deoxyhydrogen too?
        if (carriers[i].equals(AtomParity.hydrogen)) {
          carriers[i] = atom;
        }
      }

      int cfg = 0;
      switch (parity.getParity()) {
        case -1:
          cfg = Tetrahedral.LEFT;
          break;
        case 1:
          cfg = Tetrahedral.RIGHT;
          break;
      }

      if (cfg != 0)
        configs.add(new Tetrahedral<Atom, Bond>(atom, carriers, cfg));
    }

    for (Bond bond : fragment.getBondSet()) {
      BondStereo bstereo = bond.getBondStereo();
      if (bstereo == null)
        continue;
      Atom[] ref = bstereo.getAtomRefs4();
      int        cfg     = 0;
      switch (bstereo.getBondStereoValue()) {
        case CIS:
          cfg = Sp2Bond.TOGETHER;
          break;
        case TRANS:
          cfg = Sp2Bond.OPPOSITE;
          break;
      }
      configs.add(new Sp2Bond<>(bond,
                                new Atom[]{ref[1], ref[2]},
                                new Atom[]{ref[0], ref[3]},
                                cfg));
    }

    return configs;
  }

  public static void label(Fragment fragment)
  {
    new OpsinLabeller().label(new OpsinMol(fragment),
                              createCfgs(fragment));
  }
}
