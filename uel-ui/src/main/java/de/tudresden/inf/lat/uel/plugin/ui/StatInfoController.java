package de.tudresden.inf.lat.uel.plugin.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import de.tudresden.inf.lat.uel.core.processor.UelModel;

/**
 * This is the controller of the panel that shows statistical information.
 * 
 * @author Julian Mendez
 */
class StatInfoController {

	private static final String colon = ": ";
	private static final String newLine = "\n";

	private final UelModel model;
	private final StatInfoView view;

	public StatInfoController(UelModel model) {
		this.view = new StatInfoView();
		this.model = model;
		init();
	}

	private void executeSaveGoal() {
		File file = UelUI.showSaveFileDialog(view);
		if (file == null) {
			return;
		}

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(model.printPluginGoal(false));
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void init() {
		view.addSaveListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeSaveGoal();
			}
		});
	}

	public void open() {
		updateView();
		view.setVisible(true);
	}

	public void updateView() {
		view.setGoalText(model.printPluginGoal(true));
		StringBuffer info = new StringBuffer();
		for (Map.Entry<String, String> pair : model.getUelProcessor().getInfo()) {
			info.append(pair.getKey());
			info.append(colon);
			info.append(pair.getValue());
			info.append(newLine);
		}
		view.setInfoText(info.toString());
	}

}
