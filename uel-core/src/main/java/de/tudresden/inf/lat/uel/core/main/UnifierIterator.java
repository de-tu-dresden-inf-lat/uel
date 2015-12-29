package de.tudresden.inf.lat.uel.core.main;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import de.tudresden.inf.lat.uel.core.type.OWLUelClassDefinition;
import de.tudresden.inf.lat.uel.core.type.UnifierTranslator;
import de.tudresden.inf.lat.uel.type.api.AtomManager;
import de.tudresden.inf.lat.uel.type.api.UelProcessor;
import de.tudresden.inf.lat.uel.type.impl.Unifier;

public class UnifierIterator implements Iterator<Set<OWLUelClassDefinition>> {

	private boolean hasNext = false;
	private boolean isComputed = false;
	private UelProcessor processor;
	private UnifierTranslator translator;
	private Unifier unifier;

	public UnifierIterator(UelProcessor proc, UnifierTranslator translator) {
		this.processor = proc;
		this.translator = translator;
	}

	protected UelProcessor getProcessor() {
		return this.processor;
	}

	protected AtomManager getAtomManager() {
		return this.translator.getAtomManager();
	}

	public void cleanup() {
		if (processor != null) {
			processor.cleanup();
		}
	}

	private void compute() {
		if (!isComputed) {
			try {
				hasNext = processor.computeNextUnifier();
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				hasNext = false;
			}
			if (hasNext) {
				unifier = processor.getUnifier();
			}
			isComputed = true;
		}
	}

	@Override
	public boolean hasNext() {
		compute();
		return hasNext;
	}

	@Override
	public Set<OWLUelClassDefinition> next() {
		compute();
		if (!hasNext) {
			throw new NoSuchElementException();
		}

		isComputed = false;

		return translator.createOWLUelClassDefinitions(unifier.getDefinitions());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

}
