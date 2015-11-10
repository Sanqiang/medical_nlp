package edu.pitt.medical_nlp.graph;

import java.util.ArrayList;
import java.util.HashMap;

import edu.pitt.medical_nlp.utility.DependencyType;

public class Graph {
	HashMap<Integer, Node> nodes = new HashMap<>();
	ArrayList<Edge> edges = new ArrayList<>();

	public void createEdge(String lemma_adj, String lemma_n, int idx_adj, int idx_n, DependencyType type) {
		Node node_adj = new Node(idx_adj, lemma_adj);
		Node node_n = new Node(idx_n, lemma_n);
		nodes.put(idx_adj, node_adj);
		nodes.put(idx_n, node_n);

		Edge edge = new Edge(node_adj, node_n, type);
		edges.add(edge);
		node_adj.links.add(edge);
		node_n.links.add(edge);
	}

	public ArrayList<String> generateFeatures() {
		ArrayList<String> features = new ArrayList<>();
		DependencyType[] modifys = { DependencyType.AdjectiveModifer, DependencyType.NominalSubject,
				DependencyType.Negative };

		for (DependencyType dependencyType : modifys) {
			for (Edge edge : edges) {
				if (edge.type == dependencyType) {
					String feature = edge.node_adj.lemma + "_" + edge.node_n.lemma;
					features.add(feature);

					for (Edge edge_loop : edge.node_n.links) {
						if (edge_loop.type == DependencyType.Compound) {
							String feature_loop = edge.node_adj.lemma + "_" + edge_loop.getOtherNode(edge.node_n).lemma;
							features.add(feature_loop);
						}
					}

				}
			}
		}

		return features;
	}
}
