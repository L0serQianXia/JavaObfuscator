package me.qianxia.obfuscator.transformer;

public abstract class Transformer {
	private String name;
	
	public Transformer(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public abstract int run();
}
