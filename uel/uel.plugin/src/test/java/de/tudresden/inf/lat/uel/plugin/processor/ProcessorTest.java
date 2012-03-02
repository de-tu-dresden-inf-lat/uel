package de.tudresden.inf.lat.uel.plugin.processor;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasoner;
import de.tudresden.inf.lat.uel.plugin.type.SatAtom;
import de.tudresden.inf.lat.uel.plugin.ui.UelController;
import de.tudresden.inf.lat.uel.plugin.ui.UelView;
import de.tudresden.inf.lat.uel.type.api.Equation;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;

public class ProcessorTest extends TestCase {

	private static final String apath = "src/test/resources/";
	private static final String ontology01 = apath + "testOntology-01.krss";
	private static final String ontology02 = apath + "testOntology-02.krss";
	private static final String ontology03 = apath + "testOntology-03.krss";
	private static final String ontology04 = apath + "testOntology-04.krss";
	private static final String ontology05 = apath + "testOntology-05.krss";
	private static final String ontology06 = apath + "testOntology-06.krss";
	private static final String ontology07 = apath + "testOntology-07.krss";
	private static final String ontology08 = apath + "testOntology-08.krss";
	private static final String ontology09 = apath + "testOntology-09.krss";
	private static final String ontology10 = apath + "testOntology-10.krss";
	private static final String ontology11 = apath + "testOntology-11.krss";
	private static final String ontology12 = apath + "testOntology-12.krss";
	private static final String ontology13 = apath + "testOntology-13.krss";
	private static final String ontology14 = apath + "testOntology-14.krss";
	private static final String ontology15 = apath + "testOntology-15.krss";
	private static final String ontology16 = apath + "testOntology-16.krss";

	private OWLOntology createOntology(InputStream input)
			throws OWLOntologyCreationException {
		OWLOntologyManager ontologyManager = OWLManager
				.createOWLOntologyManager();
		ontologyManager.loadOntologyFromOntologyDocument(input);
		return ontologyManager.getOntologies().iterator().next();
	}

	private OWLReasoner createReasoner(String ontologyStr)
			throws OWLOntologyCreationException {
		JcelReasoner reasoner = new JcelReasoner(
				createOntology(new ByteArrayInputStream(ontologyStr.getBytes())));
		reasoner.precomputeInferences();
		return reasoner;
	}

	private Integer getAtomId(PluginGoal goal, String atomName, boolean undef) {
		if (undef) {
			atomName += PluginGoal.UNDEF_SUFFIX;
		}
		Integer ret = null;
		for (Integer currentAtomId : goal.getSatAtomManager().getIndices()) {
			SatAtom currentAtom = goal.getSatAtomManager().get(currentAtomId);
			if (currentAtom.getId().equals(atomName)) {
				ret = currentAtomId;
			}
		}
		return ret;
	}
	
	public <T> Set<T> set(T ... elements) {
		return new HashSet<T>(Arrays.asList(elements));
	}

	public void test01SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology01, set("A1", "A4"), 16, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test01Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology01, set("A1", "A4"), 1, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test02SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology02, set("A1", "A4"), 64, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test02Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology02, set("A1", "A4"), 1, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test03SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology03, set("Z"), 1, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test03Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology03, set("Z"), 1, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test04SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology04, new HashSet<String>(), 0, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test04Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology04, new HashSet<String>(), 0, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test05SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology05, set("A", "A1", "A2"), 32, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test05Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology05, set("A", "A1", "A2"), 1, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test06SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology06, set("A1", "A2"), 3, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test06Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology06, set("A1", "A2"), 2, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test07SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology07, set("A1", "A2"), 0, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test07Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology07, set("A1", "A2"), 0, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test08SAT() throws OWLOntologyCreationException, IOException {
		// including repetitions
		tryOntology(ontology08, set("A1", "A2", "A4"), 1040, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test08Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology08, set("A1", "A2", "A4"), 3, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test09SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology09, set("A1", "A2"), 0, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test09Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology09, set("A1", "A2"), 0, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test10SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology10, set("A1", "A3"), 2, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test10Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology10, set("A1", "A3"), 2, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test11SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology11, new HashSet<String>(), 1, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test11Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology11, new HashSet<String>(), 1, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test12SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology12, set("A2"), 0, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test12Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology12, set("A2"), 0, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test13SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology13, set("B2"), 0, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test13Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology13, set("B2"), 0, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test14SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology14, set("A", "B5"), 8, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test14Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology14, set("A", "B5"), 1, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test15SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology15, new HashSet<String>(), 0, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test15Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology15, new HashSet<String>(), 0, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	public void test16SAT() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology16, new HashSet<String>(), set("Head_injury", "Severe_injury"), 128, UelProcessorFactory.SAT_PROCESSOR);
	}

	public void test16Rule() throws OWLOntologyCreationException, IOException {
		tryOntology(ontology16, new HashSet<String>(), set("Head_injury", "Severe_injury"), 1, UelProcessorFactory.RULE_BASED_PROCESSOR);
	}

	private void tryOntology(String ontologyName, Set<String> varNames,
			Integer numberOfUnifiers, String processorName)
			throws OWLOntologyCreationException, IOException {
		tryOntology(ontologyName, varNames, new HashSet<String>(), numberOfUnifiers, processorName);
	}
	
	private void tryOntology(String ontologyName, Set<String> varNames,
			Set<String> undefVarNames, Integer numberOfUnifiers, String processorName)
			throws OWLOntologyCreationException, IOException {
		Map<String, OWLClass> idClassMap = new HashMap<String, OWLClass>();
		UelModel uelModel = new UelModel();

		OWLOntology owlOntology = createOntology(new FileInputStream(
				ontologyName));
		uelModel.loadOntology(owlOntology, owlOntology);
		Set<OWLClass> clsSet = owlOntology.getClassesInSignature();
		for (OWLClass cls : clsSet) {
			idClassMap.put(cls.getIRI().getFragment(), cls);
		}

		Set<String> input = new HashSet<String>();
		input.add(idClassMap.get("C").toStringID());
		input.add(idClassMap.get("D").toStringID());
		PluginGoal goal = uelModel.configure(input);

		for (String var : varNames) {
			Integer atomId = getAtomId(goal, idClassMap.get(var).toStringID(), false);
			goal.makeVariable(atomId);
		}
		for (String var : undefVarNames) {
			Integer atomId = getAtomId(goal, idClassMap.get(var).toStringID(), true);
			goal.makeVariable(atomId);
		}

		UelProcessor processor = UelProcessorFactory.createProcessor(
				processorName, goal.getUelInput());
		uelModel.configureUelProcessor(processor);
		
		boolean hasUnifiers = true;
		while (hasUnifiers) {
			hasUnifiers = uelModel.computeNextUnifier();
		}

		List<Set<Equation>> unifiers = uelModel.getUnifierList();
		String goalStr = goal.getGoalEquations();

		UelController controller = new UelController(new UelView(uelModel),
				owlOntology.getOWLOntologyManager());

		for (Set<Equation> unifier : unifiers) {
			String str = controller.getUnifier().toKRSS(unifier);
			String extendedOntology = goalStr + str;

			OWLReasoner reasoner = createReasoner(extendedOntology);
			Node<OWLClass> node = reasoner.getEquivalentClasses(idClassMap
					.get("C"));
			OWLClass elem = idClassMap.get("D");
			assertTrue(node.contains(elem));
		}

		assertEquals(numberOfUnifiers, (Integer) unifiers.size());
	}

}
