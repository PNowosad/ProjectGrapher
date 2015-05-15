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
import org.pfsw.odem.INamespace;
import org.pfsw.odem.IType;

import pl.edu.mimuw.models.ProjectClass;
import pl.edu.mimuw.models.ProjectNamespace;

/**
 * @author Paweł Nowosad
 *
 */
public class Neo4jExporter extends AModelExporter {

	static final String SERVER_ROOT_URI = "http://localhost:7474/db/data/";

	private Client webClient;
	private WebTarget rootTarget;
	
	private String commitTransactionUrl;
	
	private Map<String, ProjectClass> classMap;
	private Map<String, ProjectNamespace> namespaceMap;
	
	/**
	 * 
	 */
	public Neo4jExporter() {
		System.out.println("Neo4jExporter created!");
		
		webClient = ClientBuilder.newClient();
		rootTarget = webClient.target(SERVER_ROOT_URI);
		
		classMap = new HashMap<String, ProjectClass>();
		namespaceMap = new HashMap<String, ProjectNamespace>();
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
		
		return super.startContext(context);
	}
	
	@Override
	public boolean finishContext(IExplorationContext context) {
		for (String namespaceString : namespaceMap.keySet()) {
			ProjectNamespace namespace = namespaceMap.get(namespaceString);
			
			String nsString = new String(namespaceString);
			while (nsString.length() > 0) {
				int dotIndex = nsString.lastIndexOf(".");
				if (dotIndex != -1)
					nsString = nsString.substring(0, dotIndex);
				else
					nsString = "";
				
				ProjectNamespace superNamespace = namespaceMap.get(nsString);
				if (superNamespace != null) {
					namespace.createRelationship(superNamespace, "subnamespace", null);
					
					break;
				}
			}
		}
		
		return super.finishContext(context);
	}

	@Override
	public boolean startNamespace(INamespace namespace) {
		ProjectNamespace newNamespace = new ProjectNamespace(rootTarget, namespace);
		String namespaceString = namespace != null ? namespace.getName() : "";
		namespaceMap.put(namespaceString != null ? namespaceString : "", newNamespace);
		
		return super.startNamespace(namespace);
	}
	
	@Override
	public boolean finishNamespace(INamespace namespace) {
		
		return super.finishNamespace(namespace);
	}
	
	@Override
	public boolean startType(IType type) {
		ProjectClass newClass = new ProjectClass(rootTarget, type);
		classMap.put(type.getName(), newClass);
		
		String classNamespaceString = type.getNamespace().getName();
		ProjectNamespace classNamespace = namespaceMap.get(classNamespaceString != null ? classNamespaceString : "");
		newClass.createRelationship(classNamespace, "belongs to", null);
		
		return super.startType(type);
	}
	
	@Override
	public boolean finishType(IType type) {
		
		return super.finishType(type);
	}
	
}
