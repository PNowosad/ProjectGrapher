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
 * @author Paweł Nowosad
 *
 */
public abstract class ProjectObject {
	private enum HttpRequestMethod {
		GET,
		POST,
		PUT,
		DELETE
	}
	
//	private WebTarget rootTarget;
	private WebTarget nodeTarget;
	private WebTarget labelsTarget;
	
	public String nodeID;
	public String name;
	public String contextName;
	
	protected Map<String, String> properties;
	protected List<String> labels;
	
	/**
	 * 
	 */
	public ProjectObject(WebTarget rootTarget, IExplorationModelObject object) {
//		this.rootTarget = rootTarget;
		
		nodeTarget = rootTarget.path("node");
		
		name = object.getName();
		contextName = object.getContext().getName();
	}
	
	protected Response sendRequestToTargetWithJSON(WebTarget target, Object json, HttpRequestMethod method) {
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
		
		return response;
	}
	
	protected void createNode(Map<String, String> properties) {
		this.properties = properties;
		this.properties.put("name", name);
		
		JSONObject propertiesJSON = new JSONObject(this.properties);
		
		Response response = sendRequestToTargetWithJSON(nodeTarget, propertiesJSON, HttpRequestMethod.POST);
		
		JSONObject responseJSON = new JSONObject(response.readEntity(String.class));
		JSONObject metadataJSON = responseJSON.getJSONObject("metadata");
		nodeID = Integer.toString(metadataJSON.getInt("id"));
		labelsTarget = nodeTarget.path(nodeID).path("labels");
		
		addLabel(contextName);
	}
	
	public List<String> getLabels() {
		if (labels == null) {
			labels = new ArrayList<String>();
		}
		
		return labels;
	}
	
	protected void setLabels(List<String> labels) {
		this.labels = labels;
		
		JSONArray labelsJSON = new JSONArray(this.labels);
		sendRequestToTargetWithJSON(labelsTarget, labelsJSON, HttpRequestMethod.PUT);
	}
	
	protected void addLabel(String label) {
		List<String> labelsList = new ArrayList<String>();
		labelsList.add(label);
		
		addLabels(labelsList);
	}
	
	protected void addLabels(List<String> labels) {
		getLabels().addAll(labels);
		
		JSONArray labelsJSON = new JSONArray(labels);
		sendRequestToTargetWithJSON(labelsTarget, labelsJSON, HttpRequestMethod.POST);
	}

	public void createRelationship(ProjectObject toObject, String type, Map<String, String> properties) {
		JSONObject requestJSON = new JSONObject();
		requestJSON.put("to", nodeTarget.path(toObject.nodeID).getUri().toString());
		requestJSON.put("type", type);
		if (properties != null)
			requestJSON.put("data", new JSONObject(properties));
		
		sendRequestToTargetWithJSON(nodeTarget.path(nodeID).path("relationships"), requestJSON, HttpRequestMethod.POST);
	}
	
}
