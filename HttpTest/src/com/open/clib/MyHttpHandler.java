package com.open.clib;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.entity.ByteArrayEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.open.lib.MyLog;
import com.open.lib.ResponseHandler;
import com.open.welinks.model.MyFile;
import com.open.welinks.model.MyFile.Part;
import com.open.welinks.utils.Base64;
import com.open.welinks.utils.StreamParser;

public class MyHttpHandler {

	public String tag = "MyHttpHandler";
	public MyLog log = new MyLog(tag, true);

	public String ip = "192.168.1.11";
	public int port = 8010;
	public String host = "115.28.250.5";

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
					// InetAddress address =
					// InetAddress.getByName("images2.we-links.com");
					// String host = address.getHostAddress();
					// instance.ip = host;
					// instance.host = host;
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
	}

	public Status status = new Status();
	public InitiateMultipartUploadResult initiateMultipartUploadResult;

	public class Status {
		public int None = 0, InitUpload = 1, Uploading = 2, UploadComplete = 3, UploadFailed = 4;
		public int state = None;
	}

	public void testUpdateStatus() {

		int[] ids = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
		MyHttpJNI myHttpJNI = MyHttpJNI.getInstance();
		float[] percent = myHttpJNI.updateStates(ids);
		for (int t = 0; t < percent.length; t++) {
			log.e(t + " : " + percent[t]);
		}
	}

	public void test() {
		try {
			File file = new File("/storage/sdcard1/welinks/test11.jpg");
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

	public void testOpenLongPull() {
		MyHttp myHttp = new MyHttp();
		String IP = "112.126.71.180";
		int port = 80;
		String method = "GET";
		String url = "/api2/session/event";
		myHttp.putUrlParam("phone", "15210721344");
		myHttp.putUrlParam("accessKey", "lejoying");
		myHttp.putHeaderParam("Content-Length", "0");
		myHttp.putHeaderParam("Host", "www.we-links.com");

		TestOpenLongPull longPull = new TestOpenLongPull();

		myHttp.send(3, IP, port, method, url, longPull);
	}

	class TestOpenLongPull extends MyResponseHandler {

		@Override
		public void onSuccess(String data, int param) {
			log.e(data);
		}

		@Override
		public void onFailure(int error, String message) {
			log.e(message);
		}
	}

	public void testOpenSend() {
		MyHttpJNI myHttpJNI = MyHttpJNI.getInstance();
		String ip = "112.126.71.180";
		String head = "GET /aa.html HTTP/1.1\r\nHost: 112.126.71.180\r\nConnection: keep-alive\r\nContent-Length: " + 0 + "\r\n\r\n";
		myHttpJNI.openSend(ip.getBytes(), 80, head.getBytes(), myHttpJNI.globalID);
	}

	public void testDownload() {
		final MyHttpJNI myHttpJNI = MyHttpJNI.getInstance();
		String head = "GET /index.html HTTP/1.1\r\nHost: 192.168.1.7\r\nConnection: keep-alive\r\nContent-Length: " + 0 + "\r\n\r\n";
		byte[] data = head.getBytes();
		String ip = "192.168.1.7";
		// /storage/sdcard0/welinks/index.html
		String path = "/storage/sdcard0/welinks/test.jpg";
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					float percent = myHttpJNI.updateState(1001);
					if (percent != 1.0) {
						log.e("percent>>>>>>>>>:" + percent);
					} else {
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		byte[] ips = ip.getBytes();
		byte[] paths = path.getBytes();

		TestCallBack testCallBack = new TestCallBack();
		MyHttp myHttp = new MyHttp();
		myHttp.responseHandler = testCallBack;
		myHttpJNI.myHttpPool.put(1001, myHttp);

		myHttpJNI.openDownload(ips, 80, data, paths, 1001);
	}

	class TestCallBack extends MyResponseHandler {

		@Override
		public void onSuccess(String data, int param) {
			log.e("MyResponseHandler data:" + data);
		}
	}

	public void testOpenUpload() {
		MyFile myFile = new MyFile();

		myFile.Oss_Directory = "";
		myFile.fileName = "test11.jpg";

		initiateUpLoad(myFile);
	}

	public class TimeLine {

		public long start = 0; // 0
		public long startConnect = 0; // 1

		public long startSend = 0; // 2
		public long sent = 0; // 3

		public long startReceive = 0; // 4
		public long received = 0; // 5
	}

	public TimeLine time = new TimeLine();

	public void initiateUpLoad(MyFile myFile) {
		long expires = (new Date().getTime() / 1000) + addExpires;
		String postContent = "POST\n\n\n" + expires + "\n/" + BUCKETNAME + "/" + myFile.Oss_Directory + myFile.fileName + "?uploads";
		String signature = "";
		try {
			signature = getHmacSha1Signature(postContent, ACCESSKEYSECRET);
		} catch (Exception e) {
			e.printStackTrace();
			StackTraceElement ste = new Throwable().getStackTrace()[1];
			log.e("Exception@initiateUpLoad" + ste.getLineNumber());
		}

		RequestParams params = new RequestParams();
		HttpUtils httpUtils = new HttpUtils();

		String url = OSS_HOST_URL + myFile.Oss_Directory + myFile.fileName + "?uploads";

		// if (contentType != null) {
		// params.addHeader("Content-Type", contentType);
		// }

		params.addQueryStringParameter("OSSAccessKeyId", OSSACCESSKEYID);
		params.addQueryStringParameter("Expires", expires + "");
		params.addQueryStringParameter("Signature", signature);

		InitUpload initUpload = new InitUpload();
		initUpload.myFile = myFile;
		httpUtils.send(HttpMethod.POST, url, params, initUpload);
	}

	public class InitUpload extends ResponseHandler<String> {

		MyFile myFile;

		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {
			try {
				log.e(responseInfo.result);
				parseXml(responseInfo.result, myFile);
				startUpload(myFile);
			} catch (Exception e) {
				e.printStackTrace();
				StackTraceElement ste = new Throwable().getStackTrace()[1];
				log.e("Exception@InitUpload" + ste.getLineNumber());
			}
		};

		@Override
		public void onFailure(com.lidroid.xutils.exception.HttpException error, String msg) {
			myFile.status.state = myFile.status.Exception;
			StackTraceElement ste = new Throwable().getStackTrace()[1];
			log.e("Exception@InitUpload" + ste.getLineNumber());
		};
	};

	public void parseXml(String resultXml, MyFile myFile) throws Exception {
		InputStream is = new ByteArrayInputStream(resultXml.getBytes("UTF-8"));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(is);
		Element rootElement = doc.getDocumentElement();
		Node item = rootElement;
		NodeList properties = item.getChildNodes();
		for (int j = 0; j < properties.getLength(); j++) {
			Node property = properties.item(j);
			String nodeName = property.getNodeName();
			if (nodeName.equals("Bucket")) {
				myFile.bucket = property.getFirstChild().getNodeValue();
			} else if (nodeName.equals("Key")) {
				myFile.key = property.getFirstChild().getNodeValue();
			} else if (nodeName.equals("UploadId")) {
				myFile.uploadId = property.getFirstChild().getNodeValue();
			}
		}
	}

	int partSuccessCount = 0;
	int partCount = 0;
	String fileName = "test11.jpg";

	public void startUpload(MyFile myFile) {
		myFile.uploadPath = "/storage/sdcard1/welinks/test11.jpg";
		File file = new File(myFile.uploadPath);
		log.e("File::::" + file.exists());
		long fileLength = file.length();
		myFile.length = fileLength;
		myFile.partSuccessCount = 0;
		partCount = (int) Math.ceil((double) fileLength / (double) PartSize);
		myFile.partCount = partCount;
		log.e("partCount:" + partCount + ",   fileLength:" + fileLength);

		for (int i = 0; i < partCount; i++) {
			int partID = i + 1;
			uploadPart(myFile, partID);
		}
	}

	public int PartSize = 262144;

	public void uploadPart(MyFile myFile, int partID) {
		long expires = (new Date().getTime() / 1000) + addExpires;

		MyHttp myHttp = new MyHttp();
		myHttp.type = 1;
		myHttp.port = 80;
		myHttp.IP = host;

		String postContent = "PUT\n\n\n" + expires + "\n/" + BUCKETNAME + "/" + myFile.Oss_Directory + myFile.fileName + "?partNumber=" + partID + "&uploadId=" + myFile.uploadId;
		String signature = "";
		try {
			signature = getHmacSha1Signature(postContent, ACCESSKEYSECRET);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// RequestParams params = new RequestParams();
		// HttpUtils httpUtils = new HttpUtils();

		// String url = OSS_HOST_URL + myFile.Oss_Directory + myFile.fileName +
		// "?partNumber=" + partID + "&uploadId=" + myFile.uploadId;
		myHttp.method = "PUT";
		myHttp.url = OSS_HOST_URL + myFile.Oss_Directory + myFile.fileName;

		myHttp.putUrlParam("partNumber", partID + "");
		myHttp.putUrlParam("uploadId", myFile.uploadId + "");

		myHttp.putHeaderParam("OSSAccessKeyId", OSSACCESSKEYID);
		myHttp.putHeaderParam("Expires", expires + "");
		myHttp.putHeaderParam("Signature", signature);
		myHttp.putHeaderParam("Host", "images2.we-links.com");

		myHttp.url = OSS_HOST_URL + myFile.Oss_Directory + myFile.fileName;

		myHttp.myFile = myFile;

		if (myFile.length > 0) {

			myHttp.length = PartSize;

			myHttp.start = (partID - 1) * PartSize;
			if (partID * PartSize > myFile.length) {
				myHttp.length = (int) (myFile.length - myHttp.start);
			}

			myHttp.headerParams.put("Content-Length", myHttp.length + "");

			Part part = myFile.new Part(partID);
			part.status = part.PART_INIT;
			if (!myFile.parts.contains(part)) {
				myFile.parts.add(part);
			} else {
				part = myFile.parts.get(partID - 1);
			}
			UploadResponseHandler uploadResponseHandler = new UploadResponseHandler();
			uploadResponseHandler.partID = partID;
			uploadResponseHandler.myFile = myFile;
			uploadResponseHandler.part = part;
			myHttp.responseHandler = uploadResponseHandler;
			myHttp.send();
			// httpUtils.send(HttpMethod.PUT, url, params,
			// uploadResponseHandler);
		} else {
			StackTraceElement ste = new Throwable().getStackTrace()[1];
			log.e("Exception@uploadPart" + ste.getLineNumber());
		}
	}

	class UploadResponseHandler extends MyResponseHandler {

		public int partID = 0;
		public MyFile myFile;

		public Part part;

		@Override
		public void onStart() {
			if (partID == 1) {
				// time.start = System.currentTimeMillis();
			}
			// part.status = part.PART_LOADING;
		};

		@Override
		public void onLoading(long total, long current, boolean isUploading) {
			super.onLoading(total, current, isUploading);
		}

		@Override
		public void onSuccess(String data, int partId) {
			log.e("partId:" + partID + ",   data:" + data);
			data = data.replace("[", "{");
			data = data.replace("]", "}");
			// log.e(data);
			Gson gson = new Gson();
			HashMap<String, String> responseInfo = gson.fromJson(data, new TypeToken<HashMap<String, String>>() {
			}.getType());
			String statusCode = responseInfo.get("StatusCode");
			if (!"200".equals(statusCode)) {
				log.e("result:" + responseInfo.get("result"));
				return;
			}
			log.e(responseInfo.get("ETag") + "-____----------------------------------");
			// Part part = myFile.new Part();
			// part.partNumber = partID;
			// String eTag = data;
			// eTag = eTag.substring(3);
			// eTag = eTag.substring(0, eTag.length() - 2);
			part.eTag = responseInfo.get("ETag");
			// myFile.parts.add(part);

			myFile.partSuccessCount++;
			part.status = part.PART_SUCCESS;
			log.e("SuccessCount:" + myFile.partSuccessCount);
			if (myFile.partSuccessCount == myFile.partCount) {
				completeFile(myFile);
			}
		};

		@Override
		public void onFailure(int error, String message) {
			part.status = part.PART_FAILED;
			uploadPart(myFile, partID);
			StackTraceElement ste = new Throwable().getStackTrace()[1];
			log.e("Exception@UploadResponseHandler" + ste.getLineNumber());
		};
	};

	public void completeFile(MyFile myFile) {
		long expires = (new Date().getTime() / 1000) + addExpires;

		String postContent = "POST\n\n\n" + expires + "\n/" + BUCKETNAME + "/" + myFile.Oss_Directory + myFile.fileName + "?uploadId=" + myFile.uploadId;
		String signature = "";
		try {
			signature = getHmacSha1Signature(postContent, ACCESSKEYSECRET);
		} catch (Exception e) {
			e.printStackTrace();
			StackTraceElement ste = new Throwable().getStackTrace()[1];
			log.e("Exception@completeFile" + ste.getLineNumber());
		}

		RequestParams params = new RequestParams();
		HttpUtils httpUtils = new HttpUtils();

		String url = OSS_HOST_URL + myFile.Oss_Directory + myFile.fileName + "?uploadId=" + myFile.uploadId;

		params.addQueryStringParameter("OSSAccessKeyId", OSSACCESSKEYID);
		params.addQueryStringParameter("Expires", expires + "");
		params.addQueryStringParameter("Signature", signature);
		String xml = writeXml(myFile.parts);
		log.e(xml);
		params.setBodyEntity(new ByteArrayEntity(xml.getBytes()));
		CompleteUpload completeUpload = new CompleteUpload();
		completeUpload.myFile = myFile;
		httpUtils.send(HttpMethod.POST, url, params, completeUpload);
	}

	public class CompleteUpload extends ResponseHandler<String> {

		MyFile myFile;

		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {
			log.e("done!!!!");
			// uploadLoadingListener.onSuccess(instance, (int) (time.received -
			// time.start));

			// log.e(completeMultipartUploadResult.location + "---" +
			// completeMultipartUploadResult.bucket + "---" +
			// completeMultipartUploadResult.key + "---" +
			// completeMultipartUploadResult.eTag);
		};

		@Override
		public void onFailure(com.lidroid.xutils.exception.HttpException error, String msg) {
			myFile.status.state = myFile.status.Exception;
			StackTraceElement ste = new Throwable().getStackTrace()[1];
			log.e("Exception@CompleteUpload" + ste.getLineNumber());
			log.e("Exception@CompleteUpload" + msg);
		};
	};

	public void send(String method, String url, MyRequestParams params, int partId) {
		byte[] data = splicingRequestHeader(method, url, params);
		if (data == null) {
			log.e("----------------------------------");
		}
		MyHttpJNI myHttpJNI = MyHttpJNI.getInstance();
		log.e("bytes#######################:" + data.length);
		myHttpJNI.normalRequest(myHttpJNI, ip.getBytes(), port, data, partId);
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
			String data = "";
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
			if (params.bytes != null) {
				length += params.bytes.length;
			}
			head += (method + " " + url + " HTTP/1.1\r\nHost: " + host + "\r\nConnection: keep-alive\r\n" + "Content-Length: " + length + "\r\n\r\n");
			head += data;// \r\nContent-Type: application/x-www-form-urlencoded
			bytes = head.getBytes();
			if (params.bytes != null) {
				bytes = byteMerger(bytes, params.bytes);
			}
		} else if (method.equals(MyHttpMethod.PUT)) {
			String header = head;
			for (int i = 0; i < params.keys.size(); i++) {
				String key = params.keys.get(i);
				String value = params.keysMap.get(key);
				header += (key + ": " + value + "\r\n");
			}
			int length = params.bytes.length;
			head += (method + " " + url + " HTTP/1.1\r\n" + header + "Host: " + host + "\r\nUser-Agent: Mozilla/5.0 (Linux; U; Android 4.4.2; zh-cn; NX507J Build/KVT49L) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1\r\nAccept-Encoding: gzip\r\nConnection: keep-alive\r\n" + "Content-Length: " + length + "\r\n\r\n");
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
			for (int i = 0; i < partCount; i++) {
				Part part = parts.get(i);
				if (part == null) {
					break;
				}
				if (part.status != part.PART_SUCCESS) {
					break;
				}
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
