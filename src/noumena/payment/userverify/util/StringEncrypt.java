package noumena.payment.userverify.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.AlgorithmParameters;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.digest.DigestUtils;

import noumena.payment.userverify.ChannelVerify;

public class StringEncrypt
{
	public static String Encrypt(String strSrc, String encName)
	{
		MessageDigest md = null;
		String strDes = null;

		try
		{
			byte[] bt = strSrc.getBytes("utf-8");
			md = MessageDigest.getInstance(encName);
			md.update(bt);
			strDes = bytes2Hex(md.digest()); // to HexString
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return strDes;
	}
	
	public static String Encrypt(String strSrc)
	{
		return Encrypt(strSrc, "MD5");
	}

	public static String EncryptSHA256(String strSrc)
	{
		return Encrypt(strSrc, "SHA-256");
	}
	public static String bytes2Hex(byte[] bts)
	{
		String des = "";
		String tmp = null;
		for (int i = 0; i < bts.length; i++)
		{
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1)
			{
				des += "0";
			}
			des += tmp;
		}
		return des;
	}

	public static String md5(String strSrc)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			sun.misc.BASE64Encoder baseEncoder = new sun.misc.BASE64Encoder();
			return baseEncoder.encode(md.digest(strSrc.getBytes()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}

	}

	public static String sha(String strSrc)
	{
		return sha(strSrc, "utf8");
	}

	public static String sha(String strSrc, String characterset)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("sha1");
			sun.misc.BASE64Encoder baseEncoder = new sun.misc.BASE64Encoder();
			return baseEncoder.encode(md.digest(strSrc.getBytes(characterset)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}

	}
	
	public static String dmd5(String strSrc)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			sun.misc.BASE64Encoder baseEncoder = new sun.misc.BASE64Encoder();
			return baseEncoder.encode(md.digest(md.digest(strSrc.getBytes())));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "";
		}

	}

	public static String doPost(String url, String content)
	{
		String res = "";
		try
		{
			URL u = new URL(url);
			HttpURLConnection uc = (HttpURLConnection) u.openConnection();
			uc.setRequestMethod("POST");
			uc.setReadTimeout(60000);
			uc.setRequestProperty("Content-type", "text/xml");
			uc.setDoInput(true);
			uc.setDoOutput(true);
			OutputStreamWriter outs = new OutputStreamWriter(uc.getOutputStream(), "UTF-8");
			outs.write(content);
			outs.flush();
			outs.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String line = null;
			while ((line = in.readLine()) != null)
			{
				res += line;
			}
			in.close();
			uc.disconnect();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return res;
	}

	public static byte[] encryptAES(String content, String password)
	{
		try
		{
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128, new SecureRandom(password.getBytes()));
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);
			return result; // 加密
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decryptAES(byte[] content, String password)
	{
		try
		{
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(128, new SecureRandom(password.getBytes()));
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(content);
			return result; // 加密
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static String encryptAESIV(String content, String key, String iv) {
		try {
			byte[] bText=null;
//			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			AlgorithmParameters mAlgo =AlgorithmParameters.getInstance("AES");
			mAlgo.init(new IvParameterSpec(iv.getBytes()));
			SecretKey secretKey = new SecretKeySpec(key.getBytes(), "AES");
			Cipher mCipher =Cipher.getInstance("AES/CBC/PKCS5Padding");
			mCipher.init(Cipher.ENCRYPT_MODE, secretKey, mAlgo);
			bText = mCipher.doFinal(content.getBytes());
			String str = bytes2Hex(bText);
			return URLEncoder.encode(str,"utf-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args)
	{
		System.out.println( DigestUtils.sha512Hex("aaa"));
		System.out.println( DigestUtils.sha256Hex("aaa"));
		System.out.println("d6f644b19812e97b5d871658d6d3400ecd4787faeb9b8990c1e7608288664be77257104a58d033bcf1a0e0945ff06468ebe53e2dff36e248424c7273117dac09".length());
//		ChannelVerify.verify("kingnet", "liangjun1@kongzhong.com", "", "liangjun1", "kingnet", true);
//		ChannelVerify.verify("kingnet", "12354353458", "", "", "facebook", true);
//		ChannelVerify.verify("kingnet", "liangjun1@kongzhong.com", "", "liangjun1", "register", true);
//		ChannelVerify.verifyNew("qh", "", "200626241", "172747981619ebbba9b153d0c366999aabeea237cd40bc801", "", true);
//		ChannelVerify.verify("igs", "", "", "{\"ExpireTime\":\"2013-10-01T11:41:36.1279002+08:00\",\"ComeForm\":9,\"AccountID\":20964,\"UserID\":10008262,\"SocialID\":\"100000346615427\"},9/5Pr4uhqXhqSJiaztOW1w3F/BQ=", "", true);
	}
}
