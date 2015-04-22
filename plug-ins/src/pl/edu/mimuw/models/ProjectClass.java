/**
 * 
 */
package pl.edu.mimuw.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.WebTarget;

import org.pfsw.odem.IType;

/**
 * @author Paweł Nowosad
 *
 */
public class ProjectClass extends ProjectObject {

	/**
	 * 
	 */
	public ProjectClass(WebTarget rootTarget, IType classType, String contextName) {
		super(rootTarget);
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("name", classType.getName());
		properties.put("class_name", classType.getUnqualifiedName());
		createNode(properties);
		
		List<String> labels = new ArrayList<String>();
		labels.add("class");
		labels.add(contextName);
	}

}
