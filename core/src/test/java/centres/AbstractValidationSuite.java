package centres;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import uk.ac.ebi.centres.Descriptor;

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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public abstract class AbstractValidationSuite {

  public enum Context {
    Atom,
    Bond
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
        case E:
        case Z:
        case e:
        case z:
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

  private static final Pattern PATTERN = Pattern.compile("([A|B])?(\\d+)?([RSrsMPmpEZez])");

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
        Descriptor cip    = Descriptor.valueOf(matcher.group(3));
        Integer    idx    = strnum == null ? 0 : Integer.parseInt(strnum);
        Context    ctx    = strctx == null ? null : strctx.equals("A") ? Context.Atom : Context.Bond;
        if (ctx != null)
          ciplabels.add(new CipLabel(idx - 1, ctx, cip));
        else
          ciplabels.add(new CipLabel(idx - 1, cip));
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
