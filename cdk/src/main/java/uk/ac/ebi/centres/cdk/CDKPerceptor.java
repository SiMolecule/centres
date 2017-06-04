/*
 * Copyright (c) 2012. John May
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package uk.ac.ebi.centres.cdk;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import uk.ac.ebi.centres.DefaultPerceptor;
import uk.ac.ebi.centres.priority.Rule1a;
import uk.ac.ebi.centres.priority.Rules;
import uk.ac.ebi.centres.priority.Rule1b;
import uk.ac.ebi.centres.priority.Rule2;
import uk.ac.ebi.centres.priority.access.AtomicNumberAccessor;
import uk.ac.ebi.centres.priority.access.MassNumberAccessor;
import uk.ac.ebi.centres.priority.access.PsuedoAtomicNumberModifier;
import uk.ac.ebi.centres.priority.access.descriptor.PrimaryOrAuxiliary;
import uk.ac.ebi.centres.priority.access.descriptor.PrimaryDescriptor;
import uk.ac.ebi.centres.priority.Rule4b;
import uk.ac.ebi.centres.priority.Rule4c;
import uk.ac.ebi.centres.priority.Rule5;
import uk.ac.ebi.centres.priority.Rule3;

/**
 * @author John May
 */
public class CDKPerceptor extends DefaultPerceptor<IAtom> {

    private static final MassNumberAccessor<IAtom> cdkMassNumberAccessor = new MassNumberAccessor<IAtom>() {
        @Override
        public int getMassNumber(IAtom atom) {
            Integer massnum = atom.getMassNumber();
            if (massnum == null)
                return 0;
            return massnum;
        }
    };

    private static int atomicNumber(IAtom atom) {
        final Integer elem = atom.getAtomicNumber();
        if (elem == null) return 0;
        return elem;
    }

    public CDKPerceptor() {
        super(new Rules<IAtom>(
                      new Rule1a<IAtom>(
                              new PsuedoAtomicNumberModifier<IAtom>(
                                      new AtomicNumberAccessor<IAtom>() {
                                          @Override
                                          public int getAtomicNumber(IAtom atom) {
                                              return atomicNumber(atom);
                                          }
                                      }))
                      ,
                      new Rule1b<IAtom>(),
                      new Rule2<IAtom>(cdkMassNumberAccessor),
                      new Rule3<IAtom>(),
                      new Rule4b<IAtom>(new PrimaryDescriptor<IAtom>()),
                      new Rule4c<IAtom>(new PrimaryDescriptor<IAtom>()),
                      new Rule5<IAtom>(new PrimaryDescriptor<IAtom>())
              ),
              new Rules<IAtom>(
                      new Rule1a<IAtom>(
                              new PsuedoAtomicNumberModifier<IAtom>(
                                      new AtomicNumberAccessor<IAtom>() {
                                          @Override
                                          public int getAtomicNumber(IAtom atom) {
                                              return atomicNumber(atom);
                                          }
                                      })),
                      new Rule2<IAtom>(cdkMassNumberAccessor),
                      new Rule3<IAtom>(),
                      new Rule4b<IAtom>(new PrimaryOrAuxiliary<IAtom>()),
                      new Rule4c<IAtom>(new PrimaryOrAuxiliary<IAtom>()),
                      new Rule5<IAtom>(new PrimaryOrAuxiliary<IAtom>())
              ));
    }

    public void perceive(IAtomContainer container) {
        perceive(new CDKCentreProvider(container), new CDKManager(container));
    }

}
