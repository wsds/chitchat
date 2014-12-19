#include <jni.h>
#include <stdlib.h>
//#include <stdio.h>

//
#include <test.h>

#include <fcntl.h>
#include <asm-generic/fcntl.h>
#include "lib/Log.h"
#include "data_core/base/HashTable.h"
#include "data_core/base/Queue.h"
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "openHttp/OpenHttp.h"
#include "data_core/base/JSObject.h"
#include <sys/epoll.h>
#include <pthread.h>

#include <sys/stat.h> //文件状态结构#include <unistd.h>#include <asm-generic/mman-common.h>#include <errno.h>#include <sys/mman.h> //mmap头文件#define MAXBUFLEN 	1024#define MAXEVENTS 100void test(signed char * message);static void make_sendipv4addr(struct sockaddr_in *addr, int remoteport);int setSend(const char *ipAddr);int sendPackege(int sockd, const void * buffer, int PackegeSize, unsigned int mode);static void make_recvipv4addr(struct sockaddr_in *addr, const int localport);int recvPacket(int sockd);static unsigned short GetSocketPort(int sd);int setNonBlocking(int sock);void setEpoll(int sock);int sendData(int sockd, const char *buffer);void sendPackeges(int sockd, const char *buffer);void test2();void test3();void CallBack(int type,char * buffer,char * etag,int partId);void test5();void test6();void test7();void test8(JNIEnv *env, jobject myHttpJNI);void test9();void testPost();void test10(signed char * buffer);void CallTest(JNIEnv *env, jobject myHttpJNI);jmethodID GetClassMethodID(JNIEnv* env);void resolveLine(char * start, int length, int lineNumber, HashTable * headMap);HashTable * parseResponseHead(char * buffer, int length);void epollLooper(int epollFD);int epollFD = 0;int listeningSocketFD = 0;int connectingSocketFD = 0;int sendingSocketFD = 0;int PackegeSize = 1024;int dataLength = 0;int sentLength = 0;int packegesNum = 0;int lastPackegeSize = 0;int sentRuturnLength = 0;const char *dataBuffer;bool isSocketBufferFull = false;char target[15] = "";char * HttpMark = (char *) ("HTTP");char * ContentLengthMark = (char *) ("Content-Length");
char * HeadLengthMark = (char *) ("Head-Length");
char * ETagMark = (char *) ("ETag");
//

static JavaVM *g_jvm = NULL;
//static jobject s_jobj = NULL;
//static jmethodID s_jcallback = NULL;
//#define  LOG_TAG    "OpenHttp"

int JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	g_jvm = vm;
	Log((char*) "JVM");
	JNIEnv* env = NULL;
	jint result = -1;

	if (vm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK) {
		return -1;
	}
	if (env == NULL) {
		return -1;
	}

	result = JNI_VERSION_1_6;
	Log(result);
	return result;
}
extern "C" JNIEXPORT jint Java_com_open_clib_MyHttpJNI_nativeSend(JNIEnv *env, jobject obj, jbyteArray ip, jint port, jbyteArray url, jint method, jbyteArray header, jbyteArray body, jint start, jint length, jint id) {

	char * url_buffer;
	char * header_buffer;

	signed char * body_buffer = (signed char*) malloc(length * sizeof(char));
	env->GetByteArrayRegion(body, start, length, body_buffer);
	signed char * ip_buffer = (signed char*) malloc(16 * sizeof(char));
	env->GetByteArrayRegion(ip, 0, 15, ip_buffer);
	Log((char*) "hello");

//	OpenHttp * openHttp = OpenHttp::getInstance();
//	openHttp->initialize();
//	openHttp->openSend(ip_buffer, body_buffer);

	return (jint) 1;
}

extern "C" JNIEXPORT jint Java_com_open_clib_MyHttpJNI_test(JNIEnv *env, jobject obj, jbyteArray message, jobject myHttpJNI) {

//	env->GetJavaVM(&g_jvm);

//	int length = env->GetArrayLength(message);
//
//	signed char * body_buffer = (signed char*) malloc(length + 1 * sizeof(char));
//
//	env->GetByteArrayRegion(message, 0, length, body_buffer);
//	body_buffer[length] = 0;
//	Log((char*) body_buffer);

//	test10(body_buffer);
//	CallTest(env, myHttpJNI, message);
//	test(body_buffer);
//	test7();
//	jobject s_jobj = env->NewGlobalRef(myHttpJNI);
//	jmethodID s_jcallback = GetClassMethodID(env);
//	test8(env, myHttpJNI);
	test9();
//	testPost();

	return (jint) 1;
}

extern "C" JNIEXPORT jint Java_com_open_clib_MyHttpJNI_normalRequest(JNIEnv *env, jobject obj, jobject myHttpJNI, jbyteArray ip, jint port, jbyteArray body, jint id) {
//	Log("in..");
	env->GetJavaVM(&g_jvm);
	int length = env->GetArrayLength(body);
	signed char * body_buffer = (signed char*) malloc(length + 1);
	env->GetByteArrayRegion(body, 0, length, body_buffer);

	body_buffer[length + 1] = 0;
	Log((char *) body_buffer);

//	Log("body length:", length);
//	Log("body_buffer length:", strlen((char *) body_buffer));

	int iplength = env->GetArrayLength(ip);
	signed char * ip_buffer = (signed char*) malloc(iplength + 1 * sizeof(char));
	env->GetByteArrayRegion(ip, 0, iplength, ip_buffer);
	ip_buffer[iplength] = 0;
//	Log("iplength length:", iplength);
//	Log("iplength_buffer length:", strlen((char *) ip_buffer));

	_jobject * s_jobj = env->NewGlobalRef(myHttpJNI);
	_jmethodID * s_jcallback = GetClassMethodID(env);

//	Log("in....");
//	OpenHttp * openHttp = OpenHttp::getInstance();
//	openHttp->initialize();
//
//	openHttp->openSend((char *) ip_buffer, port, (char *) body_buffer, length, id, s_jobj, s_jcallback);

	return (jint) 1;
}
extern "C" JNIEXPORT jint Java_com_open_clib_MyHttpJNI_openInitialize(JNIEnv *env, jobject obj, jobject myHttpJNI) {
	Log((char*) "openInitialize");
	OpenHttp * openHttp = OpenHttp::getInstance();
	openHttp->initialize();
	env->GetJavaVM(&openHttp->callback_jvm);
	openHttp->callback_object = env->NewGlobalRef(myHttpJNI);
	openHttp->callback_method = GetClassMethodID(env);
	return 1;
}

extern "C" JNIEXPORT jint Java_com_open_clib_MyHttpJNI_openDownload(JNIEnv *env, jobject obj, jbyteArray ip, jint port, jbyteArray body, jbyteArray path, jint id) {

	int body_length = env->GetArrayLength(body);
	signed char * body_buffer = (signed char*) malloc(body_length);
	env->GetByteArrayRegion(body, 0, body_length, body_buffer);
	body_buffer[body_length] = 0;

	int ip_length = env->GetArrayLength(ip);
	signed char * ip_buffer = (signed char*) malloc(ip_length + 1 * sizeof(char));
	env->GetByteArrayRegion(ip, 0, ip_length, ip_buffer);
	ip_buffer[ip_length] = 0;

	int path_length = env->GetArrayLength(path);
	signed char * path_buffer = (signed char*) malloc(path_length + 1 * sizeof(char));
	env->GetByteArrayRegion(path, 0, path_length, path_buffer);
	path_buffer[path_length] = 0;

	OpenHttp * openHttp = OpenHttp::getInstance();
	openHttp->initialize();
	openHttp->openDownload((char *) ip_buffer, port, (char *) body_buffer, (char *) path_buffer, (int) id, body_length);
//	openHttp->openSend((char *) ip_buffer, port, (char *) body_buffer, (char *) path_buffer, (int) id, body_length);

	return (jint) 1;
}
extern "C" JNIEXPORT jint Java_com_open_clib_MyHttpJNI_openUpload(JNIEnv *env, jobject obj, jbyteArray ip, jint port, jbyteArray head, jbyteArray path, jint start, jint length, jint id) {

	int head_length = env->GetArrayLength(head);
	signed char * head_buffer = (signed char*) JSMalloc(head_length);
	env->GetByteArrayRegion(head, 0, head_length, head_buffer);
	head_buffer[head_length] = 0;

	int ip_length = env->GetArrayLength(ip);
	signed char * ip_buffer = (signed char*) JSMalloc(ip_length + 1 * sizeof(char));
	env->GetByteArrayRegion(ip, 0, ip_length, ip_buffer);
	ip_buffer[ip_length] = 0;

	int path_length = env->GetArrayLength(path);
	signed char * path_buffer = (signed char*) JSMalloc(path_length + 1 * sizeof(char));
	env->GetByteArrayRegion(path, 0, path_length, path_buffer);
	path_buffer[path_length] = 0;

	OpenHttp * openHttp = OpenHttp::getInstance();
	openHttp->initialize();
	openHttp->openUpload((char *) ip_buffer, port, (char *) head_buffer, (char *) path_buffer, id, head_length, start, length);

	return (jint) 1;
}

extern "C" JNIEXPORT jfloat Java_com_open_clib_MyHttpJNI_updateStates(JNIEnv *env, jobject obj, jint id) {

	OpenHttp * openHttp = OpenHttp::getInstance();

	if (openHttp != NULL) {
		HttpEntity * httpEntity = openHttp->httpEntitiesIdMap->get(id);
		if (httpEntity != NULL) {
			return httpEntity->receive_percent;
		} else {
			return 0.01;
		}
	} else {
		return 0.01;
	}

	return (jint) 0.01;
}
void test10(signed char * buffer) {
	Log(strlen((char *) buffer));
	Log((char *) buffer);
}
//void testPost() {
//	int length = 20000;
//	char * title = (char *) ("POST /api2/bug/send? HTTP/1.1\r\nHost: 192.168.1.11\r\nConnection: keep-alive\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: 20000\r\n\r\n");
//	int len = strlen(title);
//	Log(len);
//	char * key = (char *) ("data=");
//	char * value = (char *) ("12345");
//	char * buffer = (char*) JSMalloc((length + len) * sizeof(char));
//	for (int i = 0; i < length + len; i++) {
//		if (i < len) {
//			*(buffer + i) = *(title + i);
//		} else if (i < len + 5) {
//			*(buffer + i) = *(key + i % len);
//		} else if (i < length + len) {
////			*(buffer + i) = i / 100 + 1;
//			*(buffer + i) = *(value + i % 5);
//		}
//	}
//	OpenHttp * openHttp = OpenHttp::getInstance();
//	openHttp->initialize();
////char * ip, int remotePort, char * head, char * body, char * path
//	openHttp->openUpload((char *) ("192.168.1.11"), 8090, title, buffer, (char *) "/storage/sdcard0/welinks/index.html");
//}
//void test(signed char * message) {
//	char * title = (char *) ("GET /index.html HTTP/1.1\r\nHost: 192.168.1.11\r\nConnection: keep-alive\r\nContent-Length: 0\r\n\r\n");
////	Log(strlen(title));
//	int len = strlen(title);
//	char * buffer = (char*) JSMalloc((3500 + len) * sizeof(char));
//	for (int i = 0; i < 3500 + len; i++) {
//		if (i < len) {
////			Log((char *) ("char"), i);
//			*(buffer + i) = *(title + i);
//		} else {
////			Log((char *) ("charsss"), i);
//			*(buffer + i) = i / 100 + 1;
//		}
//	}
//
//	OpenHttp * openHttp = OpenHttp::getInstance();
//	openHttp->initialize();
//	openHttp->openDownload((char *) ("192.168.1.11"), 80, title, (char *) "123456", (char *) ("/storage/sdcard0/welinks/index.html"));
//}

void test9() {
//读取本地文件
	char * path = (char *) ("/sdcard/welinks/test1.png");
	int sendFD = open(path, O_RDONLY, 777);
	if (sendFD < 0) {
		Log((char *) ("Download File,Can not open !"));
		return;
	}
	Log((char *) ("success."), sendFD);
	void *pointer = mmap(NULL, 1000, PROT_READ, MAP_SHARED, sendFD, 0);
	pointer + 100000;

	if ((pointer) == (void *) -1) {
		Log((char*) ("Failed"));
		return;
	}
	Log((char *) ("success..."), sendFD);
	char * buffer = (char *) pointer;
//	Log(buffer);
//	Log(buffer + 1024);
//	Log(buffer + 2048);
//	Log(strlen(buffer));
//	int len = 1023;
//	int size = strlen(buffer) / len;
//	if (strlen(buffer) % len != 0) {
//		size += 1;
//	}
//	for (int i = 0; i < size; i++) {
//		Log(buffer + i * len);
//		Log(strlen(buffer + i * len));
//	}
}
//void test8(JNIEnv *env1, jobject myHttpJNI1) {
//
////	CallTest(env1, myHttpJNI1);
////发送200000个字节，并调用Java的回调函数
//	int length = 1000; //20000
//	char * title = (char *) ("PUT /api2/bug/send? HTTP/1.1\r\nHost: 192.168.1.11\r\nConnection: keep-alive\r\nContent-Length: 1000\r\n\r\n");
//	int len = strlen(title);
//	Log(len);
//	char * buffer = (char*) JSMalloc((length + len) * sizeof(char));
//	for (int i = 0; i < length + len; i++) {
//		if (i < len) {
//			*(buffer + i) = *(title + i);
//		} else if (i < length + len) {
//			*(buffer + i) = i / 100 + 1;
//		}
//	}
//	OpenHttp * openHttp = OpenHttp::getInstance();
//	openHttp->initialize();
////char * ip, int remotePort, char * head, char * body, char * path
//	openHttp->openUpload((char *) ("192.168.1.11"), 8090, title, buffer, (char *) "/storage/sdcard0/welinks/index.html");
//}

//void *epollLooperThread1(void *arg) {
//	epollLooper(epollFD);
//}
void test2() {

//	char * buffer = (char*) malloc(1024 * 3 * sizeof(char));
//	for (int i = 1; i < 1024 * 3 - 2; i++) {
//		*(buffer + i) = i / 100 + 1;
//	}
//	*(buffer + 1024 * 3 - 1) = 0;

//	sendingSocketFD = setSend("192.168.1.7");
//	setEpoll(sendingSocketFD);

	pthread_t epollLooperPthread;
//	int ret = pthread_create(&epollLooperPthread, NULL, epollLooperThread1, (void *) 1);
//	sleep(1);
//	sendData(sendingSocketFD, buffer);

}

//void test3() {
//	signed char * buffer = (signed char *) malloc(1024 * 3 * sizeof(char));
//	for (int i = 1; i < 1024 * 3 - 2; i++) {
//		*(buffer + i) = i / 100 + 1;
//	}
//	*(buffer + 1024 * 3 - 1) = 0;
//
//	OpenHttp *openHttp = OpenHttp::getInstance();
//
//	openHttp->initialize();
//	openHttp->openSend((char *) ("192.168.1.7"), 8091, (char *) buffer, 1024 * 3, 0, NULL, NULL);
//}

void CallBack(int id, int type, const signed char * buffer, const signed char * etag, int param) {
	OpenHttp * openHttp = OpenHttp::getInstance();
	if (openHttp != NULL) {
		JNIEnv * env = NULL;
		if (openHttp->callback_jvm->AttachCurrentThread(&env, NULL) != JNI_OK) {
			Log((char *) "Failed");
		} else {
		}
		if (openHttp->callback_jvm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK) {
			return;
		}
		if (env == NULL) {
			return;
		}
		int buffer_length = strlen((char *) buffer);
		jbyteArray body = env->NewByteArray(buffer_length);
		env->SetByteArrayRegion(body, 0, buffer_length, buffer);

		int etag_length = strlen((char *) etag);
		jbyteArray bodyetag = env->NewByteArray(etag_length);
		env->SetByteArrayRegion(bodyetag, 0, etag_length, etag);

		env->CallVoidMethod(openHttp->callback_object, openHttp->callback_method, type, body, bodyetag, id, param);
		if (openHttp->callback_jvm->DetachCurrentThread() != JNI_OK) {
			Log((char *) "FFAILED");
		}
	}
}
_jmethodID * GetClassMethodID(JNIEnv* env) {
	jclass clazz = env->FindClass("com/open/clib/MyHttpJNI");
	if (clazz == NULL) {
		Log((char*) "[GetClassMethod()]Failed to find jclass");
		return NULL;
	}

	_jmethodID * jcallback = env->GetMethodID(clazz, "callback", "(I[B[BIF)V");
	if (jcallback == NULL) {
		Log((char*) "[GetClassMethod()]Failed to find method callback");
		return NULL;
	}

	return jcallback; //返回保存为s_jcallback
}
void CallTest(JNIEnv *env, jobject myHttpJNI) {
	const signed char * buffer = (const signed char *) ("abcdefghij");
	jclass TestProvider = env->FindClass("com/open/clib/MyHttpJNI");
	jmethodID construction_id = env->GetMethodID(TestProvider, "callback", "(I[BIF)V"); //(Ljava/lang/String;byte;Integer)V
	jbyteArray body = env->NewByteArray(1000);
	env->SetByteArrayRegion(body, 0, 11, buffer);
	env->CallVoidMethod(myHttpJNI, construction_id, 1, body, 100, 10.10);
}
void test5() {
	Queue * queue = new Queue();
	queue->initialize();
	for (int i = 0; i < 20; i++) {
		JSObject * jSObject = new JSObject();
		jSObject->number = i + 110;
//		Log((char *) ("queue offer, "), i);
		queue->offer(jSObject);
	}
	for (int i = 0; i < 20; i++) {
		JSObject * jSObject = queue->take();
		if (jSObject == NULL) {
			Log((char *) ("queue item, "), (char *) ("NULL"));
			continue;
		}
		Log((char *) ("queue item, "), jSObject->number);
	}
}
void test6() {
//	char * buffer = (char *) ("HTTP/1.1 200 OK\r\nContent-Length: 4095\r\nETag: 54800b5f-fff\r\nConnection: keep-alive\r\n\r\nabcdefghhjk");
//	Log(strlen(buffer));
//	HashTable * headMap = parseResponseHead(buffer, strlen(buffer));
//	JSObject * jsObject;
//	jsObject = headMap->get(HttpMark);
//	Log((char *) ("HttpMark: "), jsObject->number);
//
//	jsObject = headMap->get(ContentLengthMark);
//	if (jsObject == NULL) {
//		Log((char *) ("ContentLengthMark @@@: "), (char *) ("NULL"));
//	} else {
//		Log((char *) ("ContentLengthMark @@@: "), jsObject->number);
//	}
//	jsObject = headMap->get(HeadLengthMark);
//	if (jsObject == NULL) {
//		Log((char *) ("HeadLengthMark @@@: "), (char *) ("NULL"));
//	} else {
//		char * lineKey = (char *) JSMalloc(50 * sizeof(char));
//		strcopy(buffer + jsObject->number, lineKey, strlen(buffer) - jsObject->number);
//		Log(HeadLengthMark, lineKey);
//	}
//	jsObject = headMap->get(ETagMark);
//	if (jsObject == NULL) {
//		Log((char *) ("ETagMark @@@: "), (char *) ("NULL"));
//	} else {
//		Log((char *) ("ETagMark @@@: "), ETagMark);
//	}
}

//void test7() {
//
//	int fp = 0;
//	if ((fp = open((char *) ("/storage/sdcard0/welinks/test.js"), O_CREAT | O_RDWR, 777)) < 0) {
//		Log((char *) " Can not open !");
//	}
//	struct stat stat_data;
//	if ((fstat(fp, &stat_data)) < 0) {
//		Log((char *) " fstat error !");
//	}
//
//	void* start_fp;
//	if ((start_fp = mmap(NULL, 1024 * 3, PROT_READ | PROT_WRITE, MAP_SHARED, fp, 0)) == (void *) -1) {
//		Log((char *) " mmap error !");
//	}
//	ftruncate(fp, 1); //增加文件大小
//	char * buffer = (char *) start_fp;
//	for (int i = 1; i < 1024 * 3; i++) {
//		*(buffer + i) = 80;
//	}
//	*(buffer + 1024 * 3 - 1) = 0;
//	ftruncate(fp, 1024 * 3); //增加文件大小
////	memcpy(start_fp, "12345678901234567890", 7);
//
//	munmap(start_fp, 1024 * 3);
//	close(fp);
//}
//
//HashTable * parseResponseHead(char * buffer, int length) {
//	HashTable * headMap = new HashTable();
//	headMap->initialize();
//
//	char * lastLine = buffer;
//	char * point = buffer;
//	int lineNumber = 0;
//	for (int i = 0; i < length - 1; i++) {
//		point++;
//		if (*point == '\n') {
//			if (*(point - 1) == '\r') {
////				resolveLine(lastLine, point - lastLine, lineNumber, headMap);
//				if (point - lastLine == 1) {
//					JSObject * jsObject = new JSObject();
//					jsObject->number = i + 2;
//					headMap->set(HeadLengthMark, jsObject);
//					break;
//				}
//				lastLine = point + 1;
//			}
//		}
//	}
//	return headMap;
//}

//void resolveLine(char * start, int length, int lineNumber, HashTable * headMap) {
//	char * lineKey = (char *) JSMalloc(50 * sizeof(char));
//	char * lineValue = (char *) JSMalloc(50 * sizeof(char));
//	char * point = start;
//	bool isKeyValue = false;
//	for (int i = 0; i < length; i++) {
//		point++;
//		if (*point == 58) {
//			isKeyValue = true;
//			strcopy(start, lineKey, i + 1);
//			if (strcmp(lineKey, ContentLengthMark) == 0) {
//				strcopy(start + i + 1, lineValue, length - i - 1);
//				int content_Length = parseStringToNubmer(lineValue, length - i - 1);
//				JSObject * jsObject = new JSObject();
//				jsObject->number = content_Length;
//				headMap->set(ContentLengthMark, jsObject);
//			} else if (strcmp(lineKey, ETagMark) == 0) {
//				strcopy(start + i + 1, lineValue, length - i - 1);
//				int content_Length = parseStringToNubmer(lineValue, length - i - 1);
//				JSObject * jsObject = new JSObject();
//				jsObject->number = 2;
//				headMap->set(ETagMark, jsObject);
//			}
//		}
//	}
//	if (isKeyValue == false) {
//		strcopy(start, lineKey, 4);
//		if (strcmp(lineKey, HttpMark) == 0) {
//			JSObject * jsObject = new JSObject();
//			jsObject->number = 1;
//			headMap->set(HttpMark, jsObject);
//		}
//	}
//}

