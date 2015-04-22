/**
 * 
 */
package pl.edu.mimuw.exporter;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientResponse;
import org.json.JSONObject;
import org.pf.tools.cda.plugin.export.spi.AModelExporter;
import org.pf.tools.cda.xpi.PluginConfiguration;
import org.pfsw.odem.IExplorationContext;
import org.pfsw.odem.IType;

import pl.edu.mimuw.models.ProjectClass;

/**
 * @author Paweł Nowosad
 *
 */
public class Neo4jExporter extends AModelExporter {

	static final String SERVER_ROOT_URI = "http://localhost:7474/db/data/";

	private Client webClient;
	private WebTarget rootTarget;
	
	private String commitTransactionUrl;
	
	private String currentContextName = "";
	private Map<String, ProjectClass> classMap;
	
	/**
	 * 
	 */
	public Neo4jExporter() {
		// TODO Auto-generated constructor stub
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
	 * @see org.pf.tools.cda.plugin.export.spi.AModelExporter#initialize(org.pf.tools.cda.xpi.PluginConfiguration)
	 */
	@Override
	public boolean initialize(PluginConfiguration arg0) {
		webClient = ClientBuilder.newClient();
		rootTarget = webClient.target(SERVER_ROOT_URI);
		
		classMap = new HashMap<String, ProjectClass>();
		
		return true;
	}
	
	public void beginNewTransaction() {
		WebTarget newTransactionTarget = rootTarget.path("transaction");
		
		Invocation.Builder newTransactionInvBuilder = newTransactionTarget.request();
		ClientResponse response = newTransactionInvBuilder.post(Entity.entity("{'statements':[]}", MediaType.APPLICATION_JSON_TYPE), ClientResponse.class);
		JSONObject responseJSON = new JSONObject(response.readEntity(String.class));
		commitTransactionUrl = responseJSON.getString("commit");
	}
	
	public void commitTransaction() {
		WebTarget commitTransactionTarget = webClient.target(commitTransactionUrl);
		commitTransactionTarget.request().post(Entity.entity("{'statements':[]}", MediaType.APPLICATION_JSON_TYPE));
		
		commitTransactionUrl = "";
	}
	
	@Override
	public boolean startContext(IExplorationContext context) {
		currentContextName = context.getName();
		
		return super.startContext(context);
	}
	
	@Override
	public boolean finishContext(IExplorationContext context) {
		currentContextName = "";
		
		return super.finishContext(context);
	}

	@Override
	public boolean startType(IType type) {
		ProjectClass newClass = new ProjectClass(rootTarget, type, currentContextName);
		classMap.put(type.getName(), newClass);
		
		return super.startType(type);
	}
	
	@Override
	public boolean finishType(IType type) {
		
		
		return super.finishType(type);
	}
	
}
