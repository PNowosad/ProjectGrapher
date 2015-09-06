/**
 * 
 */
package pl.edu.mimuw.models;

import java.util.HashMap;
import java.util.List;

import javax.ws.rs.client.WebTarget;

import org.json.JSONObject;
import org.pfsw.odem.INamespace;

/**
 * Model reprezentujący pojedynczą przestrzeń nazw w projekcie.
 * 
 * {@inheritDoc}
 * 
 * @author Paweł Nowosad
 *
 */
public class ProjectNamespace extends ProjectObject {
	
	/**
	 * {@inheritDoc} 
	 */
	public ProjectNamespace(WebTarget rootTarget, INamespace namespace, List<JSONObject> externalData) {
		super(rootTarget, namespace, externalData);
		
		createNode(new HashMap<String, String>());
		addLabel("namespace");
	}

}
