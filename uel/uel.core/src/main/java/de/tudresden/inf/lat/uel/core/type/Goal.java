package de.tudresden.inf.lat.uel.core.type;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class implements a goal of unification, i.e., a set of equations between
 * concept terms with variables.
 * 
 * The goal is unique for the procedure, and should be accessible for most other
 * objects.
 * 
 * @author Barbara Morawska
 */
public class Goal {

	private static final Logger logger = Logger.getLogger(Goal.class.getName());
	public static final String UNDEF_SUFFIX = "_UNDEF";
	public static final String VAR_PREFIX = "VAR";

	/**
	 * allAtoms is a hash map implementing all flat atoms in the goal keys are
	 * names and values are flat atoms
	 */
	private Map<String, Atom> allAtoms = new HashMap<String, Atom>();

	private IndexedSet<Atom> atomManager = null;

	/**
	 * constants is a hash map implementing all constant concept names in the
	 * goal. keys are names and values are flat atoms
	 */
	private Set<Integer> constants = new HashSet<Integer>();

	/**
	 * eatoms is a hash map implementing all flat existential restrictions keys
	 * are names and values are flat atoms
	 */
	private Set<Integer> eatoms = new HashSet<Integer>();

	/**
	 * equations is a list containing all goal equations
	 * 
	 */
	private List<Equation> equations = new ArrayList<Equation>();

	private Equation mainEquation = null;

	private int nbrVar = 0;

	private Ontology ontology = null;

	/**
	 * variables is a hash map implementing all concept names which are treated
	 * as variables keys are names and values are flat atoms
	 */
	private Set<Integer> variables = new HashSet<Integer>();

	/**
	 * Constructs a new goal based on a specified ontology.
	 * 
	 * @param ont
	 *            an ontology
	 */
	public Goal(Ontology ont, IndexedSet<Atom> manager) {
		ontology = ont;
		this.atomManager = manager;
	}

	/**
	 * Method to add an equation e to the list of goal equations
	 * 
	 * @param e
	 *            equation
	 */
	public void addEquation(Equation e) {
		equations.add(e);
	}

	/**
	 * This method is to flatten an equation and to add it to the list of goal
	 * equations
	 * 
	 * @param e
	 *            equation that needs to be flattened
	 */
	public void addFlatten(Equation e) {

		Atom a;
		Atom b = getAtomManager().get(e.getLeft());

		if (b.isRoot()) {

			throw new RuntimeException(
					" Definition should not have an existential restriction on its left side ");

		}

		if (variables.contains(b.getName())) {

			logger.warning("Warning: This definition was already added to the goal "
					+ b.getName());

		} else {

			Set<Atom> rightPart = new HashSet<Atom>();

			a = new Atom(null, b);

			a.setVariable(true);

			variables.add(getAtomManager().addAndGetIndex(a));
			allAtoms.put(a.getId(), a);

			Atom leftPart = a;

			/*
			 * FLATTENING
			 */

			for (Integer atomId : e.getRight()) {

				a = new Atom(getAtomManager().get(atomId), this);

				if (allAtoms.containsKey(a.getId())) {

					rightPart.add(allAtoms.get(a.getId()));

				} else {

					rightPart.add(a);

				}

			}

			if (e.isPrimitive()) {
				/*
				 * Adding new variable to the right side
				 */

				Atom var = new Atom(b.getId() + UNDEF_SUFFIX, false, false,
						null);
				var.setUserVariable(false);
				this.allAtoms.put(var.getId(), var);
				this.constants.add(getAtomManager().addAndGetIndex(var));
				rightPart.add(var);
			}

			Integer leftPartId = getAtomManager().addAndGetIndex(leftPart);
			Set<Integer> rightPartIds = new HashSet<Integer>();
			for (Atom elem : rightPart) {
				rightPartIds.add(getAtomManager().addAndGetIndex(elem));
			}

			addEquation(new Equation(leftPartId, rightPartIds, false));

		}
	}

	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof Goal) {
			Goal other = (Goal) o;
			ret = this.allAtoms.equals(other.allAtoms)
					&& this.constants.equals(other.constants)
					&& this.eatoms.equals(other.eatoms)
					&& this.equations.equals(other.equations)
					&& this.mainEquation.equals(other.mainEquation)
					&& this.variables.equals(other.variables)
					&& this.ontology.equals(other.ontology)
					&& this.nbrVar == other.nbrVar;
		}
		return ret;
	}

	public Map<String, Atom> getAllAtoms() {
		return allAtoms;
	}

	public IndexedSet<Atom> getAtomManager() {
		return this.atomManager;
	}

	public Set<String> getConstants() {
		return Collections.unmodifiableSet(toAtomNames(constants));
	}

	public Set<String> getEAtoms() {
		return Collections.unmodifiableSet(toAtomNames(eatoms));
	}

	/**
	 * Method to get the list of goal equations
	 * 
	 * @return the list of goal equations
	 */
	public List<Equation> getEquations() {
		return equations;
	}

	/**
	 * Returns a string representation of the equations, excluding the main
	 * equation.
	 * 
	 * @return a string representation of the equations, excluding the main
	 *         equation
	 */
	public String getGoalEquations() {
		StringBuffer sbuf = new StringBuffer();
		for (Equation eq : getEquations()) {

			sbuf.append(toString(eq));
		}
		return sbuf.toString();
	}

	public Equation getMainEquation() {
		return mainEquation;
	}

	/**
	 * This method is used by a constructor of a flat atom
	 * <code>FAtom(Atom)</code> from atom. This requires to introduce a new
	 * variable. New variables are identified by unique numbers. The next unique
	 * number is stored in <code>nbrVar</code>.
	 * 
	 * @return nbrVar
	 */
	public int getNbrVar() {
		return nbrVar;
	}

	public Set<String> getVariables() {
		return Collections.unmodifiableSet(toAtomNames(variables));
	}

	@Override
	public int hashCode() {
		return this.ontology.hashCode() + 31 * this.mainEquation.hashCode();
	}

	/**
	 * The method used in the flattening method <code>addFlatten</code> to add a
	 * relevant definition from the ontology to the goal.
	 * 
	 * <code>concept</code> is a concept name that may be defined in the
	 * ontology.
	 * 
	 * @param concept
	 *            concept
	 */
	public void importAnyDefinition(Atom concept) {
		if (!variables.contains(concept.getId())) {

			if (ontology.containsDefinition(concept.getId())) {
				addFlatten(ontology.getDefinition(concept.getId()));

			} else if (ontology.containsPrimitiveDefinition(concept.getId())) {
				addFlatten(ontology.getPrimitiveDefinition(concept.getId()));

			}
		}
	}

	public void initialize(List<Equation> equationList, Atom left, Atom right)
			throws IOException {
		initialize(equationList, left, right, null);
	}

	private void initialize(List<Equation> list, Atom left, Atom right,
			Writer output) throws IOException {

		setMainEquation(new Equation(getAtomManager().addAndGetIndex(left),
				getAtomManager().addAndGetIndex(right), false));
		for (Equation eq : list) {
			addFlatten(eq);
		}

		for (Integer atomId : variables) {
			String key = getAtomManager().get(atomId).getId();
			allAtoms.get(key).setVariable(true);
		}

		for (String key : allAtoms.keySet()) {
			Atom a = allAtoms.get(key);
			Integer id = getAtomManager().addAndGetIndex(a);

			if (!variables.contains(id) && !a.isRoot()) {
				constants.add(getAtomManager().addAndGetIndex(a));

			} else if (a.isRoot()) {
				eatoms.add(getAtomManager().addAndGetIndex(a));
			}
		}
	}

	public void makeConstant(String name) {
		Atom atom = this.allAtoms.get(name);
		Integer id = getAtomManager().addAndGetIndex(atom);
		if (this.variables.contains(id)) {
			this.variables.remove(id);
			this.constants.add(id);
			atom.setUserVariable(false);
			atom.setVariable(false);
		}
	}

	public void makeVariable(String name) {
		Atom atom = this.allAtoms.get(name);
		Integer id = getAtomManager().addAndGetIndex(atom);
		if (this.constants.contains(id)) {
			this.constants.remove(id);
			this.variables.add(id);
			atom.setUserVariable(true);
			atom.setVariable(true);
		}
	}

	public void setMainEquation(Equation equation) {
		this.mainEquation = equation;
	}

	/**
	 * This method is used by a constructor of a flat atom
	 * <code>FAtom(Atom)</code> from atom. This requires to introduce a new
	 * variable. New variables are identified by unique numbers. The next unique
	 * number is stored in <code>nbrVar</code>.
	 * 
	 * @param nbrV
	 */
	public void setNbrVar(int nbrV) {
		nbrVar = nbrV;
	}

	private Set<String> toAtomNames(Set<Integer> set) {
		Set<String> ret = new HashSet<String>();
		for (Integer elem : set) {
			ret.add(getAtomManager().get(elem).getId());
		}
		return ret;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(toString(this.mainEquation));
		sbuf.append(getGoalEquations());
		return sbuf.toString();
	}

	private String toString(Equation eq) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(KRSSKeyword.open);
		if (eq.isPrimitive()) {
			sbuf.append(KRSSKeyword.define_primitive_concept);
		} else {
			sbuf.append(KRSSKeyword.define_concept);
		}
		sbuf.append(KRSSKeyword.space);
		sbuf.append(getAtomManager().get(eq.getLeft()));
		if (eq.getRight().size() > 1) {
			sbuf.append(KRSSKeyword.space);
			sbuf.append(KRSSKeyword.open);
			sbuf.append(KRSSKeyword.and);
			for (Integer conceptId : eq.getRight()) {
				sbuf.append(KRSSKeyword.space);
				sbuf.append(getAtomManager().get(conceptId).getId());
			}
			sbuf.append(KRSSKeyword.close);
		} else if (eq.getRight().size() == 1) {
			sbuf.append(KRSSKeyword.space);
			sbuf.append(getAtomManager().get(eq.getRight().iterator().next())
					.getId());
		}
		sbuf.append(KRSSKeyword.close);
		sbuf.append("\n");
		return sbuf.toString();
	}

}
