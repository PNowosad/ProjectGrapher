/**
 * 
 */
package pl.edu.mimuw.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.WebTarget;

import org.json.JSONObject;
import org.pfsw.odem.IType;

/**
 * Model reprezentujący pojedynczy typ w projekcie, a jednocześnie wierzchołek w grafowej bazie danych.
 * 
 * @author Paweł Nowosad
 *
 */
public class ProjectClass extends ProjectObject {

	/**
	 * Odpowiada za zainicjalizowanie i przetworzenie danych o typie oraz zapisanie go w grafowej bazie danych.
	 * 
	 * @param rootTarget	główny adres do REST API grafowej bazy danych
	 * @param classType		obiekt, którym zostanie zainicjalizowany model
	 * @param externalData	dodatkowe dane, które zostaną przypisane do wierzchołka
	 */
	public ProjectClass(WebTarget rootTarget, IType classType, List<JSONObject> externalData) {
		super(rootTarget, classType, externalData);
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("class_name", classType.getUnqualifiedName());
		properties.put("visibility", classType.getVisibility().toString());
		properties.put("is_abstract", classType.isAbstract() ? "true" : "false");
		properties.put("is_final", classType.isFinal() ? "true" : "false");
		createNode(properties);
		
		List<String> labels = new ArrayList<String>();
		labels.add("type");
		labels.add(classType.getClassification().toString());
		if (classType.isAbstract())
			labels.add("abstract");
		if (classType.isFinal())
			labels.add("final");
		this.addLabels(labels);
	}

}
