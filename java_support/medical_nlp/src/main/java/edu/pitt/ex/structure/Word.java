package edu.pitt.ex.structure;

import edu.stanford.nlp.ling.HasWord;

public class Word implements HasWord {

	private static final long serialVersionUID = 1L;
	public String token;
	public int idx;
	
	public Word(String token, int idx){
		this.token = token;
		this.idx = idx;
	}

	@Override
	public String word() {
		// TODO Auto-generated method stub
		return token;
	}

	@Override
	public void setWord(String word) {
		this.token = word;
	}
}
