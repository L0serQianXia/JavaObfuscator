package me.qianxia.obfuscator.transformer;

/**
 * @author QianXia
 * @data 2021/2/24
 */
public abstract class Transformer {
	private String name;
	
	public Transformer(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	/**
	 *  执行混淆
	 * @return 混淆执行次数
	 */
	public abstract int run();
}
