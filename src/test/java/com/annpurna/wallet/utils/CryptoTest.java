package com.annpurna.wallet.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import java.util.Base64;

import com.annpurna.wallet.crypto.CryptoUtil;

public class CryptoTest {

	private static final String PRIV_KEY_FILE = "/home/atri/workspace_hlf/annpurna-cli/users/user1/pkcs8.pem" ;
	private static final String PUB_KEY_FILE = "/home/atri/workspace_hlf/annpurna-cli/users/user1/pub.crt" ;
	
	public static void main(String args[]) throws Exception {
		cryptoKeyPairTest();
	}
	
	private static void cryptoKeyPairTest() throws Exception {
		byte[] data = "1234567".getBytes() ;
		KeyPair keyPair = CryptoUtil.generateKeyPair() ;
		PrivateKey privKey = keyPair.getPrivate();
		System.out.println("format:"+privKey.getFormat());
		
		
		
		String base64EncodedPrivKey = Base64.getEncoder().encodeToString(privKey.getEncoded());
		System.out.println("base64EncodedPrivKey: "+base64EncodedPrivKey);
		byte [] base64DecodedPrivKey = Base64.getDecoder().decode(base64EncodedPrivKey) ;
		
		System.out.println("SHA3:"+CryptoUtil.generateSHA3Hash(base64EncodedPrivKey));
		System.out.println("SHA3:"+CryptoUtil.generateSHA3Hash(base64EncodedPrivKey));
		System.out.println("SHA3:"+CryptoUtil.generateSHA3Hash(base64EncodedPrivKey));
		
		
		privKey= CryptoUtil.generatePKCS8EncodedPrivateKey(base64DecodedPrivKey);
		
		byte[] signature = CryptoUtil.signWithPrivatekey(data,privKey);
		
		byte[] encodedSign = Base64.getEncoder().encode(signature);
		String signStr = new String(encodedSign);
		System.out.println("signStr: "+signStr);
		byte[] decodedSign = Base64.getDecoder().decode(signStr.getBytes()) ;
		data = "1234567".getBytes() ;
		System.out.println("Public Key verification");
		PublicKey pubKey = keyPair.getPublic();
		System.out.println("format:"+pubKey.getFormat() + ",length:" + pubKey.getEncoded().length);
		
		
		PublicKey pubKeyFromPrivKey = CryptoUtil.getPublicKeyfromPrivateKey(privKey);
		//pubKey = pubKeyFromPrivKey;
		
		String base64EncodedPubKey = Base64.getEncoder().encodeToString(pubKey.getEncoded());
		String base64EncodedPubKey2 = Base64.getEncoder().encodeToString(pubKeyFromPrivKey.getEncoded());
		System.out.println("base64EncodedPubKey: "+base64EncodedPubKey);
		System.out.println("base64EncodedPubKey2: "+base64EncodedPubKey2);
	
		System.out.println("SHA3:"+CryptoUtil.generateSHA3Hash(base64EncodedPubKey));
		System.out.println("SHA3:"+CryptoUtil.generateSHA3Hash(base64EncodedPubKey));
		System.out.println("SHA3:"+CryptoUtil.generateSHA3Hash(base64EncodedPubKey));

		System.out.println("SHA3:"+CryptoUtil.generateSHA3Hash(base64EncodedPubKey2));
		System.out.println("SHA3:"+CryptoUtil.generateSHA3Hash(base64EncodedPubKey2));
		System.out.println("SHA3:"+CryptoUtil.generateSHA3Hash(base64EncodedPubKey2));
		
		byte [] base64DecodedPubKey = Base64.getDecoder().decode(base64EncodedPubKey) ;
		
		
		//pubKey = CryptoUtil.generateX509EncodedPublicKey(base64DecodedPubKey) ;
		
		System.out.println("verified:"+CryptoUtil.verifySinature(decodedSign, data,pubKeyFromPrivKey));
	}
	
	private static void cryptoTest() throws Exception{
		byte[] data = "1234567".getBytes() ;
		System.out.println("PRIV_KEY_FILE");
		System.out.println(CryptoUtil.getCertificateAsText(PRIV_KEY_FILE));
		byte[] signature = CryptoUtil.signWithPrivatekey(data, PRIV_KEY_FILE) ;
		byte[] encodedSign = Base64.getEncoder().encode(signature);
		String signStr = new String(encodedSign);
		System.out.println("base64 encoded sign:"+signStr);
		byte[] decodedSign = Base64.getDecoder().decode(signStr.getBytes()) ;
		data = "1234567".getBytes() ;
		System.out.println("PUB_KEY_FILE");
		System.out.println(CryptoUtil.getCertificateAsText(PUB_KEY_FILE));
		System.out.println("verified:"+CryptoUtil.verifySinature(decodedSign, data, PUB_KEY_FILE));
	}
}
