package com.open.clib;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import com.open.lib.MyLog;
import com.open.welinks.utils.Base64;
import com.open.welinks.utils.StreamParser;

public class MyHttpHandler {

	public String tag = "HttpHandler";
	public MyLog log = new MyLog(tag, true);

	public String ip = "";
	public int port = 80;
	public String host = "";

	public String BUCKETNAME = "wxgs";// welinkstest
	public String OSSACCESSKEYID = "dpZe5yUof6KSJ8RM";
	public String ACCESSKEYSECRET = "UOUAYzQUyvjUezdhZDAmX1aK6VZ5aG";

	public String OSS_HOST_URL = "http://images2.we-links.com/";// http://images5.we-links.com/
	public String OSS_DIRECTORY = "temp/";// multipart

	public int addExpires = 600;

	public static MyHttpHandler instance;

	public static MyHttpHandler getInstance() {
		if (instance == null) {
			instance = new MyHttpHandler();
			instance.initialize();
		}
		return instance;
	}

	private void initialize() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					InetAddress address = InetAddress.getByName("images2.we-links.com");
					String host = address.getHostAddress();
					instance.ip = host;
					instance.host = host;
				} catch (Exception e) {
					e.printStackTrace();
					log.e(e.toString());
				}
			}
		}).start();
	}

	static class MyHttpMethod {
		public static String GET = "GET";
		public static String POST = "POST";
		public static String PUT = "PUT";
		public static String GETPUT = "GETPUT";
	}

	public Status status = new Status();
	public InitiateMultipartUploadResult initiateMultipartUploadResult;

	public class Status {
		public int None = 0, InitUpload = 1, Uploading = 2, UploadComplete = 3, UploadFailed = 4;
		public int state = None;
	}

	public List<Part> parts = new ArrayList<Part>();

	public class Part {
		public int partNumber;
		public String eTag;
	}

	public void test() {
		try {
			File file = new File("/storage/sdcard0/welinks/test0023.png");
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] bytes = StreamParser.parseToByteArray(fileInputStream);
			String head = "PUT /api2/bug/send? HTTP/1.1\r\nHost: 192.168.1.11\r\nConnection: keep-alive\r\nContent-Length: " + bytes.length + "\r\n\r\n";
			byte[] data = byteMerger(head.getBytes(), bytes);
			MyHttpJNI myHttpJNI = MyHttpJNI.getInstance();
			myHttpJNI.test(data, myHttpJNI);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void initUpload() {
		status.state = status.None;
		parts.clear();
		try {
			MyRequestParams params = new MyRequestParams();

			long expires = (new Date().getTime() / 1000) + addExpires;
			String postContent = "POST\n\n\n" + expires + "\n/" + BUCKETNAME + "/" + fileName + "?uploads";
			String signature = "";
			signature = MyHttpHandler.getHmacSha1Signature(postContent, ACCESSKEYSECRET);
			String url = "http://" + host + "/" + BUCKETNAME + "/" + fileName + "?uploads";

			params.putParameter("OSSAccessKeyId", OSSACCESSKEYID);
			params.putParameter("Expires", expires + "");
			params.putParameter("Signature", signature);

			send(MyHttpMethod.POST, url, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int partSuccessCount = 0;
	int partCount = 0;
	String fileName = "test0023.png";

	public void startUpload() {
		try {
			File file = new File("/storage/sdcard0/welinks/test0023.png");
			FileInputStream fileInputStream = new FileInputStream(file);
			byte[] bytes = StreamParser.parseToByteArray(fileInputStream);
			partSuccessCount = 0;
			partCount = (int) Math.ceil((double) bytes.length / (double) 256000);

			// log.e("partCount:" + partCount);

			for (int i = 0; i < 1; i++) {
				int partID = i + 1;
				upload(partID, bytes);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void upload(int partID, byte[] bytes) {
		int length = 256000;

		int start = (partID - 1) * length;
		if (partID * length > bytes.length) {
			length = bytes.length - start;
		}

		byte[] buffer = new byte[length];

		for (int j = 0; j < length; j++) {
			buffer[j] = bytes[start + j];
		}
		log.e("length---------------:" + buffer.length);
		log.e("upload.....");
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		digest.update(buffer, 0, buffer.length);
		BigInteger bigInt = new BigInteger(1, digest.digest());
		String eTag = bigInt.toString(16).toUpperCase(Locale.getDefault());
		if (eTag.length() < 32) {
			for (int h = 0; h < 32 - eTag.length(); h++) {
				eTag = "0" + eTag;
			}
		}
		log.e("*************************************：" + eTag);
		try {
			long expires = (new Date().getTime() / 1000) + addExpires;
			String postContent = "PUT\n\n\n" + expires + "\n/" + BUCKETNAME + "/" + fileName + "?partNumber=" + 1 + "&uploadId=" + initiateMultipartUploadResult.uploadId;
			String signature = "";
			try {
				signature = MyHttpHandler.getHmacSha1Signature(postContent, ACCESSKEYSECRET);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			String url = "http://" + host + "/" + BUCKETNAME + "/" + fileName + "?partNumber=" + 1 + "&uploadId=" + initiateMultipartUploadResult.uploadId;

			MyRequestParams params = new MyRequestParams();
			params.putParameter("OSSAccessKeyId", OSSACCESSKEYID);
			params.putParameter("Expires", expires + "");
			params.putParameter("Signature", signature);
			params.putBodyEntity(buffer);
			send(MyHttpMethod.PUT, url, params);
		} catch (Exception e) {
			e.printStackTrace();
			log.e(e.toString());
		}
	}

	public void addPart(int partId, String eTag) {
		Part part = new Part();
		part.partNumber = partId;
		part.eTag = eTag;
		parts.add(part);
		partSuccessCount++;
		if (partSuccessCount == partCount) {
			status.state = status.Uploading;
			uploadCompelte();
		}
	}

	public void uploadCompelte() {
		try {
			long expires = (new Date().getTime() / 1000) + addExpires;

			String postContent = "POST\n\n\n" + expires + "\n/" + BUCKETNAME + "/" + fileName + "?uploadId=" + initiateMultipartUploadResult.uploadId;
			String signature = "";
			try {
				signature = getHmacSha1Signature(postContent, ACCESSKEYSECRET);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}

			MyRequestParams params = new MyRequestParams();

			String url = "http://" + host + "/" + BUCKETNAME + "/" + fileName + "?uploadId=" + initiateMultipartUploadResult.uploadId;

			params.putParameter("OSSAccessKeyId", OSSACCESSKEYID);
			params.putParameter("Expires", expires + "");
			params.putParameter("Signature", signature);
			params.putBodyEntity(writeXml(parts).getBytes());

			send(MyHttpMethod.GETPUT, url, params);
		} catch (Exception e) {
			e.printStackTrace();
			log.e(e.toString());
		}
	}

	public void send(String method, String url, MyRequestParams params) {
		byte[] data = splicingRequestHeader(method, url, params);
		if (data == null) {
			log.e("REDREDREDREDREDREDREDREDREDREDREDREDREDREDREDREDREDREDREDREDREDREDREDREDREDREDREDRED");
		}
		MyHttpJNI myHttpJNI = MyHttpJNI.getInstance();
		log.e("bytes#######################:" + data.length);
		myHttpJNI.normalRequest(myHttpJNI, ip.getBytes(), port, data, 1);
	}

	public byte[] splicingRequestHeader(String method, String url, MyRequestParams params) {
		byte[] bytes = null;
		String head = "";
		if (method.equals(MyHttpMethod.GET)) {
			String getUrl = head;
			for (int i = 0; i < params.keys.size(); i++) {
				String key = params.keys.get(i);
				String value = params.keysMap.get(key);
				if (i == 0) {
					getUrl += ("?" + key + "=" + value);
				} else {
					getUrl += ("&" + key + "=" + value);
				}
			}
			head += (method + " " + getUrl + " HTTP/1.1\r\nHost: " + host + "\r\nConnection: keep-alive\r\n" + "Content-Length: 0\r\n\r\n");
			bytes = head.getBytes();
		} else if (method.equals(MyHttpMethod.POST)) {
			String data = head;
			for (int i = 0; i < params.keys.size(); i++) {
				String key = params.keys.get(i);
				String value = params.keysMap.get(key);
				if (i == 0) {
					data += (key + "=" + value);
				} else {
					data += ("&" + key + "=" + value);
				}
			}
			int length = data.length();
			head += (method + " " + url + " HTTP/1.1\r\nHost: " + host + "\r\nConnection: keep-alive\r\n" + "Content-Length: " + length + "\r\n\r\n");
			head += data;// \r\nContent-Type: application/x-www-form-urlencoded
			bytes = head.getBytes();
		} else if (method.equals(MyHttpMethod.PUT)) {
			String header = head;
			for (int i = 0; i < params.keys.size(); i++) {
				String key = params.keys.get(i);
				String value = params.keysMap.get(key);
				header += (key + ": " + value + "\r\n");
			}
			int length = params.bytes.length;
			head += (method + " " + url + " HTTP/1.1\r\n" + header + "Host: " + host + "\r\nUser-Agent: Mozilla/5.0 (Linux; U; Android 4.4.2; zh-cn; NX507J Build/KVT49L) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1\r\nAccept-Encoding: gzip\r\nConnection: keep-alive\r\n" + "Content-Length: " + length + "\r\n\r\n");
			log.e("head length:::::::::::::::::::::::" + head.getBytes().length);
			bytes = byteMerger(head.getBytes(), params.bytes);
		} else if (method.equals(MyHttpMethod.GETPUT)) {
			String header = head;
			for (int i = 0; i < params.keys.size(); i++) {
				String key = params.keys.get(i);
				String value = params.keysMap.get(key);
				header += (key + ": " + value + "\r\n");
			}
			int length = params.bytes.length;
			head += ("POST" + " " + url + " HTTP/1.1\r\n" + header + "Host: " + host + "\r\nConnection: keep-alive\r\n" + "Content-Length: " + length + "\r\n\r\n");
			bytes = byteMerger(head.getBytes(), params.bytes);
		}
		return bytes;
	}

	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
		byte[] byte_3 = new byte[byte_1.length + byte_2.length];
		System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
		System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
		return byte_3;
	}

	class InitiateMultipartUploadResult {
		public String bucket;
		public String key;
		public String uploadId;
	}

	public String writeXml(List<Part> parts) {
		StringWriter stringWriter = new StringWriter();
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlSerializer xmlSerializer = factory.newSerializer();// Xml.newSerializer();
			xmlSerializer.setOutput(stringWriter);
			xmlSerializer.startDocument("utf-8", true);
			xmlSerializer.startTag(null, "CompleteMultipartUpload");
			for (Part part : parts) {
				xmlSerializer.startTag(null, "Part");
				xmlSerializer.startTag(null, "PartNumber");
				xmlSerializer.text(part.partNumber + "");
				xmlSerializer.endTag(null, "PartNumber");
				xmlSerializer.startTag(null, "ETag");
				xmlSerializer.text("\"" + part.eTag + "\"");
				xmlSerializer.endTag(null, "ETag");
				xmlSerializer.endTag(null, "Part");
			}
			xmlSerializer.endTag(null, "CompleteMultipartUpload");
			xmlSerializer.endDocument();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		return stringWriter.toString();
	}

	public InitiateMultipartUploadResult parseXml(String resultXml) throws Exception {
		InputStream is = new ByteArrayInputStream(resultXml.getBytes("UTF-8"));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);
		Element rootElement = doc.getDocumentElement();
		InitiateMultipartUploadResult initiateMultipartUploadResult = new InitiateMultipartUploadResult();
		Node item = rootElement;
		NodeList properties = item.getChildNodes();
		for (int j = 0; j < properties.getLength(); j++) {
			Node property = properties.item(j);
			String nodeName = property.getNodeName();
			if (nodeName.equals("Bucket")) {
				initiateMultipartUploadResult.bucket = property.getFirstChild().getNodeValue();
			} else if (nodeName.equals("Key")) {
				initiateMultipartUploadResult.key = property.getFirstChild().getNodeValue();
			} else if (nodeName.equals("UploadId")) {
				initiateMultipartUploadResult.uploadId = property.getFirstChild().getNodeValue();
			}
		}
		return initiateMultipartUploadResult;
	}

	public static String getHmacSha1Signature(String value, String key) throws NoSuchAlgorithmException, InvalidKeyException {
		byte[] keyBytes = key.getBytes();
		SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(signingKey);

		byte[] rawHmac = mac.doFinal(value.getBytes());
		return new String(Base64.encode(rawHmac));
	}
}
