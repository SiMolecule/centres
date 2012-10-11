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


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author John May
 */
public class TestChEBI {

    public static void main(String[] args) throws IOException, InterruptedException {


    }}
//        IteratingMDLReader reader = new IteratingMDLReader(new BufferedInputStream(new FileInputStream("/databases/chebi/ChEBI_lite.sdf"), 4096),
//                                                           SilentChemObjectBuilder.getInstance(),
//                                                           true);
//
//        List<IAtomContainer> containers = new ArrayList<IAtomContainer>(12000);
//
//        long rStart = System.currentTimeMillis();
//        while (reader.hasNext()) {
//            IAtomContainer container = reader.next();
//            if (container.getAtomCount() < 50)
//                containers.add(container);
//        }
//        long rEnd = System.currentTimeMillis();
//
//        reader.close();
//
//        System.out.println("Read: " + (rEnd - rStart) + " ms");
//
//        List<IAtomContainer> bad = new ArrayList<IAtomContainer>();
//        IsotopeFactory factory = IsotopeFactory.getInstance(SilentChemObjectBuilder.getInstance());
//
//        //Set<String> ignore = new HashSet<String>(Arrays.asList("CHEBI:3499", "CHEBI:16155", "CHEBI:16157", "CHEBI:16322", "CHEBI:16323", "CHEBI:16324", "CHEBI:16507", "CHEBI:16508", "CHEBI:17401", "CHEBI:17402", "CHEBI:17403", "CHEBI:17404", "CHEBI:17405", "CHEBI:17406", "CHEBI:17407", "CHEBI:17409", "CHEBI:17410", "CHEBI:17411"));
//
//        long tStart = System.currentTimeMillis();
//        for (int i = 0; i < containers.size(); i++) {
//            IAtomContainer container = containers.get(i);
//            try {
//                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container);
//                for (IAtom atom : container.atoms()) {
//                    if (atom.getSymbol().equals("R"))
//                        atom.setMassNumber(0);
//                    if (atom.getMassNumber() == null) {
//                        bad.add(container);
//                    }
//                }
//                CDKHueckelAromaticityDetector.detectAromaticity(container);
//            } catch (CDKException ex) {
//                System.err.println(ex.getMessage());
//                bad.add(container);
//            }
//        }
//
//        //containers = containers.subList(14500, containers.size());
//
//        System.out.println(bad.size());
//
//        long tEnd = System.currentTimeMillis();
//        System.out.println("Typed: " + (tEnd - tStart) + " ms");
//
//        // remove problem molecules
//        containers.removeAll(bad);
//
//
//        final PreferredNameService<ChEBIIdentifier> names = DefaultServiceManager.getInstance().getService(ChEBIIdentifier.class,
//                                                                                                           PreferredNameService.class);
//        final List<ChEBIIdentifier> timeout = new ArrayList<ChEBIIdentifier>();
//
//
//        int count = 0;
//        final Map<Identifier, Long> timing = new HashMap<Identifier, Long>();
//
//        CDKPerceptor perceptor = new CDKPerceptor();
//
//        CSVWriter writer = new CSVWriter(new FileWriter("/Users/johnmay/Desktop/chebi-centres-" + getDateTime() + ".tsv"), '\t', '\0');
//        int missed = 0;
//        long pStart = System.currentTimeMillis();
//        for (final IAtomContainer container : containers) {
//            try {
//                perceptor.perceive(container);
//                //append(container, writer);
//            } catch (WarpCoreEjection ex) {
//                System.err.println(ex.getMessage());
//                missed++;
//            }
//        }
//        long pEnd = System.currentTimeMillis();
//        writer.close();
//
//
//        System.out.println("Perceived in " + (pEnd - pStart) + " ms");
//        System.out.println("Succeeded for  " + (containers.size() - missed) / (double) containers.size() * 100 + "% of " + containers.size() + " structures");
//
//    }
//
//
//    private final static String getDateTime() {
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
//        df.setTimeZone(TimeZone.getTimeZone("GMT+1"));
//        return df.format(new Date());
//    }
//
//
//    public static void append(IAtomContainer container, CSVWriter writer) {
//        String id = (String) container.getProperty("ChEBI ID");
//        for (IAtom atom : container.atoms()) {
//            Descriptor descriptor = (Descriptor) atom.getProperty("descriptor");
//            if (descriptor != General.NONE && descriptor != null) {
//                writer.writeNext(new String[]{
//                        id,
//                        toString(atom, container),
//                        descriptor.toString()
//                });
//            }
//        }
//        for (IBond bond : container.bonds()) {
//            Descriptor descriptor = (Descriptor) bond.getProperty("descriptor");
//            if (descriptor != General.NONE && descriptor != null) {
//                writer.writeNext(new String[]{
//                        id,
//                        toString(bond, container),
//                        descriptor.toString()
//                });
//            }
//        }
//    }
//
//
//    public static String toString(IAtom atom, IAtomContainer container) {
//        return atom.getSymbol() + (container.getAtomNumber(atom) + 1);
//    }
//
//
//    public static String toString(IBond bond, IAtomContainer container) {
//        return toString(bond.getAtom(0), container) + "=" + toString(bond.getAtom(1), container);
//    }
//
//
//    public static String toString(IBond.Order order) {
//        if (order == IBond.Order.SINGLE) return "-";
//        if (order == IBond.Order.DOUBLE) return "=";
//        if (order == IBond.Order.TRIPLE) return "#";
//        if (order == IBond.Order.QUADRUPLE) return "$";
//        return "?";
//    }



