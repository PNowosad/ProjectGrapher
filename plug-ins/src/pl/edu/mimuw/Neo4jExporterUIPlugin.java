/**
 * 
 */
package pl.edu.mimuw;

import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.json.JSONObject;
import org.pf.plugin.IInitializablePlugin;
import org.pf.tools.cda.plugin.export.spi.AModelExporter;
import org.pf.tools.cda.ui.plugin.export.spi.AModelExporterUIPlugin;
import org.pf.tools.cda.xpi.IPluginInfo;
import org.pf.tools.cda.xpi.PluginConfiguration;
import org.pfsw.odem.IExplorationModelObject;

import pl.edu.mimuw.exporter.Neo4jExporter;

/**
 * @author Paweł Nowosad
 *
 */
public class Neo4jExporterUIPlugin extends AModelExporterUIPlugin implements IPluginInfo, IInitializablePlugin {

	final static String PLUGIN_CONFIGURATION_IMPORTED_JSONS = "imported_jsons";
	
	private String databaseUrl = "http://localhost:7474/";
	
	private java.util.List<JSONObject> importedJsons = new ArrayList<JSONObject>(); 
	
	/**
	 * 
	 */
	public Neo4jExporterUIPlugin() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.pf.tools.cda.ui.xpi.IPluginActionInfo#getActionText(java.util.Locale, org.pfsw.odem.IExplorationModelObject)
	 */
	@Override
	public String getActionText(Locale arg0, IExplorationModelObject arg1) {
		return "Neo4j";
	}

	/* (non-Javadoc)
	 * @see org.pf.tools.cda.xpi.IPluginInfo#getPluginProvider()
	 */
	@Override
	public String getPluginProvider() {
		return "Paweł Nowosad";
	}

	/* (non-Javadoc)
	 * @see org.pf.tools.cda.xpi.IPluginInfo#getPluginVersion()
	 */
	@Override
	public String getPluginVersion() {
		return "1.0";
	}
	
	private JPanel createImportPanel(final Dialog parent) {
		JPanel importPanel = new JPanel();
		importPanel.setLayout(new BoxLayout(importPanel, BoxLayout.Y_AXIS));
		
		JLabel infoLabel = new JLabel("List of json files to import:");
		infoLabel.setSize(100, 10);
		importPanel.add(infoLabel);
		
		final List importList = new List();
		importList.setSize(1200, 200);
		importPanel.add(importList);
		
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonsPanel.setSize(1200, 10);
		
		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser openDialog = new JFileChooser();
				openDialog.setDialogTitle("Select JSON file to import");
				openDialog.setMultiSelectionEnabled(true);
				openDialog.setFileFilter(new FileNameExtensionFilter("JSON file", "json"));
				int returnVal = openDialog.showOpenDialog(parent);
				if (returnVal == JFileChooser.APPROVE_OPTION) {					
					for (File selectedFile : openDialog.getSelectedFiles()) {
						importList.add(selectedFile.getAbsolutePath());
						
						try(BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
					        StringBuilder sb = new StringBuilder();
					        String line = br.readLine();

					        while (line != null) {
					            sb.append(line);
					            sb.append(System.lineSeparator());
					            line = br.readLine();
					        }
					        
					        JSONObject json = new JSONObject(sb.toString());
					        importedJsons.add(json);
					    } catch (FileNotFoundException e1) {
					    	JOptionPane.showMessageDialog(parent, "File doesn't exist at path: " + selectedFile.getAbsolutePath(), "Error opening file", JOptionPane.ERROR_MESSAGE);
					    	
							e1.printStackTrace();
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(parent, "Error occurred reading file at path: " + selectedFile.getAbsolutePath(), "Error reading file", JOptionPane.ERROR_MESSAGE);
							
							e1.printStackTrace();
						}
					}
				}
			}
		});
		buttonsPanel.add(addButton);
		
		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedItemIndex = importList.getSelectedIndex();
				if (selectedItemIndex != -1) {
					importList.remove(selectedItemIndex);
					
					importedJsons.remove(selectedItemIndex);
				}
			}
		});
		buttonsPanel.add(removeButton);
		
		importPanel.add(buttonsPanel);
		
		return importPanel;
	}
	
	@Override
	public PluginConfiguration getConfiguration(Frame parent) {
		final PluginConfiguration pluginConfiguration = super.getConfiguration(parent);
		
		final Dialog importDialog = new Dialog(parent, "Import additional data", true);
		importDialog.setSize(1200, 600);
		importDialog.setLayout(new BoxLayout(importDialog, BoxLayout.Y_AXIS));
		importDialog.add(this.createImportPanel(importDialog));
		importDialog.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {	
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				importDialog.setVisible(false);
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
		
		JButton okButton = new JButton("Continue");
		okButton.setSize(200, 50);
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				pluginConfiguration.set(PLUGIN_CONFIGURATION_IMPORTED_JSONS, importedJsons);
				
				importDialog.setVisible(false);
			}
		});
		importDialog.add(okButton);
		
		// Show dialog
		importDialog.pack();
		importDialog.setVisible(true);
		
		return pluginConfiguration;
	}

	/* (non-Javadoc)
	 * @see org.pf.tools.cda.ui.plugin.export.spi.AModelExporterUIPlugin#createExporter(org.pf.tools.cda.xpi.PluginConfiguration)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public AModelExporter createExporter(PluginConfiguration arg0) {
		java.util.List<JSONObject> importedJsons = null;
		if (arg0.get(PLUGIN_CONFIGURATION_IMPORTED_JSONS) instanceof java.util.List<?>) {
			 importedJsons = (java.util.List<JSONObject>) arg0.get(PLUGIN_CONFIGURATION_IMPORTED_JSONS);
		}
		return new Neo4jExporter(this.databaseUrl, importedJsons);
	}

	@Override
	public void initPlugin(String arg0, Properties arg1) {
		String newDatabaseUrl = arg1.getProperty("databaseUrl");
		if (newDatabaseUrl != null) {
			this.databaseUrl = newDatabaseUrl;
		}
	}

}
