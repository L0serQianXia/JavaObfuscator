package me.qianxia.obfuscator.transformer;

import java.util.ArrayList;
import java.util.List;

import me.qianxia.obfuscator.transformer.transformers.StringTransformer;

/**
 * @author QianXia
 * @data 2021/2/24
 */
public class Transformers {
	public static List<Transformer> transformers = new ArrayList<>();
	
	static {
		transformers.add(new StringTransformer());
	}
}
