package de.tudresden.inf.lat.uel.ontmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.HashMap;

import de.tudresden.inf.lat.uel.main.Atom;
import de.tudresden.inf.lat.uel.main.Equation;

/**
 * 
 * This class is used to load a part of ontology to the goal. Ontology is a set
 * of definitions written in a file in the krss format
 * 
 * @author Barbara Morawska
 * 
 */

public class Ontology {

	private HashMap<String, Atom> allatoms = new HashMap<String, Atom>();

	/**
	 * 
	 * The flattened definitions stored by the defined concepts.
	 * 
	 */

	private HashMap<String, Equation> definitions = new HashMap<String, Equation>();

	private StreamTokenizer str;

	public Ontology() {
	}

	/**
	 * Method to get a definition from the ontology by the defined concept (Used
	 * by Goal initialization.)
	 */
	public Equation getDefinition(String name) {

		return definitions.get(name);

	}

	public HashMap<String, Equation> getDefinitions() {
		return definitions;
	}

	/**
	 * Method to load ontology from a file containing definitions in the krss
	 * format.
	 * 
	 * @throws IOException
	 * 
	 */

	public void loadOntology(Reader input) throws IOException {

		/*
		 * open file
		 */

		Reader reader = new BufferedReader(input);

		str = new StreamTokenizer(reader);
		str.ordinaryChar('(');
		str.ordinaryChar(')');
		str.wordChars('_', '_');
		str.wordChars('-', '-');

		str.wordChars(':', ':');
		str.wordChars('/', '/');
		str.wordChars('.', '.');
		str.wordChars('#', '#');

		str.nextToken();
		String definition = str.sval;

		int token = str.nextToken();
		String concept = "";

		/*
		 * search for "define-concept"
		 */

		while (token != StreamTokenizer.TT_EOF) {

			if (token == StreamTokenizer.TT_NUMBER) {

				concept = new Integer((int) str.nval).toString();

			} else if (token == StreamTokenizer.TT_WORD) {

				concept = str.sval;

			} else if (token != '(' && token != ')') {

				throw new RuntimeException("Something wrong in knowledge base "
						+ str);

			}

			/*
			 * concept definition found -- need to parse it and add to equations
			 */

			if (definition != null
					&& definition.equalsIgnoreCase("define-concept")) {

				int tokenTOP = str.nextToken();

				if (tokenTOP == '('
						|| (str.sval != null && !str.sval
								.equalsIgnoreCase("top"))) {

					str.pushBack();

					parse(concept);

				} else {

					System.out.println("Something wrong with database");

				}
			}

			definition = concept;
			token = str.nextToken();

		}

	}

	/*
	 * parsing the right side of the equation:
	 */

	private HashMap<String, Atom> parse() throws IOException {

		HashMap<String, Atom> result = new HashMap<String, Atom>();
		Atom a;
		String s = "";
		int token = str.nextToken();

		if (token == '(') {

			token = str.nextToken();
			if (token != StreamTokenizer.TT_WORD)
				throw new RuntimeException("Expecting constructor but found "
						+ str);

			s = str.sval;

			if (s.equalsIgnoreCase("AND")) {
				token = str.nextToken();

				/*
				 * parsing inside (AND
				 */

				while (token != StreamTokenizer.TT_EOF) {

					/*
					 * after ( expected SOME
					 */

					if (token == '(') {
						token = str.nextToken();

						if (token != StreamTokenizer.TT_WORD
								&& token != StreamTokenizer.TT_NUMBER)
							throw new RuntimeException(
									"Expecting constant or constructor but found "
											+ str);

						String ss = "";
						if (token == StreamTokenizer.TT_WORD) {

							ss = str.sval;
						} else if (token == StreamTokenizer.TT_NUMBER) {

							ss = new Integer((int) str.nval).toString();
						}

						s = "(".concat(ss);

						/*
						 * And cannot appear in AND
						 */
						if (s.equalsIgnoreCase("(AND"))
							throw new RuntimeException(
									"AND cannot occur inside (AND ...) " + str);

						/*
						 * SOME was found inside AND
						 */
						if (s.equalsIgnoreCase("(SOME")) {

							token = str.nextToken();
							if (token != StreamTokenizer.TT_WORD
									&& token != StreamTokenizer.TT_NUMBER)
								throw new RuntimeException(
										"Expecting role name but found " + str);

							String rolename = "";

							if (token == StreamTokenizer.TT_WORD) {

								rolename = str.sval;

							} else if (token == StreamTokenizer.TT_NUMBER) {

								rolename = new Integer((int) str.nval)
										.toString();

							}

							HashMap<String, Atom> arglist = new HashMap<String, Atom>(
									parse());

							/*
							 * WARNING ONTOLOGY IS NOT FLATTENED
							 */

							a = new Atom(rolename, true, arglist);
							if (!allatoms.containsKey(a.toString())) {
								result.put(a.toString(), a);
								allatoms.put(a.toString(), a);
							} else {
								result.put(a.toString(),
										allatoms.get(a.toString()));

							}

						}

						/*
						 * constant or variable inside AND
						 */

					} else if (token == StreamTokenizer.TT_WORD
							|| token == StreamTokenizer.TT_NUMBER) {

						String newname = "";

						if (token == StreamTokenizer.TT_WORD) {
							newname = str.sval;
						} else if (token == StreamTokenizer.TT_NUMBER) {

							newname = new Integer((int) str.nval).toString();

						}

						if (!allatoms.containsKey(newname)) {

							Atom c = new Atom(newname, false, null);
							result.put(newname, c);
							allatoms.put(newname, c);
						} else {
							result.put(newname, allatoms.get(newname));
						}

					} else if (token == ')') {

						return result;

					}

					token = str.nextToken();

				}

				/*
				 * Parsing SOME at top level
				 */

			} else if (s.equalsIgnoreCase("SOME")) {

				token = str.nextToken();

				if (token != StreamTokenizer.TT_WORD
						&& token != StreamTokenizer.TT_NUMBER)
					throw new RuntimeException("Expecting role name but found "
							+ str);

				String rolename = "";

				if (token == StreamTokenizer.TT_WORD) {

					rolename = str.sval;

				} else if (token == StreamTokenizer.TT_NUMBER) {

					rolename = new Integer((int) str.nval).toString();

				}

				HashMap<String, Atom> arglist = new HashMap<String, Atom>(
						parse());

				/*
				 * WARNING ONTOLOGY IS NOT FLATTENED
				 */

				a = new Atom(rolename, true, arglist);

				if (!allatoms.containsKey(a.toString())) {
					result.put(a.toString(), a);
					allatoms.put(a.toString(), a);
				} else {
					result.put(a.toString(), allatoms.get(a.toString()));
				}

				/*
				 * close bracket after (SOME...
				 */

				token = str.nextToken();
				if (token != ')')
					throw new RuntimeException("Expected ) found" + str);

				return result;

			}
			/*
			 * PARSING CONSTANT AT TOP LEVEL
			 */

		} else if (token == StreamTokenizer.TT_WORD
				|| token == StreamTokenizer.TT_NUMBER) {
			// Found a constant or variable at the top level

			if (token == StreamTokenizer.TT_WORD) {

				s = str.sval;
			} else if (token == StreamTokenizer.TT_NUMBER) {

				s = new Integer((int) str.nval).toString();
			}

			if (!s.equalsIgnoreCase("TOP") && !allatoms.containsKey(s)) {

				a = new Atom(s, false, null);
				result.put(a.toString(), a);

			} else if (!s.equalsIgnoreCase("TOP") && allatoms.containsKey(s)) {

				a = allatoms.get(s);
				result.put(a.toString(), a);

				token = str.nextToken();

			} else {

				throw new RuntimeException(
						"Something wrong: Top should not be here  " + str);
			}

			return result;

		}

		return result;

	}

	private void parse(String concept) throws IOException {
		/*
		 * create atom concept (constant)
		 */
		HashMap<String, Atom> leftside = new HashMap<String, Atom>();
		HashMap<String, Atom> rightside = new HashMap<String, Atom>(parse());

		Atom a;

		if (!allatoms.containsKey(concept)) {
			a = new Atom(concept, false, null);
			allatoms.put(a.toString(), a);

		} else {
			a = allatoms.get(concept);
		}

		leftside.put(a.toString(), a);

		Equation equation = new Equation(leftside, rightside);

		definitions.put(concept, equation);

	}

}
