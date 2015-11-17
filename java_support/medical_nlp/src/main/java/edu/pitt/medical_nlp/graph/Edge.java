package edu.pitt.medical_nlp.graph;

import edu.pitt.medical_nlp.utility.DependencyType;

public class Edge {
	DependencyType type;
	WordNode node_adj, node_n;

	public Edge(WordNode node_adj, WordNode node_n, DependencyType type) {
		this.node_adj = node_adj;
		this.node_n = node_n;
		this.type = type;
	}

	public WordNode getOtherNode(WordNode node) {
		if (node == node_adj) {
			return node_n;
		} else if (node == node_n) {
			return node_adj;
		} else {
			System.err.println("get other node error");
			return null;
		}
	}
}
