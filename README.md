# Stereochemistry Labelling Done Good

Centres is an Open Source Java library for that allows perception and labelling of stereogenic centres in chemical structures using the [Cahn-Ingold-Prelog priority rules](https://en.wikipedia.org/wiki/Cahn%E2%80%93Ingold%E2%80%93Prelog_priority_rules).

### How to Use

You can use Centres interatively at <a href="http://www.simolecule.com/cdkdepict">http://www.simolecule.com/cdkdepict</a> by selecting the annotation "CIP Stereo Label". Alternatively you can download the ``centres.jar`` (built on CDK) from the releases page. This JAR can be used to label and test an SD or SMILES file.

```
java -jar centres.jar input.sdf
```

To run the benchmark tests pressented by [Hanson *et al*](https://chemrxiv.org/articles/Algorithmic_Analysis_of_Cahn-Ingold-Prelog_Rules_of_Stereochemistry_Proposals_for_Revised_Rules_and_a_Guide_for_Machine_Implementation/6342881) (note limitations below) run the following commands. The files can be downloaded from https://cipvalidationsuite.github.io/ValidationSuite/.

```
java -jar centres.jar compounds_2d.sdf -e CIP_LABELS
java -jar centres.jar compounds_3d.sdf -e CIP_LABELS
java -jar centres.jar compounds.smi -e 1
```

### Key Features
* Generic library allowing [dependency injection](http://en.wikipedia.org/wiki/Dependency_injection) of any molecule/atom object representation. Currently
supported 'endpoints':
  * [Chemistry Development Kit](github.com/cdk/cdk)
  * [OPSIN](https://bitbucket.org/dan2097/opsin/)
  * [JChem](https://chemaxon.com/products/jchem-engines)
* Perception and labelling of tetrahedral (__R__/__S__/__r__/__s__) and geometric double bonds (__Z__/__E__).
* Implementation of the Cahn-Ingold-Prelog (CIP) priority rules as they appear in Prelog and Helmchen, 1982
* Implementation of the Cahn-Ingold-Prelog (CIP) priority rules as they appear in Nomenclature of Organic Chemistry, IUPAC Recommendations and Preferred Names 2013

### Install

`jchem` and `opsin` backends do not currently pass the internal validation tests and should be skipped when running tests.

```
mvn install -pl '!jchem,!opsin'
```

alternatively skip the tests:

```
mvn install -DskipTests
```

### License
[Lesser General Public License 3.0](http://www.gnu.org/licenses/lgpl.html)

### Authors
John Mayfield (né May)

### Limitations
 - Helicene and chirality planes are not supported
 - SMILES does not capture axial atropisomerism but this is supported in 2D/3D
 - When using CDK the 3D SDfile must find stereocentres with little information available, the current algorithm does not find all centres in the test sets by default. To prevent this a modified version of CDK is used in Centres that captures all possible tetrahedral/double bond stereochemistries.

### References
* Robert M. Hanson John Mayfield Mikko J. Vainio Andrey Yerin Dmitry Redkin Sophia Musacchio. Algorithmic Analysis of Cahn-Ingold-Prelog Rules of Stereochemistry: Proposals for Revised Rules and a Guide for Machine Implementation. __Submitted__ [preprint](https://chemrxiv.org/articles/Algorithmic_Analysis_of_Cahn-Ingold-Prelog_Rules_of_Stereochemistry_Proposals_for_Revised_Rules_and_a_Guide_for_Machine_Implementation/6342881).
* Prelog and Helmchen. Basic Principles of the CIP-System and Proposals for a Revision. __Angewandte Chemie International Edition__ 21 (1982) 567-683
* Perdih and Rmzinger. Stereo-chemistry and Sequence Rules A Proposal for Modification of Cahn-Ingold-Prelog System. __Tetrahedron: Asymmetry__ Vol 5 (1994) 835-861
