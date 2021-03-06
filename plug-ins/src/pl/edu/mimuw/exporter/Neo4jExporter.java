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
 * Główna klasa przetwarzająca wszystkie elementy struktury projektu. Realizuje wzorzec projektowy "Visitor".
 * Odpowiada za stworzenie odpowiednich modeli dla każdego elementu oraz połączenie ich w odpowiednie relacje.
 * Nawiązuje połączenie z grafową bazą danych.
 * 
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
	private Map<String, ProjectContainer> containerMap;
	
	/**
	 * Główny konstruktor odpowiedzialny za zainicjalizowanie eksportera oraz stworzenie klienta do komunikacji z bazą danych.
	 * 
	 * @param databaseUrl	adres do serwera bazy danych, gdzie zostanie zapisany graf
	 * @param externalData	wczytane do pamięci z plików JSONy z dodatkowymi danymi do zapisania w grafie
	 */
	public Neo4jExporter(String databaseUrl, List<JSONObject> externalData) {
		System.out.println("Neo4jExporter created!");
		
		SERVER_ROOT_URI = databaseUrl + "db/data/";
		
		webClient = ClientBuilder.newClient();
		rootTarget = webClient.target(SERVER_ROOT_URI);
		
		classMap = new HashMap<String, ProjectClass>();
		namespaceMap = new HashMap<String, ProjectNamespace>();
		containerMap = new HashMap<String, ProjectContainer>();
		
		externalJsons = externalData;
	}

	/**
	 * @return Nazwę dostawcy wtyczki, np. osoba, organizacja.
	 * 
	 * @see org.pf.tools.cda.xpi.IPluginInfo#getPluginProvider()
	 */
	@Override
	public String getPluginProvider() {
		return "Paweł Nowosad";
	}

	/**
	 * @return Wersję wtyczki, np. "1.0"
	 * 
	 * @see org.pf.tools.cda.xpi.IPluginInfo#getPluginVersion()
	 */
	@Override
	public String getPluginVersion() {
		return "0.1";
	}

	/**
	 * Odpowiada za zainicjalizowanie eksportera.
	 * 
	 * @return <code>true</code> jeśli kontynuować przetwarzanie, <code>false</code> jeśli zatrzymać
	 * 
	 * @see org.pf.tools.cda.plugin.export.spi.AModelExporter#initialize(org.pf.tools.cda.xpi.PluginConfiguration)
	 */
	@Override
	public boolean initialize(PluginConfiguration arg0) {

		return true;
	}
	
	/**
	 * Znajduje wszystkie dodatkowe dane jakie powinny być przypisane do przekazanego w parametrze obiektu.
	 * 
	 * @param object	element struktury projektu, dla którego będą znalezione wszystkie dodatkowe informacje
	 * 
	 * @return			lista wszystkich znalezionych informacji o tym obiekcie
	 */
	private List<JSONObject> findAllJsonsForIExplorationModelObject(IExplorationModelObject object) {
		String objectName = object.getName();
		
		List<JSONObject> foundJsons = new ArrayList<JSONObject>();
		for (JSONObject jsonObject : externalJsons) {
			if (jsonObject.has(objectName)) {
				JSONObject externalJsonForName = jsonObject.getJSONObject(objectName);
				foundJsons.add(externalJsonForName);
			}
		}
		
		return foundJsons;
	}
	
	/**
	 * Rozpoczyna nową transakcję w grafowej bazie danych.
	 */
	public void beginNewTransaction() {
		WebTarget newTransactionTarget = rootTarget.path("transaction");
		
		Invocation.Builder newTransactionInvBuilder = newTransactionTarget.request();
		ClientResponse response = newTransactionInvBuilder.post(Entity.entity("{'statements':[]}", MediaType.APPLICATION_JSON_TYPE), ClientResponse.class);
		JSONObject responseJSON = new JSONObject(response.readEntity(String.class));
		commitTransactionUrl = responseJSON.getString("commit");
	}
	
	/**
	 * Zamyka i zatwierdza transakcję w grafowej bazie danych.
	 */
	public void commitTransaction() {
		WebTarget commitTransactionTarget = webClient.target(commitTransactionUrl);
		commitTransactionTarget.request().post(Entity.entity("{'statements':[]}", MediaType.APPLICATION_JSON_TYPE));
		
		commitTransactionUrl = "";
	}
	
	/**
	 * Rozpoczyna przetwarzanie drzewa dla danego kontekstu.
	 * 
	 * @param context	aktualny kontekst
	 * 
	 * @return			zwraca czy kontynuować przetwarzanie
	 */
	@Override
	public boolean startContext(IExplorationContext context) {
		
		return super.startContext(context);
	}
	
	/**
	 * Kończy przetwarzanie drzewa dla danego kontekstu.
	 * 
	 * @param context	aktualny kontekst
	 * 
	 * @return			zwraca czy kontynuować przetwarzanie
	 */
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
	
	/**
	 * Rozpoczyna przetwarzanie drzewa dla danego kontenera. Tworzy model dla nowego kontenera.
	 * 
	 * @param container	aktualny kontener
	 * 
	 * @return			zwraca czy kontynuować przetwarzanie
	 */
	@Override
	public boolean startContainer(IContainer container) {
		String containerName = container.getName();
		if (this.containerMap.containsKey(containerName))
			this.currentContainer = this.containerMap.get(containerName);
		else {
			this.currentContainer = new ProjectContainer(rootTarget, container, findAllJsonsForIExplorationModelObject(container));
			containerMap.put(this.currentContainer.name, this.currentContainer);
		}
		
		IContainer parentContainer = container.getParentContainer();
		if (parentContainer != null) {
			String parentContainerName = parentContainer.getName();
			ProjectContainer parentContainerNode = null;
			if (containerMap.containsKey(parentContainerName))
				parentContainerNode = containerMap.get(parentContainerName);
			else {
				parentContainerNode = new ProjectContainer(rootTarget, parentContainer, findAllJsonsForIExplorationModelObject(parentContainer));
				containerMap.put(parentContainerName, parentContainerNode);
			}
			
			this.currentContainer.createRelationship(parentContainerNode, "contained in", null);
		}
		
		return super.startContainer(container);
	}
	
	/**
	 * Kończy przetwarzanie drzewa dla danego kontenera.
	 * 
	 * @param container	aktualny kontener
	 * 
	 * @return			zwraca czy kontynuować przetwarzanie
	 */
	@Override
	public boolean finishContainer(IContainer container) {
		
		return super.finishContainer(container);
	}

	/**
	 * Rozpoczyna przetwarzanie drzewa dla danej przestrzeni nazw. Tworzy model dla nowej przestrzeni nazw.
	 * 
	 * @param namespace	aktualna przestrzeń nazw
	 * 
	 * @return			zwraca czy kontynuować przetwarzanie
	 */
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
	
	/**
	 * Kończy przetwarzanie drzewa dla danej przestrzeni nazw.
	 * 
	 * @param namespace	aktualna przestrzeń nazw
	 * 
	 * @return			zwraca czy kontynuować przetwarzanie
	 */
	@Override
	public boolean finishNamespace(INamespace namespace) {
		
		return super.finishNamespace(namespace);
	}
	
	/**
	 * Rozpoczyna przetwarzanie drzewa dla danego typu. Tworzy model dla nowego typu.
	 * Tworzy odpowiednie relacje pomiędzy typami i przestrzeniami nazw.
	 * 
	 * @param type	aktualny typ
	 * 
	 * @return		zwraca czy kontynuować przetwarzanie
	 */
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
	
	/**
	 * Kończy przetwarzanie drzewa dla danego typu.
	 * 
	 * @param type	aktualny typ
	 * 
	 * @return		zwraca czy kontynuować przetwarzanie
	 */
	@Override
	public boolean finishType(IType type) {
		
		return super.finishType(type);
	}
	
}
