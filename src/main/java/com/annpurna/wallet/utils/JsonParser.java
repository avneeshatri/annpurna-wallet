package com.annpurna.wallet.utils;


import com.google.gson.Gson;
import com.owlike.genson.Genson;

public class JsonParser {
/*	private static final Genson genson = new Genson();
	
	public static <T> T deserialize(String json, Class<T> classz){
		return genson.deserialize(json, classz) ;
	}
	
	public static String serialize(Object pojo){
		return genson.serialize(pojo) ;
	}*/
	
	private static Gson gson = new Gson();

	public static  <T>  T deserialize(String json, Class <T> t) {
		return gson.fromJson(json, t);
	}
	
	public static String serialize(Object obj) {
		return gson.toJson(obj);
	}
}
