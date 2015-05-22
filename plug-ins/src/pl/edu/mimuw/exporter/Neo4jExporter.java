/**
 * 
 */
package pl.edu.mimuw.exporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.pfsw.odem.DependencyClassification;
import org.pfsw.odem.DependencySet;
import org.pfsw.odem.IContainer;
import org.pfsw.odem.IDependency;
import org.pfsw.odem.IDependencyFilter;
import org.pfsw.odem.IExplorationContext;
import org.pfsw.odem.IExplorationModelObject;
import org.pfsw.odem.INamespace;
import org.pfsw.odem.IType;

import pl.edu.mimuw.models.ProjectClass;
import pl.edu.mimuw.models.ProjectContainer;
import pl.edu.mimuw.models.ProjectNamespace;

/**
 * @author Paweł Nowosad
 *
 */
public class Neo4jExporter extends AModelExporter {

	static String SERVER_ROOT_URI;

	private Client webClient;
	private WebTarget rootTarget;
	
	private String commitTransactionUrl;
	
	private List<JSONObject> externalJsons;
	
	private ProjectContainer currentContainer;
	private Map<String, ProjectClass> classMap;
	private Map<String, ProjectNamespace> namespaceMap;
	
	/**
	 * 
	 */
	public Neo4jExporter(String databaseUrl, List<JSONObject> externalData) {
		System.out.println("Neo4jExporter created!");
		
		SERVER_ROOT_URI = databaseUrl + "db/data/";
		
		webClient = ClientBuilder.newClient();
		rootTarget = webClient.target(SERVER_ROOT_URI);
		
		classMap = new HashMap<String, ProjectClass>();
		namespaceMap = new HashMap<String, ProjectNamespace>();
		
		externalJsons = externalData;
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
	
	private List<JSONObject> findAllJsonsForIExplorationModelObject(IExplorationModelObject object) {
		String objectName = object.getName();
		
		List<JSONObject> foundJsons = new ArrayList<JSONObject>();
		for (JSONObject jsonObject : externalJsons) {
			JSONObject externalJsonForName = jsonObject.getJSONObject(objectName);
			if (externalJsonForName != null) {
				foundJsons.add(externalJsonForName);
			}
		}
		
		return foundJsons;
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
	public boolean startContainer(IContainer container) {
		this.currentContainer = new ProjectContainer(rootTarget, container, findAllJsonsForIExplorationModelObject(container));
		
		return super.startContainer(container);
	}
	
	@Override
	public boolean finishContainer(IContainer container) {
		
		return super.finishContainer(container);
	}

	@Override
	public boolean startNamespace(INamespace namespace) {
		ProjectNamespace newNamespace = new ProjectNamespace(rootTarget, namespace, findAllJsonsForIExplorationModelObject(namespace));
		
		String namespaceString = namespace != null ? namespace.getName() : "";
		namespaceMap.put(namespaceString != null ? namespaceString : "", newNamespace);
		
		if (currentContainer != null) {
			newNamespace.createRelationship(currentContainer, "part of", null);
		}
		
		return super.startNamespace(namespace);
	}
	
	@Override
	public boolean finishNamespace(INamespace namespace) {
		
		return super.finishNamespace(namespace);
	}
	
	@Override
	public boolean startType(IType type) {
		ProjectClass newClass = classMap.get(type.getName());
		if (newClass == null) {
			newClass = new ProjectClass(rootTarget, type, findAllJsonsForIExplorationModelObject(type));
			classMap.put(type.getName(), newClass);
		}
		
		String classNamespaceString = type.getNamespace().getName();
		ProjectNamespace classNamespace = namespaceMap.get(classNamespaceString != null ? classNamespaceString : "");
		newClass.createRelationship(classNamespace, "belongs to", null);
		
		DependencySet<IType,IType> dependencySet = type.getDependencies();
		List<IDependency<IType, IType>> dependencyList = dependencySet.collect(new IDependencyFilter<IDependency<IType,IType>>() {
			
			@Override
			public boolean matches(IDependency<IType, IType> arg0) {
				return true;
			}
		});
		
		Set<String> matchedDependencies = new HashSet<String>();
		for (IDependency<IType, IType> dependency : dependencyList) {
			String targetName = dependency.getTargetElement().getName();
			String dependencyName = dependency.getDependencyClassification().toString();
			Map<String, String> relProperties = new HashMap<String, String>();
			
			String classificationKey = "classification";
			if (dependency.getDependencyClassification() == DependencyClassification.EXTENSION)
				relProperties.put(classificationKey, "extends");
			else if (dependency.getDependencyClassification() == DependencyClassification.IMPLEMENTATION)
				relProperties.put(classificationKey, "implements");
			else
				relProperties.put(classificationKey, "uses");
			
			if (!matchedDependencies.contains(targetName + dependencyName)) {
				matchedDependencies.add(targetName + dependencyName);
				
				ProjectClass targetClass = classMap.get(targetName);
				if (targetClass == null) {
					targetClass = new ProjectClass(rootTarget, dependency.getTargetElement(), findAllJsonsForIExplorationModelObject(dependency.getTargetElement()));
					classMap.put(targetName, targetClass);
				}
				
				newClass.createRelationship(targetClass, dependencyName, relProperties);
			}
		}
		
		return super.startType(type);
	}
	
	@Override
	public boolean finishType(IType type) {
		
		return super.finishType(type);
	}
	
}
