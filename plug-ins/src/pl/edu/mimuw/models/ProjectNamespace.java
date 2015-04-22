/**
 * 
 */
package pl.edu.mimuw.models;

import java.util.HashMap;

import javax.ws.rs.client.WebTarget;

import org.pfsw.odem.INamespace;

/**
 * @author Paweł Nowosad
 *
 */
public class ProjectNamespace extends ProjectObject {
	
	/**
	 * @param rootTarget
	 */
	public ProjectNamespace(WebTarget rootTarget, INamespace namespace) {
		super(rootTarget, namespace);
		
		createNode(new HashMap<String, String>());
		addLabel("namespace");
	}

}
