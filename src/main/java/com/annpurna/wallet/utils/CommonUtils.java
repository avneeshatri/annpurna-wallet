package com.annpurna.wallet.utils;

public class CommonUtils {
	
    public static boolean  isBlank(final Object data){
    	if (data == null){
    		return true ;
    	} else if(data instanceof String && ((String)data).trim().isEmpty()) {
    		return true ;
    	}
    	
    	return false ;
    }
}
