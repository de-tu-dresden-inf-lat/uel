/**
 * 
 */
package de.tudresden.inf.lat.uel.plugin.main;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import de.tudresden.inf.lat.jcel.owlapi.main.JcelReasonerFactory;
import de.tudresden.inf.lat.uel.core.main.AlternativeUelStarter;
import de.tudresden.inf.lat.uel.core.main.UnifierIterator;
import de.tudresden.inf.lat.uel.core.processor.UelModel;
import de.tudresden.inf.lat.uel.core.processor.UnificationAlgorithmFactory;

/**
 * @author Stefan Borgwardt
 *
 */
public class SNOMEDEvaluation {

	// private static final String WORK_DIR = "C:\\Users\\Stefan\\Work\\";
	private static final String WORK_DIR = "/Users/stefborg/Documents/";
	private static final String SNOMED_PATH = WORK_DIR + "Ontologies/snomed-english-rdf.owl";
	private static final String SNOMED_RESTR_PATH = WORK_DIR + "Ontologies/snomed-restrictions.owl";
	private static final String POS_PATH = WORK_DIR + "Projects/uel-snomed/uel-snomed-pos.owl";
	private static final String NEG_PATH = WORK_DIR + "Projects/uel-snomed/uel-snomed-neg.owl";
	private static final String CONSTRAINTS_PATH = WORK_DIR + "Projects/uel-snomed/constraints_const.owl";

	private static OWLClass cls(String name) {
		return OWLManager.getOWLDataFactory().getOWLClass(IRI.create("http://www.ihtsdo.org/" + name));
	}

	private static OWLObjectProperty prp(String name) {
		return OWLManager.getOWLDataFactory().getOWLObjectProperty(IRI.create("http://www.ihtsdo.org/" + name));
	}

	/**
	 * Entry point for tests.
	 * 
	 * @param args
	 *            arguments (ignored)
	 */
	public static void main(String[] args) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology snomed = AlternativeUelStarter.loadOntology(SNOMED_PATH, manager);
		OWLOntology snomedRestrictions = AlternativeUelStarter.loadOntology(SNOMED_RESTR_PATH, manager);
		AlternativeUelStarter starter = new AlternativeUelStarter(
				new HashSet<OWLOntology>(Arrays.asList(snomed, snomedRestrictions)));
		starter.setVerbose(true);
		starter.markUndefAsVariables(false);
		// starter.markUndefAsAuxVariables(true);
		starter.setSnomedMode(true);

		OWLOntology pos = AlternativeUelStarter.loadOntology(POS_PATH, manager);
		OWLOntology neg = AlternativeUelStarter.loadOntology(NEG_PATH, manager);
		OWLOntology constraints = AlternativeUelStarter.loadOntology(CONSTRAINTS_PATH, manager);
		// OWLOntology neg = UelModel.EMPTY_ONTOLOGY;
		// String[] varNames = { "X" };
		String[] varNames = { "X" };
		UnifierIterator iterator = (UnifierIterator) starter.modifyOntologyAndSolve(pos, neg, null,
				Arrays.asList(varNames).stream().map(SNOMEDEvaluation::cls).collect(Collectors.toSet()),
				UnificationAlgorithmFactory.SAT_BASED_ALGORITHM_MINIMAL, false);

		Set<OWLAxiom> background = iterator.getUelModel().renderDefinitions();
		UelModel model = iterator.getUelModel();

		OWLDataFactory fac = manager.getOWLDataFactory();
		OWLAxiom goalAxiom = fac
				.getOWLEquivalentClassesAxiom(cls("X"),
						fac.getOWLObjectIntersectionOf(cls("SCT_106133000"), cls("SCT_365781004"),
								fac.getOWLObjectSomeValuesFrom(prp("RoleGroup"),
										fac.getOWLObjectIntersectionOf(fac.getOWLObjectSomeValuesFrom(
												prp("SCT_363713009"), cls("SCT_371157007")),
										fac.getOWLObjectSomeValuesFrom(prp("SCT_363714003"), cls("SCT_307124006"))))));

		System.out.println("Unifiers:");

		try {
			Scanner in = new Scanner(System.in);
			int i = 0;
			boolean skip = true;

			if (!skip) {
				System.out.println("Press RETURN to start computing the next unifier (input 'a' for all unifiers) ...");
			}
			while (skip || in.hasNextLine()) {
				if (!skip) {
					if (in.nextLine().equals("a")) {
						skip = true;
					}
				}
				// if (i == 0) {
				// skip = false;
				// }

				i++;
				System.out.println();
				System.out.println("--- " + i);

				if (iterator.hasNext()) {

					// TODO compute unifiers modulo equivalence?

					System.out.println(
							model.getStringRenderer(null).renderUnifier(model.getCurrentUnifier(), false, true, true));

					Set<OWLEquivalentClassesAxiom> unifier = iterator.next();
					OWLOntologyManager ontologyManager = OWLManager.createOWLOntologyManager();
					OWLOntology extendedOntology = ontologyManager.createOntology();
					ontologyManager.addAxioms(extendedOntology, background);
					ontologyManager.addAxioms(extendedOntology, unifier);

					// ontologyManager.saveOntology(extendedOntology, new
					// FunctionalSyntaxDocumentFormat(), System.out);

					OWLReasoner reasoner = new JcelReasonerFactory().createNonBufferingReasoner(extendedOntology);
					reasoner.precomputeInferences();

					for (OWLAxiom a : pos.getAxioms(AxiomType.SUBCLASS_OF)) {
						System.out.println(a + " (pos): " + reasoner.isEntailed(a));
						// Assert.assertTrue(reasoner.isEntailed(a));
					}
					for (OWLAxiom a : neg.getAxioms(AxiomType.SUBCLASS_OF)) {
						System.out.println(a + " (neg): " + reasoner.isEntailed(a));
						// Assert.assertTrue(!reasoner.isEntailed(a));
					}

					System.out.println(goalAxiom + " (goal): " + reasoner.isEntailed(goalAxiom));
					if (reasoner.isEntailed(goalAxiom)) {
						System.out.println("Success! " + i);
						// break;
					}

					reasoner.dispose();

					System.out.println();

					System.out.flush();
				} else {
					System.out.println("No more unifiers.");
					break;
				}
			}
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
