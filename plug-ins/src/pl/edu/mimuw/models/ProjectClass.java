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
	public ProjectClass(WebTarget rootTarget, IType classType) {
		super(rootTarget);
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("name", classType.getName());
		properties.put("class_name", classType.getUnqualifiedName());
		properties.put("visibility", classType.getVisibility().toString());
		properties.put("is_abstract", classType.isAbstract() ? "true" : "false");
		properties.put("is_final", classType.isFinal() ? "true" : "false");
		createNode(properties);
		
		List<String> labels = new ArrayList<String>();
		labels.add(classType.getClassification().toString());
		labels.add(classType.getContext().getName());
		if (classType.isAbstract())
			labels.add("abstract");
		if (classType.isFinal())
			labels.add("final");
		this.addLabels(labels);
		
//		System.out.println("Node for class " + classType.getName() + " created with ID - " + nodeID);
	}

}
