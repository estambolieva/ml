package com.wordpress.moreintelligent.util;

import java.util.HashMap;

import com.wordpress.moreintelligent.util.bloomier.ImmutableBloomierFilter;

public class CreateBloorimerFilter {
	
	/*
	 * q must be chosen to be large as the false-positive rate is proportional to 2 to the power of -q 
	 */
	ImmutableBloomierFilter<String, Integer> bloomier;
	HashMap<String, Integer> map;
	
	private void loadMap(){
		map = new HashMap<String, Integer>();
		map.put("ivan", 1);
		map.put("stamboliev", 243);
	}
	
	public void createBloomierFilter(HashMap<String, Integer> mapToFilter) throws Exception{
		bloomier = new ImmutableBloomierFilter<String, Integer>(mapToFilter, 10, 10, 32, Integer.class, 10000);
		System.out.println(bloomier.get("ivan"));
		bloomier.get(("stamboliev"));
	}
	
	public static void main(String[] args) throws Exception{
		CreateBloorimerFilter cc = new CreateBloorimerFilter();
		cc.loadMap();
		cc.createBloomierFilter(cc.map);
	}
}
