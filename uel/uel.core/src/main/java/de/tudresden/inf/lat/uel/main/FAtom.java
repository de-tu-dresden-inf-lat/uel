package de.tudresden.inf.lat.uel.main;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 * This class extends Atom. It implements a flat atom, hence an atom which is
 * either a concept name or an existential restriction with a concept name as an
 * argument.
 * 
 * Concept name can be a concept constant or a variable. While constructing flat
 * atoms from atoms, we introduce new variables and add new equations to the
 * goal.
 * 
 * @author Barbara Morawska
 * 
 */

public class FAtom extends Atom {

	private FAtom child = null;

	private Collection<FAtom> S = new ArrayList<FAtom>();

	private boolean sys = false;
	private boolean var = false;

	/**
	 * Constructor of flat atom which takes a non-flat atom, flattens it:
	 * introduces new variable, adds an equation to the goal, checks for
	 * additional definitions in the ontology, if ontology is loaded, and
	 * flattens them and adds to the goal.
	 * 
	 * Flattening is recursive.
	 * 
	 * @param atom
	 */
	public FAtom(Atom atom, Goal goal) {
		super(atom.getName(), atom.isRoot());

		if (!atom.isRoot()) {
			child = null;

			goal.importDefinition(atom);

		} else if (!atom.isFlat()) {

			FAtom b = null;
			String newvar = "VAR" + goal.getNbrVar();

			Equation eq = new Equation();

			b = new FAtom(newvar, false, true, null);

			goal.setNbrVar(goal.getNbrVar() + 1);

			child = b;

			eq.getLeft().put(b.toString(), b);
			eq.setRight(atom.getChildren());

			goal.addFlatten(eq);

		} else {

			for (String key : atom.getChildren().keySet()) {

				if (goal.getAllAtoms().containsKey(key)) {

					child = goal.getAllAtoms().get(key);

				} else {

					child = new FAtom(atom.getChildren().get(key), goal);

				}
			}
		}

		if (!goal.getAllAtoms().containsKey(this.toString())) {

			goal.getAllAtoms().put(this.toString(), this);

		}

	}

	/**
	 * Constructor of flat atom (used in flattening Goal.addAndFlatten)
	 * 
	 * atom is possible non-flat atom c is a flat atom, which is an argument for
	 * a flat atom to be constructed.
	 * 
	 * 
	 * @param atom
	 * @param c
	 */
	public FAtom(FAtom c, Atom atom) {
		super(atom.getName(), atom.isRoot());
		child = c;
	}

	/**
	 * Constructor of flat atom (used in ReaderAndParser to create a flat system
	 * variable).
	 * 
	 * name is <code>name</code> of an atom r is true if atom is an existential
	 * restriction v is true if atom is a variable arg is a flat atom, which is
	 * an argument for a role name in an existential restriction
	 * 
	 * @param name
	 * @param r
	 * @param v
	 * @param arg
	 */
	public FAtom(String name, boolean r, boolean v, FAtom arg) {
		super(name, r);
		var = v;
		child = arg;
	}

	/**
	 * Adds a flat atom to a substitution set Used in Translator, to define
	 * substitution for variables.
	 * 
	 * @param atom
	 */
	public void addToS(FAtom atom) {

		S.add((FAtom) atom);

	}

	/**
	 * Returns an argument in the flat atom, which is an existential
	 * restriction. Otherwise it returns null.
	 * 
	 * Used in defining clauses in Translator
	 * 
	 * @return an argument in the flat atom, which is an existential
	 *         restriction; otherwise it returns null
	 */
	public FAtom getChild() {

		return child;
	}

	public Collection<FAtom> getS() {
		return this.S;
	}

	/**
	 * Not used in UEL. Checks if this atom is a constant.
	 * 
	 * @return <code>true</code> if and only if this atoms is a constant
	 */
	public boolean isCons() {

		return !(var || this.isRoot());
	}

	/**
	 * Checks if this flat atom is a system variable.
	 * 
	 * @return <code>true</code> if and only if this flat atom is a system
	 *         variable
	 */
	public boolean isSys() {

		return sys;

	}

	/**
	 * Checks if a flat atom is a variable.
	 * 
	 * @return <code>true</code> if and only if a flat atom is a variable
	 */
	public boolean isVar() {

		return var;

	}

	/**
	 * Prints to a Print Writer out a substitution set (i.e. a set of atoms) as
	 * a conjunction of atoms in the krss format. Used in Translator.
	 * 
	 * @param out
	 */
	public void printS(PrintWriter out) {

		if (S.isEmpty()) {

			out.print("TOP ");

		} else if (S.size() == 1) {

			out.print(S.iterator().next().toString());

		} else {

			out.print("(AND ");

			for (FAtom atom : S) {
				out.print(" ");
				out.print(atom.toString());
				// out.print(atom.getName());
				out.print(" ");
			}

			out.print(" )");
		}

	}

	/**
	 * Resets substitution set of a variable. This is necessary to be able to
	 * compute new substitution
	 * 
	 * Used in Translator
	 * 
	 */
	public void resetS() {

		S = new ArrayList<FAtom>();

	}

	/**
	 * If v is true, it defines this atom as a variable
	 * 
	 * @param v
	 */
	public void setVar(boolean v) {

		var = v;

	}

	/**
	 * Sets a flat atom to be a system variable. Used at Goal initialization.
	 * 
	 */
	public void sysVar() {

		if (!isRoot()) {
			sys = true;
		} else {
			throw new IllegalStateException(
					"WARNING: cannot change existential atom  into a system variable");
		}

	}

	@Override
	public String toString() {

		StringBuilder str = new StringBuilder(this.getName());

		if (child != null) {

			str = str.insert(0, "(SOME ");

			str.append(" ");
			str.append(child.toString());
			str.append(")");
		}
		return str.toString();
	}

}
