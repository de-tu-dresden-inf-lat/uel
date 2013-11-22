package de.tudresden.inf.lat.uel.plugin.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import de.tudresden.inf.lat.uel.plugin.processor.PluginGoal;
import de.tudresden.inf.lat.uel.plugin.processor.UelModel;
import de.tudresden.inf.lat.uel.plugin.processor.UelProcessorFactory;
import de.tudresden.inf.lat.uel.plugin.type.AtomManager;
import de.tudresden.inf.lat.uel.plugin.type.OWLUelClassDefinition;
import de.tudresden.inf.lat.uel.plugin.type.UnifierTranslator;
import de.tudresden.inf.lat.uel.plugin.ui.UelController;
import de.tudresden.inf.lat.uel.type.api.Atom;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.ConceptName;
import de.tudresden.inf.lat.uel.type.impl.ExistentialRestriction;

public class AlternativeUelStarter {

	public static final String classPrefix = "http://uel.sourceforge.net/entities/auxclass#A";
	private OWLOntology auxOntology;
	private int classCounter = 0;
	private Map<OWLClassExpression, OWLClass> mapOfAuxClassExpr = new HashMap<OWLClassExpression, OWLClass>();
	private OWLOntology ontology;
	private UelProcessor uelProcessor;

	/**
	 * Constructs a new UEL starter.
	 * 
	 * @param ontology
	 *            OWL ontology
	 */
	public AlternativeUelStarter(OWLOntology ontology) {
		if (ontology == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.ontology = ontology;
		try {
			this.auxOntology = ontology.getOWLOntologyManager()
					.createOntology();
		} catch (OWLOntologyCreationException e) {
			throw new RuntimeException(e);
		}
	}

	public OWLClass findAuxiliaryDefinition(OWLClassExpression expr) {
		OWLClass ret = null;
		if (expr.isClassExpressionLiteral()) {
			ret = expr.asOWLClass();
		} else {
			ret = this.mapOfAuxClassExpr.get(expr);
			if (ret == null) {
				this.classCounter++;
				OWLOntologyManager manager = this.auxOntology
						.getOWLOntologyManager();
				OWLDataFactory factory = manager.getOWLDataFactory();
				IRI iri = IRI.create(classPrefix + classCounter);
				ret = factory.getOWLClass(iri);

				this.mapOfAuxClassExpr.put(expr, ret);

				OWLAxiom newDefinition = factory.getOWLEquivalentClassesAxiom(
						ret, expr);
				this.auxOntology.getOWLOntologyManager().addAxiom(auxOntology,
						newDefinition);
			}
		}
		return ret;
	}

	public String getId(OWLClass cls) {
		return UelController.getId(cls);
	}

	private String isVariable(ConceptName name, AtomManager atomManager,
			Set<Integer> userVariables) {
		if (name.isVariable()) {
			if (userVariables.contains(atomManager.getAtoms().addAndGetIndex(
					name))) {
				return "uv";
			} else {
				return "v";
			}
		} else {
			return "c";
		}
	}

	public static void main(String[] args) {
		int argIdx = 0;
		String mainFilename = "";
		String subsFilename = "";
		String dissubsFilename = "";
		String varFilename = "";
		int processorIdx = 0;
		while (argIdx < args.length) {
			switch (args[argIdx]) {
			case "-s":
				argIdx++;
				subsFilename = args[argIdx];
				break;
			case "-d":
				argIdx++;
				dissubsFilename = args[argIdx];
				break;
			case "-v":
				argIdx++;
				varFilename = args[argIdx];
				break;
			case "-p":
				argIdx++;
				processorIdx = Integer.parseInt(args[argIdx]) - 1;
				break;
			case "-h":
				printSyntax();
				return;
			default:
				mainFilename = args[argIdx];
				break;
			}
			argIdx++;
		}

		AlternativeUelStarter starter = new AlternativeUelStarter(
				loadOntology(mainFilename));
		Set<OWLSubClassOfAxiom> subsumptions = loadOntology(subsFilename)
				.getAxioms(AxiomType.SUBCLASS_OF, false);
		Set<OWLSubClassOfAxiom> dissubsumptions = loadOntology(dissubsFilename)
				.getAxioms(AxiomType.SUBCLASS_OF, false);
		Set<OWLClass> variables = loadVariables(varFilename);
		String processorName = UelProcessorFactory.getProcessorNames().get(
				processorIdx);

		Iterator<Set<OWLUelClassDefinition>> result = starter
				.modifyOntologyAndSolve(subsumptions, dissubsumptions,
						variables, processorName);
		int unifierIdx = 1;
		while (result.hasNext()) {
			System.out.println("Unifier " + unifierIdx + ":");
			Set<OWLUelClassDefinition> unifier = result.next();
			for (OWLUelClassDefinition def : unifier) {
				System.out
						.println(def.asOWLEquivalentClassesAxiom().toString());
			}
			System.out.println();
			unifierIdx++;
		}

		System.out.println("Stats:");
		for (Entry<String, String> entry : starter.getStats()) {
			System.out.println(entry.getKey() + ":");
			System.out.println(entry.getValue());
		}
	}

	private static void printSyntax() {
		System.out
				.println("Usage: uel [-s subsumptions.owl] [-d dissubsumptions.owl] [-v variables.txt] [-p processorIndex] [-h] [ontology.owl]");
	}

	private static Set<OWLClass> loadVariables(String filename) {
		if (filename.isEmpty()) {
			return Collections.emptySet();
		}
		try {
			OWLDataFactory factory = OWLManager.createOWLOntologyManager()
					.getOWLDataFactory();
			BufferedReader input = new BufferedReader(new FileReader(new File(
					filename)));
			Set<OWLClass> variables = new HashSet<OWLClass>();
			String line = "";
			while (line != null) {
				line = input.readLine();
				if (line != null) {
					variables.add(factory.getOWLClass(IRI.create(line)));
				}
			}
			return variables;
		} catch (IOException ex) {
			ex.printStackTrace();
			System.exit(-1);
			return null;
		}
	}

	private static OWLOntology loadOntology(String filename) {
		try {
			OWLOntologyManager ontologyManager = OWLManager
					.createOWLOntologyManager();
			if (filename.isEmpty()) {
				return ontologyManager.createOntology();
			}
			InputStream input = new FileInputStream(new File(filename));
			ontologyManager.loadOntologyFromOntologyDocument(input);
			return ontologyManager.getOntologies().iterator().next();
		} catch (IOException | OWLOntologyCreationException ex) {
			ex.printStackTrace();
			System.exit(-1);
			return null;
		}
	}

	public Iterator<Set<OWLUelClassDefinition>> modifyOntologyAndSolve(
			Set<OWLSubClassOfAxiom> subsumptions,
			Set<OWLSubClassOfAxiom> dissubsumptions, Set<OWLClass> variables,
			String processorName) {

		// add two definitions for each subsumption to the ontology
		for (OWLSubClassOfAxiom subsumption : subsumptions) {
			findAuxiliaryDefinition(subsumption.getSubClass());
			findAuxiliaryDefinition(subsumption.getSuperClass());
		}

		// construct (small!) disequations from the dissubsumptions
		Set<OWLEquivalentClassesAxiom> disequations = new HashSet<OWLEquivalentClassesAxiom>();
		for (OWLSubClassOfAxiom dissubsumption : dissubsumptions) {
			OWLClass auxSubClass = findAuxiliaryDefinition(dissubsumption
					.getSubClass());
			OWLClass auxSuperClass = findAuxiliaryDefinition(dissubsumption
					.getSuperClass());
			OWLDataFactory factory = this.auxOntology.getOWLOntologyManager()
					.getOWLDataFactory();

			OWLClassExpression conjunction = factory
					.getOWLObjectIntersectionOf(auxSubClass, auxSuperClass);
			OWLClass auxConjunction = findAuxiliaryDefinition(conjunction);
			OWLEquivalentClassesAxiom disequation = factory
					.getOWLEquivalentClassesAxiom(auxSubClass, auxConjunction);
			disequations.add(disequation);
		}

		UelModel model = new UelModel();
		model.loadOntology(this.ontology, this.auxOntology);

		PluginGoal goal = new PluginGoal(model.getAtomManager(),
				model.getOntology());

		// add the subsumptions themselves to the PluginGoal
		for (OWLSubClassOfAxiom subsumption : subsumptions) {
			String subClassId = getId(findAuxiliaryDefinition(subsumption
					.getSubClass()));
			String superClassId = getId(findAuxiliaryDefinition(subsumption
					.getSuperClass()));

			// System.out.println(subClassId + " subsumed by " + superClassId);

			goal.addGoalSubsumption(subClassId, superClassId);
		}

		// add the disequations
		for (OWLEquivalentClassesAxiom disequation : disequations) {
			Iterator<OWLClassExpression> expressions = disequation
					.getClassExpressions().iterator();
			String class1Id = getId((OWLClass) expressions.next());
			String class2Id = getId((OWLClass) expressions.next());

			// System.out.println(class1Id + " not equivalent to " + class2Id);

			goal.addGoalDisequation(class1Id, class2Id);
		}

		// translate the variables to the IDs, and mark them as variables in the
		// PluginGoal
		AtomManager atomManager = goal.getAtomManager();
		for (OWLClass var : variables) {
			String name = getId(var);
			ConceptName conceptName = atomManager.createConceptName(name, true);
			Integer atomId = atomManager.getAtoms().addAndGetIndex(conceptName);
			// System.out.println("user variable: " + name);
			goal.makeUserVariable(atomId);
		}

		// mark the auxiliary variables as auxiliary
		for (OWLClass auxVar : mapOfAuxClassExpr.values()) {
			String name = getId(auxVar);
			ConceptName conceptName = atomManager.createConceptName(name, true);
			Integer atomId = atomManager.getAtoms().addAndGetIndex(conceptName);
			// System.out.println("aux. variable: " + name);
			goal.makeAuxiliaryVariable(atomId);
		}

		goal.updateUelInput();
		// System.out.println("final number of equations: "
		// + goal.getUelInput().getEquations().size());

		// output unification problem for debugging
		// print(goal.getUelInput().getEquations(), goal.getAtomManager(),
		// goal.getUelInput().getUserVariables());
		uelProcessor = UelProcessorFactory.createProcessor(processorName,
				goal.getUelInput());

		UnifierTranslator translator = new UnifierTranslator(ontology
				.getOWLOntologyManager().getOWLDataFactory(), atomManager, goal
				.getUelInput().getUserVariables(), goal.getAuxiliaryVariables());
		return new UnifierIterator(uelProcessor, translator);
	}

	public List<Entry<String, String>> getStats() {
		return uelProcessor.getInfo();
	}

	private void print(Integer atomId, AtomManager atomManager,
			Set<Integer> userVariables) {
		Atom atom = atomManager.getAtoms().get(atomId);
		if (atom.isExistentialRestriction()) {
			ExistentialRestriction ex = (ExistentialRestriction) atom;
			System.out.print("(exists "
					+ atomManager.getRoleName(ex.getRoleId()) + " "
					+ atomManager.getConceptName(ex.getConceptNameId()) + "["
					+ isVariable(ex.getChild(), atomManager, userVariables)
					+ "])");
		} else {
			ConceptName name = (ConceptName) atom;
			System.out
					.print(atomManager.getConceptName(name.getConceptNameId())
							+ "["
							+ isVariable(name, atomManager, userVariables)
							+ "]");
		}
	}

	private void print(Set<Equation> equations, AtomManager atomManager,
			Set<Integer> userVariables) {
		for (Equation eq : equations) {
			print(eq.getLeft(), atomManager, userVariables);
			System.out.print(" = ");
			for (Integer atomId : eq.getRight()) {
				print(atomId, atomManager, userVariables);
				System.out.print(" + ");
			}
			System.out.println();
		}
	}

}
