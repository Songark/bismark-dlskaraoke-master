package com.karaoke.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import android.annotation.SuppressLint;
import android.util.Base64;

@SuppressLint("TrulyRandom")
public class TripleDes {

	private final static String keyString = "aWJaZGVmZ2hpamtsbW5vcHFyc3R5dnd4";
	private final static String ivString = "aCNPJDbc";
	
	public static String encrypt(String data) throws Exception {
		
		if (data == null || data.equals(""))
		{
		    return null;
		}
		
		MessageDigest md = MessageDigest.getInstance("md5");
		final byte[] digestOfPassword = md.digest(Base64.decode(keyString.getBytes("UTF-8"), Base64.DEFAULT));
		final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
		for (int j = 0, k = 16; j < 8;)
		{
		    keyBytes[k++] = keyBytes[j++];
		}

		KeySpec keySpec = new DESedeKeySpec(keyString.getBytes());
		SecretKey key = SecretKeyFactory.getInstance("DESede").generateSecret(keySpec);
		IvParameterSpec iv = new IvParameterSpec(ivString.getBytes());
		Cipher ecipher = Cipher.getInstance("DESede/CFB8/NoPadding");
		ecipher.init(Cipher.ENCRYPT_MODE, key, iv);
			
		byte[] valeur = data.getBytes("UTF-8");
		byte[] enc = ecipher.doFinal(valeur);
		
		return new String(Base64.encode(enc, Base64.DEFAULT), "UTF-8");
	}
	
	public static String decrypt(String data) throws Exception {
		
		if(data == null || data.equals(""))
		{
			return "";
		}
		
		MessageDigest md = MessageDigest.getInstance("md5");
		final byte[] digestOfPassword = md.digest(Base64.decode(keyString.getBytes("UTF-8"), Base64.DEFAULT));
		final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
		
		for (int j = 0, k = 16; j < 8;)
		{
		    keyBytes[k++] = keyBytes[j++];
		}
		
		KeySpec keySpec = new DESedeKeySpec(keyString.getBytes());
		SecretKey key = SecretKeyFactory.getInstance("DESede").generateSecret(keySpec);
		IvParameterSpec iv = new IvParameterSpec(ivString.getBytes());
		Cipher ecipher = Cipher.getInstance("DESede/CFB8/NoPadding");
		ecipher.init(Cipher.DECRYPT_MODE, key, iv);
			
		byte [] dt = Base64.decode(data.getBytes("UTF-8"), Base64.DEFAULT);
		
		return new String(ecipher.doFinal(dt), "UTF-8");
	}
	
	// MD5
	public static String md5(String data)
	{
		try
		{
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(data.getBytes(), 0, data.length());
			String result = "";
			
			result = new BigInteger(1, m.digest()).toString(16);
			return result;
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}