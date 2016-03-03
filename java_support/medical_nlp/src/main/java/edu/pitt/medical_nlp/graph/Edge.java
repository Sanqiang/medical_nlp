package edu.pitt.medical_nlp.graph;

import edu.pitt.medical_nlp.utility.DependencyType;

public class Edge {
	public DependencyType type;
	public String type_str ;
	public WordNode node_gov, node_dep;

	public Edge(WordNode node_gov, WordNode node_dep, DependencyType type) {
		this.node_gov = node_gov;
		this.node_dep = node_dep;
		this.type = type;
	}
	
	public Edge(WordNode node_gov, WordNode node_dep, String type_str) {
		this.node_gov = node_gov;
		this.node_dep = node_dep;
		this.type_str = type_str;
	}

	public WordNode getOtherNode(WordNode node) {
		if (node == node_gov) {
			return node_dep;
		} else if (node == node_dep) {
			return node_gov;
		} else {
			System.err.println("get other node error");
			return null;
		}
	}
}
