package centres;

import com.simolecule.centres.BaseMol;
import com.simolecule.centres.Descriptor;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(Parameterized.class)
public abstract class AbstractValidationSuite {

  public enum Context {
    Atom,
    Bond
  }

  public static abstract class GenSmiles {
    public abstract String generate(BaseMol mol);
  }

  public static final class TestUnit {
    private final String         smiles;
    private final List<CipLabel> labels;
    private String name = "";

    public TestUnit(String smiles, List<CipLabel> labels)
    {
      this.smiles = smiles;
      this.labels = labels;
    }

    public TestUnit(String smiles, CipLabel... labels)
    {
      this.smiles = smiles;
      this.labels = Arrays.asList(labels);
    }

    public TestUnit(String smiles, Descriptor exp)
    {
      this.smiles = smiles;
      this.labels = Collections.singletonList(new CipLabel(0, exp));
    }

    public String getSmiles()
    {
      return smiles;
    }

    public List<CipLabel> getLabels()
    {
      return labels;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }
  }

  public static final class CipLabel {
    private final int        idx;
    private final Context    ctx;
    private final Descriptor exp;

    public CipLabel(int idx, Context ctx, Descriptor exp)
    {
      this.idx = idx;
      this.ctx = ctx;
      this.exp = exp;
    }

    public static List<CipLabel> build(Integer idx1, Descriptor exp1, Integer idx2, Descriptor exp2, Object... objs)
    {
      if ((objs.length & 0x1) != 0)
        throw new IllegalArgumentException("Expected even number of arguments!");
      List<CipLabel> labels = new ArrayList<>();
      labels.add(new CipLabel(idx1, exp1));
      labels.add(new CipLabel(idx2, exp2));
      for (int i = 0; i < objs.length; i += 2) {
        labels.add(new CipLabel((Integer) objs[i], (Descriptor) objs[i + 1]));
      }
      return labels;
    }

    public CipLabel(int idx, Descriptor exp)
    {
      this.idx = idx;
      this.exp = exp;
      switch (exp) {
        case seqTrans:
        case seqCis:
        case E:
        case Z:
        case M:
        case P:
        case m:
        case p:
          this.ctx = Context.Bond;
          break;
        case R:
        case S:
        case r:
        case s:
          this.ctx = Context.Atom;
          break;
        default:
          throw new IllegalArgumentException("Cannot determine default context from descriptor.");
      }
    }

    public int getIdx()
    {
      return idx;
    }

    public Context getCtx()
    {
      return ctx;
    }

    public Descriptor getExp()
    {
      return exp;
    }
  }

  private static final Pattern PATTERN = Pattern.compile("([A|B])?(\\d+)?([URSrsMPmpEZez])");


  public <A, B> void check(BaseMol<A, B> mol, GenSmiles callback)
  {
    if (callback == null) {
      callback = new GenSmiles() {
        @Override
        public String generate(BaseMol mol)
        {
          return "No SMILES callback provided!";
        }
      };
    }

    Set<Object> checked  = new HashSet<>();
    int         atomIter = 0;
    int         bondIter = 0;
    String      smiles = callback.generate(mol);

    for (CipLabel label : expected.getLabels()) {
      switch (label.getCtx()) {
        case Atom:


          if (label.getIdx() < 0) {
            while (atomIter < mol.getNumAtoms()) {
              A          atom   = mol.getAtom(atomIter);
              Descriptor actual = mol.getAtomProp(atom, BaseMol.CIP_LABEL_KEY);
              if (actual != null &&
                  actual != Descriptor.Unknown) {
                checked.add(atom);
                Assert.assertThat("Atom idx=" + atomIter + " expected=" + label.getExp() + " was=" + actual +
                                  "\n" + smiles,
                                  actual, CoreMatchers.is(label.getExp()));
                atomIter++;
                break;
              }
              atomIter++;
            }
            Assert.assertTrue("Label not found, expected " + label.getExp() + "\n" + smiles,
                              atomIter < mol.getNumAtoms());
          } else {
            A atom = mol.getAtom(label.getIdx());
            checked.add(atom);
            Assert.assertNotNull("No atom at index " + label.getIdx(), atom);
            Descriptor actual = mol.getAtomProp(atom, BaseMol.CIP_LABEL_KEY);
            Assert.assertThat("Atom idx=" + label.getIdx() + " expected=" + label.getExp() + " was=" + actual +
                              "\n" + smiles,
                              actual,
                              CoreMatchers.is(label.getExp()));
          }
          break;
        case Bond:

          if (label.getIdx() < 0) {
            while (bondIter < mol.getNumBonds()) {
              B bond = mol.getBond(bondIter);
              checked.add(bond);
              Descriptor actual = mol.getBondProp(bond, BaseMol.CIP_LABEL_KEY);
              if (actual != null &&
                  actual != Descriptor.Unknown) {
                Assert.assertThat("Bond idx=" + bondIter + " expected=" + label.getExp() + " was=" + actual +
                                  "\n" + smiles,
                                  actual, CoreMatchers.is(label.getExp()));
                bondIter++;
                break;
              }
              bondIter++;
            }
            Assert.assertTrue("Label not found, expected " + label.getExp() + "\n" + smiles,
                              bondIter < mol.getNumBonds());
          } else {
            B bond = mol.getBond(label.getIdx());
            checked.add(bond);
            Assert.assertNotNull("No atom at index " + label.getIdx(), bond);
            Descriptor actual = mol.getBondProp(bond, BaseMol.CIP_LABEL_KEY);
            Assert.assertThat("Bond idx=" + label.getIdx() + " expected=" + label.getExp() + " was=" + actual +
                              "\n" + smiles,
                              actual,
                              CoreMatchers.is(label.getExp()));
          }
          break;

      }
    }

    for (int i = 0; i < mol.getNumAtoms(); i++) {
      A atom = mol.getAtom(i);
      if (checked.contains(atom))
        continue;
      Descriptor desc = mol.getAtomProp(atom, BaseMol.CIP_LABEL_KEY);
      if (desc != null && desc != Descriptor.Unknown)
        Assert.fail("No expected value for Atom idx=" + mol.getAtomIdx(atom) + " was=" + desc + "\n" + smiles);
    }
    for (int i = 0; i < mol.getNumBonds(); i++) {
      B bond = mol.getBond(i);
      if (checked.contains(bond))
        continue;
      Descriptor desc = mol.getBondProp(bond, BaseMol.CIP_LABEL_KEY);
      if (desc != null && desc != Descriptor.Unknown)
        Assert.fail("No expected value for Bond idx=" + mol.getBondIdx(bond) + " was=" + desc + "\n" + smiles);
    }

    if (expected.getLabels().isEmpty()) {
      System.err.println("No labels assigned for: " + expected.getSmiles());
    }
  }

  private static List<CipLabel> parse(String str)
  {
    if (str.isEmpty())
      return Collections.emptyList();
    String[]       labels    = str.split(",");
    List<CipLabel> ciplabels = new ArrayList<>();
    for (String label : labels) {
      Matcher matcher = PATTERN.matcher(label);
      if (matcher.matches()) {
        String     strctx = matcher.group(1);
        String     strnum = matcher.group(2);
        Descriptor cip    = Descriptor.parse(matcher.group(3));
        if (cip != Descriptor.Unknown) {
          Integer idx = strnum == null ? 0 : Integer.parseInt(strnum);
          Context ctx = strctx == null ? null : strctx.equals("A") ? Context.Atom : Context.Bond;
          if (ctx != null)
            ciplabels.add(new CipLabel(idx - 1, ctx, cip));
          else
            ciplabels.add(new CipLabel(idx - 1, cip));
        }
      } else {
        System.err.println("Cannot parse label: " + label);
      }
    }
    return ciplabels;
  }

  private static TestUnit parseLine(String line, int linenum)
  {
    String[] parts = line.split("\\s+", 3);
    if (parts.length < 2)
      parts = new String[]{parts[0], ""};
    String smi    = parts[0];
    String labels = parts[1];
    String name;
    if (parts.length < 3)
      name = "L" + linenum;
    else
      name = parts[2] + " (L" + linenum + ")";
    TestUnit unit = new TestUnit(smi, parse(labels));
    unit.setName(name);
    return unit;
  }

  @Parameterized.Parameters(name = "{1}")
  public static Collection<Object[]> load()
  {
    List<Object[]> testunits = new ArrayList<>();
    try (InputStream in = ClassLoader.getSystemResourceAsStream("com/simolecule/centres/validate.smi");
         Reader rdr = new InputStreamReader(in);
         BufferedReader brdr = new BufferedReader(rdr)) {
      String line;
      int    linenum = 0;
      while ((line = brdr.readLine()) != null) {
        linenum++;
        if (line.isEmpty() || line.charAt(0) == '#')
          continue;
        TestUnit tunit = parseLine(line, linenum);
        if (tunit != null)
          testunits.add(new Object[]{tunit, tunit.getName()});
      }
    } catch (IOException e) {
      System.err.println("Could not load validation suite");
    }
    return testunits;
  }

  @Parameterized.Parameter(0)
  public TestUnit expected;

  @Parameterized.Parameter(1)
  public String name;

}
