package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.WindowConstants;

import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;

import de.tudresden.inf.lat.uel.plugin.processor.UelProcessor;

/**
 * This class is a controller for UEL.
 * 
 * @author Julian Mendez
 */
public class UelController implements ActionListener, OWLOntologyChangeListener {

	private static final String actionAcceptVar = "accept var";
	private static final String actionGetVar = "get var candidate";
	private static final String actionNext = "next";
	private static final String actionPrevious = "previous";
	private static final String actionRejectVar = "reject var";
	private static final String actionSave = "save";

	private static final Logger logger = Logger.getLogger(UelController.class
			.getName());

	private List<OWLClass> classList = null;
	private boolean ontologyChanged = true;
	private int unifierIndex = -1;
	private VarSelectionView varFrame = null;
	private UelView view = null;

	public UelController(UelView panel) {
		if (panel == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		this.view = panel;
		init();
	}

	/**
	 * Action handler.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		String cmd = e.getActionCommand();
		if (cmd.equals(actionGetVar)) {
			executeActionGetVar();
		} else if (cmd.equals(actionAcceptVar)) {
			executeActionAcceptVar();
		} else if (cmd.equals(actionRejectVar)) {
			executeActionRejectVar();
		} else if (cmd.equals(actionPrevious)) {
			executeActionPrevious();
		} else if (cmd.equals(actionNext)) {
			executeActionNext();
		} else if (cmd.equals(actionSave)) {
			executeActionSave();
		} else {
			throw new IllegalStateException();
		}
	}

	private void executeActionAcceptVar() {
		getVarWindow().setVisible(false);
		getModel().clearCandidates();
		getModel().addAll(getVarWindow().getSelectedValues());
		getVarWindow().dispose();
		getView().setButtonPreviousEnabled(false);
		getView().setButtonNextEnabled(true);
		getView().setButtonSaveEnabled(false);
	}

	private void executeActionGetVar() {
		getView().setButtonPreviousEnabled(false);
		getView().setButtonNextEnabled(false);
		getView().setButtonSaveEnabled(false);
		keepOntologyUpdated();
		Set<OWLClass> classSet = new HashSet<OWLClass>();
		classSet.add(getSelectedClass00());
		classSet.add(getSelectedClass01());
		getModel().recalculateCandidates(classSet);
		this.varFrame = initVarFrame(getModel().getCandidates());
		getVarWindow().setVisible(true);
		this.unifierIndex = -1;
	}

	private void executeActionNext() {
		getVarWindow().setVisible(false);
		getView().setButtonPreviousEnabled(true);
		getView().setButtonNextEnabled(true);
		getView().setButtonSaveEnabled(true);

		if (getModel().getUnifierList().isEmpty()) {
			Set<OWLClass> classSet = new HashSet<OWLClass>();
			classSet.add(getSelectedClass00());
			classSet.add(getSelectedClass01());
			getModel().configure(classSet);
		}
		this.unifierIndex++;
		if (this.unifierIndex >= getModel().getUnifierList().size()) {
			boolean unifiable = getModel().computeNextUnifier();
			if (!unifiable && this.unifierIndex > 0) {
				this.unifierIndex--;
			}
		}
		updateUnifier();
	}

	private void executeActionPrevious() {
		getVarWindow().setVisible(false);
		getView().setButtonPreviousEnabled(true);
		getView().setButtonNextEnabled(true);
		getView().setButtonSaveEnabled(true);

		this.unifierIndex--;
		if (this.unifierIndex < 0) {
			this.unifierIndex = 0;
		}
		updateUnifier();
	}

	private void executeActionRejectVar() {
		getVarWindow().setVisible(false);
		getVarWindow().dispose();
		getView().setButtonPreviousEnabled(false);
		getView().setButtonNextEnabled(false);
		getView().setButtonSaveEnabled(false);
	}

	private void executeActionSave() {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showSaveDialog(getView());
		File file = null;
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fileChooser.getSelectedFile();
		}
		if (file != null) {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file));
				if (getModel().getUnifierList().size() > 0) {
					writer.write(getModel().getUnifierList().get(
							this.unifierIndex));
				}
				writer.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

		}
	}

	public DefaultListModel getListModel() {
		return getView().getListModel();
	}

	public UelProcessor getModel() {
		return getView().getModel();
	}

	public OWLWorkspace getOWLWorkspace() {
		return getModel().getOWLWorkspace();
	}

	public OWLClass getSelectedClass00() {
		return this.classList.get(getView().getSelectedIndex00());
	}

	public OWLClass getSelectedClass01() {
		return this.classList.get(getView().getSelectedIndex01());
	}

	public VarSelectionView getVarWindow() {
		return this.varFrame;
	}

	public UelView getView() {
		return this.view;
	}

	/**
	 * Initializes the data and GUI. This method is called when the view is
	 * initialized.
	 */
	public void init() {
		getOWLWorkspace().getOWLModelManager().addOntologyChangeListener(this);
		getView().setButtonPreviousEnabled(false);
		getView().setButtonNextEnabled(false);
		getView().setButtonSaveEnabled(false);
		getView().addButtonGetVarListener(this, actionGetVar);
		getView().addButtonPreviousListener(this, actionPrevious);
		getView().addButtonNextListener(this, actionNext);
		getView().addButtonSaveListener(this, actionSave);
		refresh();
		getView().setButtonGetVarEnabled(true);
	}

	private VarSelectionView initVarFrame(Set<String> set) {
		VarSelectionView ret = new VarSelectionView(set);
		ret.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		ret.addAcceptVarButtonListener(this, actionAcceptVar);
		ret.addRejectVarButtonListener(this, actionRejectVar);
		ret.setLocation(400, 400);
		ret.setSize(300, 200);
		ret.setVisible(true);
		return ret;
	}

	private void keepOntologyUpdated() {
		if (this.ontologyChanged) {
			this.ontologyChanged = false;
			getModel().reloadOntology(getOWLWorkspace().getOWLModelManager());
		}
	}

	public void ontologiesChanged(List<? extends OWLOntologyChange> change)
			throws OWLException {
		if (change == null) {
			throw new IllegalArgumentException("Null argument.");
		}

		logger.info("The ontology has changed.");
		this.ontologyChanged = true;
	}

	public void refresh() {
		OWLClass nothing = getModel().getOWLWorkspace().getOWLModelManager()
				.getOWLDataFactory().getOWLNothing();
		OWLClass thing = getModel().getOWLWorkspace().getOWLModelManager()
				.getOWLDataFactory().getOWLThing();
		Set<OWLClass> set = new TreeSet<OWLClass>();
		set.addAll(getModel().getOWLWorkspace().getOWLModelManager()
				.getActiveOntology().getClassesInSignature());
		set.remove(nothing);
		set.remove(thing);
		this.classList = new ArrayList<OWLClass>();
		this.classList.add(nothing);
		this.classList.add(thing);
		this.classList.addAll(set);
		List<String> classNameSet = new ArrayList<String>();
		for (OWLClass cls : this.classList) {
			classNameSet.add(cls.getIRI().toURI().getFragment());
		}
		getView().refresh(classNameSet);
	}

	public void removeListeners() {
		getOWLWorkspace().getOWLModelManager().removeOntologyChangeListener(
				this);
	}

	public void setSelectedClass(OWLClass selectedClass) {
	}

	private void updateUnifier() {
		if (getModel().getUnifierList().size() > 0) {
			getView().getUnifier().setText(
					getModel().getUnifierList().get(this.unifierIndex));
		} else {
			getView().getUnifier().setText("[not unifiable]");
		}
	}

}
