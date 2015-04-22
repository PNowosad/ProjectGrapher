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
	
	protected int nodeID;
	protected Map<String, String> properties;
	protected List<String> labels;
	
	/**
	 * 
	 */
	public ProjectObject(WebTarget rootTarget) {
//		this.rootTarget = rootTarget;
		
		nodeTarget = rootTarget.path("node");
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
		
		JSONObject propertiesJSON = new JSONObject(this.properties);
		
		Response response = sendRequestToTargetWithJSON(nodeTarget, propertiesJSON, HttpRequestMethod.POST);
		
		JSONObject responseJSON = new JSONObject(response.readEntity(String.class));
		JSONObject metadataJSON = responseJSON.getJSONObject("metadata");
		nodeID = metadataJSON.getInt("id");
		labelsTarget = nodeTarget.path(Integer.toString(nodeID)).path("labels");
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
