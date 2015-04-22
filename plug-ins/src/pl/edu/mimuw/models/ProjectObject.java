/**
 * 
 */
package pl.edu.mimuw.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.POST;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONObject;

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
	
	private WebTarget rootTarget;
	private WebTarget nodeTarget;
	private WebTarget labelsTarget;
	
	protected String nodeID;
	protected Map<String, String> properties;
	protected List<String> labels;
	
	/**
	 * 
	 */
	public ProjectObject(WebTarget rootTarget) {
		this.rootTarget = rootTarget;
		
		nodeTarget = rootTarget.path("node");
	}
	
	protected ClientResponse sendRequestToTargetWithJSON(WebTarget target, Object json, HttpRequestMethod method) {
		Invocation.Builder builder = target.request();
		
		Entity<String> requestEntity = Entity.entity(json.toString(), MediaType.APPLICATION_JSON_TYPE);
		ClientResponse response = null;
		
		switch (method) {
		case GET:
			response = builder.get(ClientResponse.class);
			break;

		case POST:
			response = builder.post(requestEntity, ClientResponse.class);
			break;
		
		case PUT:
			response = builder.put(requestEntity, ClientResponse.class);
			break;
			
		case DELETE:
			response = builder.delete(ClientResponse.class);
			break;
		}
		
		return response;
	}
	
	protected void createNode(Map<String, String> properties) {
		this.properties = properties;
		
		JSONObject propertiesJSON = new JSONObject(this.properties);
		
		ClientResponse response = sendRequestToTargetWithJSON(nodeTarget, propertiesJSON, HttpRequestMethod.POST);
		
		JSONObject responseJSON = new JSONObject(response.readEntity(String.class));
		JSONObject metadataJSON = responseJSON.getJSONObject("metadata");
		nodeID = metadataJSON.getString("id");
		labelsTarget = nodeTarget.path(nodeID).path("labels");
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
		getLabels().add(label);
		
		sendRequestToTargetWithJSON(labelsTarget, label, HttpRequestMethod.POST);
	}
	
	protected void addLabels(List<String> labels) {
		getLabels().addAll(labels);
		
		JSONArray labelsJSON = new JSONArray(labels);
		sendRequestToTargetWithJSON(labelsTarget, labelsJSON, HttpRequestMethod.POST);
	}

}
