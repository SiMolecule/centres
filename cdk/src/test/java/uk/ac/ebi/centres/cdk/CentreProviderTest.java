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

package uk.ac.ebi.centres.cdk;

/**
 * @author John May
 */
public class CentreProviderTest {

//    /**
//     * Tests that the central double bond is not identifier as a centre and thus
//     * not labelled as UNSPECIFIED
//     */
//    @Test public void testIminoethenol() {
//
//        IAtomContainer container = MolLoader.loadCML(getClass().getResourceAsStream("2-iminoethen-1-ol.xml"));
//
//        Collection<Centre<IAtom>> centres = new CDKCentreProvider(container).getCentres(new CDKManager(container));
//
//        Assert.assertTrue("central bond should not be provided as a centre",
//                          centres.isEmpty());
//
//    }
//
//    @Test
//    public void testGetCentres() throws Exception, IOException, ExecutionException, TimeoutException, InterruptedException {
//
////        final IAtomContainer container = CMLLoader.loadCML(getClass().getResourceAsStream("myo-inositol.xml"));
////
////        // setting correct properties :/
////        for (IAtom atom : container.atoms()) {
////            atom.setAtomicNumber(PeriodicTable.getAtomicNumber(atom.getSymbol()));
////            atom.setMassNumber(IsotopeFactory.getInstance(atom.getBuilder()).getMajorIsotope(atom.getSymbol()).getMassNumber());
////        }
////        AtomContainerManipulator.percieveAtomTypesAndConfigureUnsetProperties(container);
////
////        long start = System.currentTimeMillis();
////        System.out.println("Number of centres: " + new CDKCentreProvider(container).getCentres(new DefaultDescriptorManager<IAtom>()).size());
////        long end = System.currentTimeMillis();
////
////        System.out.println(end - start);
////
////        PriorityRule<IAtom> rules = new CombinedRule<IAtom>(
////                new AtomicNumberRule<IAtom>(new AtomicNumberAccessor<IAtom>() {
////                    @Override
////                    public int getAtomicNumber(IAtom atom) {
////                        return atom.getAtomicNumber();
////                    }
////                }),
////                new MassNumberRule<IAtom>(new MassNumberAccessor<IAtom>() {
////                    @Override
////                    public int getMassNumber(IAtom atom) {
////                        return atom.getMassNumber();
////                    }
////                }),
////                new ZERule<IAtom>(),
////                new PairRule<IAtom>(new PrimaryDescriptor<IAtom>()),
////                new RSRule<IAtom>(new PrimaryDescriptor<IAtom>())
////        );
////
////        PriorityRule<IAtom> auxrule = new CombinedRule<IAtom>(
////                new AtomicNumberRule<IAtom>(new AtomicNumberAccessor<IAtom>() {
////                    @Override
////                    public int getAtomicNumber(IAtom atom) {
////                        return atom.getAtomicNumber();
////                    }
////                }),
////                new MassNumberRule<IAtom>(new MassNumberAccessor<IAtom>() {
////                    @Override
////                    public int getMassNumber(IAtom atom) {
////                        return atom.getMassNumber();
////                    }
////                }),
////                new ZERule<IAtom>(),
////                new PairRule<IAtom>(new AuxiliaryDescriptor<IAtom>()),
////                new RSRule<IAtom>(new AuxiliaryDescriptor<IAtom>()));
////
////        final Perceptor<IAtom> perceptor = new DefaultPerceptor<IAtom>(rules,
////                                                                       auxrule,
////                                                                       new CDK2DSignCalculator());
////
////
////        perceptor.perceive(new CDKCentreProvider(container), new CDKManager(container));
////
////        perceptor.shutdown();
////
////        Centre<IAtom> centre = new ArrayList<Centre<IAtom>>(new CDKCentreProvider(container).getCentres(new DefaultDescriptorManager<IAtom>())).get(0);
////
////
////        Digraph<IAtom> digraph = new ConnectionTableDigraph<IAtom>(centre,
////                                                                   new DefaultDescriptorManager<IAtom>(),
////                                                                   new CDKConnectionTable(container));
////
////        CytoscapeWriter<IAtom> writer = new CytoscapeWriter<IAtom>(new File("/Users/johnmay/Desktop/digraph"), digraph) {
////            @Override
////            public void mapAttributes(IAtom atom, Map<String, String> map) {
////                map.put("atom.symbol", atom.getSymbol());
////                map.put("atom.number", atom.getProperty("number").toString());
////            }
////        };
////        System.out.println(centre);
////        writer.writeSif();
////        writer.writeAttributes();
////        writer.close();
////
////
////        for (IAtom atom : container.atoms()) {
////            System.out.println(atom.getSymbol() + atom.getProperty("number")
////                                       + ": " + atom.getProperty("descriptor"));
////        }
////        System.out.println("Bonds:");
////        for (IBond bond : container.bonds()) {
////            System.out.println(bond.getAtom(0).getSymbol() + "=" + bond.getAtom(1).getSymbol() + container.getBondNumber(bond)
////                                       + ": " + bond.getProperty("descriptor"));
////        }
//
//
//    }
//
//
//    /**
//     * Simple method to dump the graph to a cytoscape file
//     *
//     * @param name
//     * @param centre
//     *
//     * @throws java.io.IOException
//     */
//    public void write(String name, Centre centre) throws IOException {
//
//        AbstractNode<IAtom> ligand = ((AbstractNode<IAtom>) centre);
//        ligand.getProvider().build();
//
//        Digraph digraph = (Digraph) ((AbstractNode) centre).getProvider();
//
//        CytoscapeWriter<IAtom> writer = new CytoscapeWriter<IAtom>(new File(name),
//                                                                   digraph) {
//            @Override
//            public void mapAttributes(IAtom atom, Map<String, String> map) {
//                map.put("symbol", atom.getSymbol());
//                map.put("number", atom.getProperty("number").toString());
//            }
//        };
//        writer.writeSif();
//        writer.writeAttributes();
//        writer.close();
//
//    }

}
