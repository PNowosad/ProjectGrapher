/**
 * 
 */
package pl.edu.mimuw;

import java.util.Locale;

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
public class Neo4jExporterUIPlugin extends AModelExporterUIPlugin implements IPluginInfo {

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
		return "0.1";
	}

	/* (non-Javadoc)
	 * @see org.pf.tools.cda.ui.plugin.export.spi.AModelExporterUIPlugin#createExporter(org.pf.tools.cda.xpi.PluginConfiguration)
	 */
	@Override
	public AModelExporter createExporter(PluginConfiguration arg0) {
		return new Neo4jExporter();
	}

}
