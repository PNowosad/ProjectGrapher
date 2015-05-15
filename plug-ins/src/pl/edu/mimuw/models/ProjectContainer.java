/**
 * 
 */
package pl.edu.mimuw.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.WebTarget;

import org.pfsw.odem.IContainer;

/**
 * @author Paweł Nowosad
 *
 */
public class ProjectContainer extends ProjectObject {
	
	/**
	 * 
	 */
	public ProjectContainer(WebTarget rootTarget, IContainer container) {
		super(rootTarget, container);
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("classification", container.getClassification().toString());
		createNode(properties);
		
		List<String> labels = new ArrayList<String>();
		labels.add("container");
		this.addLabels(labels);
	}

}
