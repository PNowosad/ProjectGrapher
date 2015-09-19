/**
 * 
 */
package pl.edu.mimuw.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.pfsw.odem.IExplorationModelObject;

/**
 * Abstrakcyjny model reprezentujący dowolny element w strukturze projektu oraz jego wierzchołek w grafowej bazie danych.
 * Implementuje podstawową logikę obsługi bazy danych, wspólną dla wszystkich wierzchołków. Jednocześnie udostępniając
 * spójny interfejs do obsługi wierzchołków. Inicjalizuje podstawowe dane na podstawie przekazanego elementu struktury
 * projektu.
 * 
 * @author Paweł Nowosad
 *
 */
public abstract class ProjectObject {
	
	final static String ExtDataLabels = "labels";
	final static String ExtDataProperties = "properties";
	final static String ExtDataRelations = "relations";
	
	private enum HttpRequestMethod {
		GET,
		POST,
		PUT,
		DELETE
	}
	
	private WebTarget nodeTarget;
	private WebTarget labelsTarget;
	
	public String nodeID;
	public String name;
	public String contextName;
	
	protected Map<String, String> properties;
	protected List<String> labels;
	protected List<JSONObject> externalData;
	
	/**
	 * Odpowiada za zainicjalizowanie i przetworzenie danych o elemencie oraz zapisanie go w grafowej bazie danych.
	 * 
	 * @param rootTarget	główny adres do REST API grafowej bazy danych
	 * @param object		obiekt, którym zostanie zainicjalizowany model
	 * @param externalData	dodatkowe dane, które zostaną przypisane do wierzchołka
	 */
	public ProjectObject(WebTarget rootTarget, IExplorationModelObject object, List<JSONObject> externalData) {
		nodeTarget = rootTarget.path("node");
		
		name = object.getName();
		contextName = object.getContext().getName();
		
		this.externalData = externalData;
	}
	
	/**
	 * Odpowiada za zainicjalizowanie i przetworzenie danych o elemencie oraz zapisanie go w grafowej bazie danych bez użycia obiektu
	 * 
	 * @param rootTarget			główny adres do REST API grafowej bazy danych
	 * @param objectName			nazwa obiektu
	 * @param objectContextName		nazwa kontekstu, w którym znajduje się obiekt
	 * @param externalData			dodatkowe dane, które zostaną przypisane do wierzchołka
	 */
	public ProjectObject(WebTarget rootTarget, String objectName, String objectContextName, List<JSONObject> externalData) {
		nodeTarget = rootTarget.path("node");
		
		name = objectName;
		contextName = objectContextName;
		
		this.externalData = externalData;
	}
	
	/**
	 * Wysyła rządanie do serwera bazy danych używając odpowiedniej metody i zawierające przekazane dane.
	 * Odpowiada za zserializowanie i przygotowanie danych do wysłania.
	 * 
	 * @param target	adres, na który ma zostać wysłane rządanie
	 * @param json		niezserializowane dane, które mają zostać wysłane razem z rządaniem
	 * @param method	typ metody jaki ma zostać użyty do wysłania rządania, np. GET, POST
	 * 
	 * @return			odpowiedź serwera na przesłane rządanie
	 */
	protected String sendRequestToTargetWithJSON(WebTarget target, Object json, HttpRequestMethod method) {
		Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON_TYPE);
		
		Entity<String> requestEntity = Entity.entity(json.toString(), MediaType.APPLICATION_JSON_TYPE);
		Response response = null;
		
		switch (method) {
		case GET:
			response = builder.get();
			break;

		case POST:
			response = builder.post(requestEntity);
			break;
		
		case PUT:
			response = builder.put(requestEntity);
			break;
			
		case DELETE:
			response = builder.delete();
			break;
		}
		
		return response.readEntity(String.class);
	}
	
	/**
	 * Przetwarza wszystkie zapisane w modelu dane, zapisuje je do odpowiedniego formatu JSONa, oczekiwanego przez serwer.
	 * Wysyła do serwera rządanie stworzenia wierzchołka z odpowiednimi danymi.
	 * Zapisuje z odpowiedzi serwera przydzielone ID, w celu umożliwienia późniejszej edycji danych wierzchołka.
	 * 
	 * @param properties	właściwości jakie powinny zostać zapisane razem z wierzchołkiem
	 */
	protected void createNode(Map<String, String> properties) {
		this.properties = properties;
		this.properties.put("name", name);
		
		List<String> initLabels = new ArrayList<String>();
		
		if (externalData != null) {
			for (JSONObject objectJsonInfo : externalData) {
				if (objectJsonInfo.has(ExtDataProperties)) {
					JSONObject jsonProperties = objectJsonInfo.getJSONObject(ExtDataProperties);
					for (String key : JSONObject.getNames(jsonProperties)) {
						this.properties.put(key, jsonProperties.get(key).toString());
					}
				}
				
				if (objectJsonInfo.has(ExtDataLabels)) {
					JSONArray jsonLabels = objectJsonInfo.getJSONArray(ExtDataLabels);
					for (int i = 0; i < jsonLabels.length(); i++) {
						initLabels.add(jsonLabels.get(i).toString());
					}
				}
			}
		}
		
		JSONObject propertiesJSON = new JSONObject(this.properties);
		
		String response = sendRequestToTargetWithJSON(nodeTarget, propertiesJSON, HttpRequestMethod.POST);
		
		JSONObject responseJSON = new JSONObject(response);
		JSONObject metadataJSON = responseJSON.getJSONObject("metadata");
		nodeID = Integer.toString(metadataJSON.getInt("id"));
		labelsTarget = nodeTarget.path(nodeID).path("labels");
		
		initLabels.add(contextName);
		addLabels(initLabels);
	}
	
	/**
	 * 
	 * @return	listę wszystkich przypisanych etykiet do wierzchołka
	 */
	public List<String> getLabels() {
		if (labels == null) {
			labels = new ArrayList<String>();
		}
		
		return labels;
	}
	
	/**
	 * Zastępuje poprzednią listę etykiet nową i zapisuje zmiany na serwerze.
	 * 
	 * @param labels	lista nowych etykiet dla wierzchołka
	 */
	protected void setLabels(List<String> labels) {
		this.labels = labels;
		
		JSONArray labelsJSON = new JSONArray(this.labels);
		sendRequestToTargetWithJSON(labelsTarget, labelsJSON, HttpRequestMethod.PUT);
	}
	
	/**
	 * Dodaje nową etykietę do aktualnej listy i zapisuje zmiany na serwerze.
	 * 
	 * @param label		nowa etykieta dla wierzchołka 
	 */
	protected void addLabel(String label) {
		List<String> labelsList = new ArrayList<String>();
		labelsList.add(label);
		
		addLabels(labelsList);
	}
	
	/**
	 * Dokleja do obecnej listy etykiet nową i zapisuje zmiany na serwerze.
	 * 
	 * @param labels	lista nowych etykiet 
	 */
	protected void addLabels(List<String> labels) {
		getLabels().addAll(labels);
		
		JSONArray labelsJSON = new JSONArray(labels);
		sendRequestToTargetWithJSON(labelsTarget, labelsJSON, HttpRequestMethod.POST);
	}

	/**
	 * Tworzy nową, skierowaną krawędź w grafie od tego wierzchołka do wierzchołka przekazanego w parametrze.
	 * Zapisuje zmiany na serwerze.
	 * 
	 * @param toObject		obiekt, z którym ma być połączony krawędzią ten wierzchołek; krawędź będzie skierowana do przekazanego obiektu
	 * @param type			typ relacji, zostanie zapisany jako etykieta krawędzi
	 * @param properties	właściwości jakie mają być przypisane dla tworzonej krawędzi
	 */
	public void createRelationship(ProjectObject toObject, String type, Map<String, String> properties) {
		JSONObject requestJSON = new JSONObject();
		requestJSON.put("to", nodeTarget.path(toObject.nodeID).getUri().toString());
		requestJSON.put("type", type);
		if (properties != null)
			requestJSON.put("data", new JSONObject(properties));
		
		sendRequestToTargetWithJSON(nodeTarget.path(nodeID).path("relationships"), requestJSON, HttpRequestMethod.POST);
	}
	
}
