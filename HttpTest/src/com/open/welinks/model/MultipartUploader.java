package com.open.welinks.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.lidroid.xutils.http.client.entity.InputStreamUploadEntity;
import com.open.lib.MyLog;
import com.open.lib.ResponseHandler;
import com.open.welinks.model.MyFile.Part;
import com.open.welinks.utils.Base64;

public class MultipartUploader {

	public static String tag = "MultipartUploader";
	
	public MyLog log = new MyLog(tag, true);

	public static MultipartUploader instance;

	public static MultipartUploader getInstance() {
		if (instance == null) {
			instance = new MultipartUploader();
		}
		return instance;
	}

	public FileHandler fileHandler;
	public Gson gson = new Gson();

	void initialize() {
		fileHandler = FileHandler.getInstance();
	}

	public void checkFileExists(MyFile myFile) {
		RequestParams params = new RequestParams();
		HttpUtils httpUtils = new HttpUtils();
		params.addBodyParameter("phone", "152");
		params.addBodyParameter("accessKey", "lejoying");
		params.addBodyParameter("fileName", myFile.fileName);

		CheckFileExists checkFileExists = new CheckFileExists();
		checkFileExists.myFile = myFile;
		httpUtils.send(HttpMethod.POST, API.IMAGE_CHECKFILEEXIST, params, checkFileExists);
	}

	public class CheckFileExists extends ResponseHandler<String> {
		public MyFile myFile;

		class Response {
			public String 提示信息;
			public String 失败原因;
			public String fileName;
			public boolean exists;
		}

		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {
			Response response = gson.fromJson(responseInfo.result, Response.class);
			if (response.提示信息.equals("查找成功")) {
				myFile.isExists = response.exists;
				fileHandler.onCheckFile(myFile);
			} else {
				myFile.status.state = myFile.status.Failed;
			}
		};

		@Override
		public void onFailure(com.lidroid.xutils.exception.HttpException error, String msg) {
			myFile.status.state = myFile.status.Exception;
		};
	};

	public int addExpires = 600;
	public String BUCKETNAME = "wxgs";// welinkstest
	public String OSSACCESSKEYID = "dpZe5yUof6KSJ8RM";
	public String ACCESSKEYSECRET = "UOUAYzQUyvjUezdhZDAmX1aK6VZ5aG";
	public String OSS_HOST_URL = "http://images2.we-links.com/";// http://images5.we-links.com/

	public void initiateUpLoad(MyFile myFile) {
		long expires = (new Date().getTime() / 1000) + addExpires;
		String postContent = "POST\n\n\n" + expires + "\n/" + BUCKETNAME + "/" + myFile.fileName + "?uploads";
		String signature = "";
		try {
			signature = getHmacSha1Signature(postContent, ACCESSKEYSECRET);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		RequestParams params = new RequestParams();
		HttpUtils httpUtils = new HttpUtils();

		String url = OSS_HOST_URL + myFile.fileName + "?uploads";

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
				parseXml(responseInfo.result, myFile);
				fileHandler.onInitiateUpLoad(myFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		@Override
		public void onFailure(com.lidroid.xutils.exception.HttpException error, String msg) {
			myFile.status.state = myFile.status.Exception;
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

	public static String getHmacSha1Signature(String value, String key) throws NoSuchAlgorithmException, InvalidKeyException {
		byte[] keyBytes = key.getBytes();
		SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(signingKey);

		byte[] rawHmac = mac.doFinal(value.getBytes());
		return new String(Base64.encode(rawHmac));
	}

	public int PartSize = 256000;

	public void uploadParts(MyFile myFile) {
		myFile.partCount = (int) Math.ceil((double) myFile.length / (double) PartSize);

		for (int i = 0; i < myFile.partCount; i++) {
			int partID = i + 1;
			uploadPart(myFile, partID);
		}
	}

	public void uploadPart(MyFile myFile, int partID) {
		long expires = (new Date().getTime() / 1000) + addExpires;

		String postContent = "PUT\n\n\n" + expires + "\n/" + BUCKETNAME + "/" + myFile.fileName + "?partNumber=" + partID + "&uploadId=" + myFile.uploadId;
		String signature = "";
		try {
			signature = getHmacSha1Signature(postContent, ACCESSKEYSECRET);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		HttpUtils httpUtils = new HttpUtils();

		String url = OSS_HOST_URL + myFile.fileName + "?partNumber=" + partID + "&uploadId=" + myFile.uploadId;

		params.addQueryStringParameter("OSSAccessKeyId", OSSACCESSKEYID);
		params.addQueryStringParameter("Expires", expires + "");
		params.addQueryStringParameter("Signature", signature);

		if ((myFile.bytes != null) && (myFile.length > 0)) {

			int length = PartSize;

			int start = (partID - 1) * PartSize;
			if (partID * PartSize > myFile.length) {
				length = (int) (myFile.length - start);
			}

			byte[] byte0 = new byte[length];

			for (int j = 0; j < length; j++) {
				byte0[j] = myFile.bytes[start + j];
			}// TO DO
			MessageDigest digest = null;
			try {
				digest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			digest.update(byte0, 0, byte0.length);
			BigInteger bigInt = new BigInteger(1, digest.digest());
			String eTag = bigInt.toString(16).toUpperCase(Locale.getDefault());
			if (eTag.length() < 32) {
				for (int h = 0; h < 32 - eTag.length(); h++) {
					eTag = "0" + eTag;
				}
			}
			Part part = myFile.new Part(partID, eTag);
			part.status = part.PART_INIT;
			if (!myFile.parts.contains(part)) {
				myFile.parts.add(part);
			}
			params.setBodyEntity(new InputStreamUploadEntity(new ByteArrayInputStream(byte0), byte0.length));
			params.setBodyEntity(new ByteArrayEntity(byte0));
			UploadResponseHandler uploadResponseHandler = new UploadResponseHandler();
			uploadResponseHandler.partID = partID;
			uploadResponseHandler.myFile = myFile;
			uploadResponseHandler.part = part;
			httpUtils.send(HttpMethod.PUT, url, params, uploadResponseHandler);
		}
	}

	class UploadResponseHandler extends ResponseHandler<String> {

		public int partID = 0;
		public MyFile myFile;
		public Part part;

		@Override
		public void onStart() {
			if (partID == 1) {
				// time.start = System.currentTimeMillis();
			}
			part.status = part.PART_LOADING;
		};

		@Override
		public void onLoading(long total, long current, boolean isUploading) {
			super.onLoading(total, current, isUploading);
		}

		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {
			myFile.partSuccessCount++;
			part.status = part.PART_SUCCESS;
			// uploadPrecent = (int) ((((double) partSuccessCount / (double) partCount)) * 100);
			// uploadLoadingListener.onLoading(instance, uploadPrecent, (time.received - time.start), UPLOAD_SUCCESS);
			// log.e(partSuccessCount + "-----" + partCount + "------" +
			// part.eTag);
			if (myFile.partSuccessCount == myFile.partCount) {
				fileHandler.onUpLoadFile(myFile);
			}
		};

		@Override
		public void onFailure(HttpException error, String msg) {
			// log.e( error + "-----************---" + msg);
			// instance.isUploadStatus = UPLOAD_FAILED;
			part.status = part.PART_FAILED;
			uploadPart(myFile, partID);
			// uploadLoadingListener.loading(
			// instance.partSuccessCount / partCount, UPLOAD_FAILED);
		};
	};

	public void completeFile(MyFile myFile) {
		long expires = (new Date().getTime() / 1000) + addExpires;

		String postContent = "POST\n\n\n" + expires + "\n/" + BUCKETNAME + "/" + myFile.fileName + "?uploadId=" + myFile.uploadId;
		String signature = "";
		try {
			signature = getHmacSha1Signature(postContent, ACCESSKEYSECRET);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		RequestParams params = new RequestParams();
		HttpUtils httpUtils = new HttpUtils();

		String url = OSS_HOST_URL + myFile.fileName + "?uploadId=" + myFile.uploadId;

		params.addQueryStringParameter("OSSAccessKeyId", OSSACCESSKEYID);
		params.addQueryStringParameter("Expires", expires + "");
		params.addQueryStringParameter("Signature", signature);
		params.setBodyEntity(new ByteArrayEntity(writeXml(myFile.parts).getBytes()));
		CompleteUpload completeUpload = new CompleteUpload();
		completeUpload.myFile = myFile;
		httpUtils.send(HttpMethod.POST, url, params, completeUpload);
	}

	public class CompleteUpload extends ResponseHandler<String> {
		MyFile myFile;

		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {
			fileHandler.onCompleteFile(myFile);
			// uploadLoadingListener.onSuccess(instance, (int) (time.received - time.start));
	
			// log.e(completeMultipartUploadResult.location + "---" +
			// completeMultipartUploadResult.bucket + "---" +
			// completeMultipartUploadResult.key + "---" +
			// completeMultipartUploadResult.eTag);
		};

		@Override
		public void onFailure(com.lidroid.xutils.exception.HttpException error, String msg) {
			myFile.status.state = myFile.status.Exception;
		};
	};

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
	
	
	public void recordFileName(MyFile myFile) {
		RequestParams params = new RequestParams();
		HttpUtils httpUtils = new HttpUtils();
		params.addBodyParameter("phone", "152");
		params.addBodyParameter("accessKey", "lejoying");
		params.addBodyParameter("fileName", myFile.fileName);

		UploadFileName uploadFileName=new UploadFileName();
		httpUtils.send(HttpMethod.POST, API.IMAGE_UPLOADFILENAME, params, uploadFileName);
	}

	public class UploadFileName extends ResponseHandler<String> {

		class Response {
			public String 提示信息;
			public String 失败原因;
		}

		@Override
		public void onSuccess(ResponseInfo<String> responseInfo) {
			Response response = gson.fromJson(responseInfo.result, Response.class);
			if (response.提示信息.equals("上传成功")) {
				log.e("上传文件名到服务器成功");
			} else {
				log.e("上传文件名到服务器失败**********************" + response.失败原因);
			}
		};

		@Override
		public void onFailure(com.lidroid.xutils.exception.HttpException error, String msg) {
			log.e("上传文件名到服务器失败**********************" + "请求错误");
		};
	};

}
