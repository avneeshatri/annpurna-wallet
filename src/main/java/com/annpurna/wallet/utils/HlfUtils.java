package com.annpurna.wallet.utils;

public class HlfUtils {
    
    public static String buildCollectionName(String clientOrgID)  {
    	return String.format("_implicit_org_%s", clientOrgID);
    }
}
