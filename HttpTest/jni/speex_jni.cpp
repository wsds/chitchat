#include <jni.h>
#include <stdlib.h>
//#include <stdio.h>
//
#include <sys/mman.h> //mmap头文件#include <fcntl.h>#include <asm-generic/fcntl.h>#include "lib/Log.h"#include "data_core/base/HashTable.h"#include "data_core/base/Queue.h"#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "openHttp/OpenHttp.h"
#include "data_core/base/JSObject.h"
#include <sys/epoll.h>
#include <pthread.h>

#include <sys/stat.h> //文件状态结构#include <unistd.h>#include <asm-generic/mman-common.h>#include <errno.h>#define MAXBUFLEN 	1024#define MAXEVENTS 100void test(signed char * message);static void make_sendipv4addr(struct sockaddr_in *addr, int remoteport);int setSend(const char *ipAddr);int sendPackege(int sockd, const void * buffer, int PackegeSize, unsigned int mode);static void make_recvipv4addr(struct sockaddr_in *addr, const int localport);int recvPacket(int sockd);static unsigned short GetSocketPort(int sd);
int setNonBlocking(int sock);
void setEpoll(int sock);
int sendData(int sockd, const char *buffer);
void sendPackeges(int sockd, const char *buffer);
void test2();
void test3();
void test4(JNIEnv *env, jobject myHttpJNI, jbyteArray message);
void test5();
void test6();
void test7();
void test8();
void resolveLine(char * start, int length, int lineNumber, HashTable * headMap);
HashTable * parseResponseHead(char * buffer, int length);
void epollLooper(int epollFD);

int epollFD = 0;
int listeningSocketFD = 0;
int connectingSocketFD = 0;
int sendingSocketFD = 0;

int PackegeSize = 1024;

int dataLength = 0;
int sentLength = 0;
int packegesNum = 0;
int lastPackegeSize = 0;
int sentRuturnLength = 0;
const char *dataBuffer;
bool isSocketBufferFull = false;

char target[15] = "";

char * HttpMark = (char *) ("HTTP");
char * ContentLengthMark = (char *) ("Content-Length");
char * HeadLengthMark = (char *) ("Head-Length");
char * ETagMark = (char *) ("ETag");

//#define  LOG_TAG    "OpenHttp"
//#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
extern "C" JNIEXPORT jint Java_com_open_clib_MyHttpJNI_nativeSend(JNIEnv *env, jobject obj, jbyteArray ip, jint port, jbyteArray url, jint method, jbyteArray header, jbyteArray body, jint start, jint length, jint id) {

	char * url_buffer;
	char * header_buffer;

	signed char * body_buffer = (signed char*) malloc(length * sizeof(char));
	env->GetByteArrayRegion(body, start, length, body_buffer);
	signed char * ip_buffer = (signed char*) malloc(16 * sizeof(char));
	env->GetByteArrayRegion(ip, 0, 15, ip_buffer);
	Log((char*) "hello");

	OpenHttp * openHttp = OpenHttp::getInstance();
	openHttp->initialize();
//	openHttp->openSend(ip_buffer, body_buffer);

	return (jint) 1;
}

extern "C" JNIEXPORT jint Java_com_open_clib_MyHttpJNI_test(JNIEnv *env, jobject obj, jbyteArray message, jobject myHttpJNI) {

	int length = env->GetArrayLength(message);
	signed char * body_buffer = (signed char*) malloc(length + 1 * sizeof(char));
	env->GetByteArrayRegion(message, 0, length, body_buffer);
	body_buffer[length] = 0;
	Log((char*) body_buffer);

//	test(body_buffer);
//	test4(env, myHttpJNI, message);
//	test(body_buffer);
//	test7();
	test8();
	return (jint) 1;
}

void test(signed char * message) {
	char * title = (char *) ("GET /index.html HTTP/1.1\r\nHost: 192.168.1.11\r\nConnection: keep-alive\r\nContent-Length: 0\r\n\r\n");
//	Log(strlen(title));
	int len = strlen(title);
	char * buffer = (char*) JSMalloc((3500 + len) * sizeof(char));
	for (int i = 0; i < 3500 + len; i++) {
		if (i < len) {
//			Log((char *) ("char"), i);
			*(buffer + i) = *(title + i);
		} else {
//			Log((char *) ("charsss"), i);
			*(buffer + i) = i / 100 + 1;
		}
	}
//	*(buffer + (3500 + len - 1)) = 0;
//	Log(buffer);
//	Log(strlen(buffer));
//	int socket = setSend("192.168.1.7");
//	sendPacket(socket, "GET /index.html HTTP/1.1\r\nHost: www.testhttp.com\r\n\r\n\r\n\r\n");
//	sendPacket(socket, buffer);
//	char target[15] = "";
//	parseNubmerToString((int) GetSocketPort(socket), target);
//	parseNubmerToString(9555444, target);
//	Log((char*) "Connected @ ");
//	Log((char*) target);
//	recvPacket(socket);

	OpenHttp * openHttp = OpenHttp::getInstance();
	openHttp->initialize();
	//char * ip, int remotePort, char * head, char * body, char * path
	openHttp->openDownload((char *) ("192.168.1.11"), 80, title, (char *) "123456", (char *) "/storage/sdcard0/welinks/index.html");
//	openHttp->openSend((char *) ("192.168.1.7"), 80, title);
}
void test8() {
	char * title = (char *) ("PUT /api2/bug/send? HTTP/1.1\r\nHost: 192.168.1.11\r\nConnection: keep-alive\r\nContent-Length: 3004\r\n\r\n");
	int len = strlen(title);
	Log(len);
	char * end = (char *) ("\r\n\r\n");
	char * buffer = (char*) JSMalloc((3000 + len + 4) * sizeof(char));
	for (int i = 0; i < 3000 + len + 4; i++) {
		if (i < len) {
			*(buffer + i) = *(title + i);
		} else if (i < 3000 + len) {
			*(buffer + i) = i / 100 + 1;
		} else if (i <= 3000 + len + 4) {
			*(buffer + i) = *(end + i % (3000 + len + 4));
		}
	}
//	*(buffer + len) = (char *) "\r\n\r\n";
//	int len1 = strlen((char *) ("\r\n\r\n"));
//	Log(len1);
	OpenHttp * openHttp = OpenHttp::getInstance();
	openHttp->initialize();
	//char * ip, int remotePort, char * head, char * body, char * path
	openHttp->openUpload((char *) ("192.168.1.11"), 8090, title, buffer, (char *) "/storage/sdcard0/welinks/index.html");
}

//void *epollLooperThread1(void *arg) {
//	epollLooper(epollFD);
//}
void test2() {

	char * buffer = (char*) malloc(1024 * 3 * sizeof(char));
	for (int i = 1; i < 1024 * 3 - 2; i++) {
		*(buffer + i) = i / 100 + 1;
	}
	*(buffer + 1024 * 3 - 1) = 0;

//	sendingSocketFD = setSend("192.168.1.7");
//	setEpoll(sendingSocketFD);

	pthread_t epollLooperPthread;
//	int ret = pthread_create(&epollLooperPthread, NULL, epollLooperThread1, (void *) 1);
	sleep(1);
//	sendData(sendingSocketFD, buffer);

}

void test3() {
	signed char * buffer = (signed char *) malloc(1024 * 3 * sizeof(char));
	for (int i = 1; i < 1024 * 3 - 2; i++) {
		*(buffer + i) = i / 100 + 1;
	}
	*(buffer + 1024 * 3 - 1) = 0;

	OpenHttp *openHttp = OpenHttp::getInstance();

	openHttp->initialize();
	openHttp->openSend((char *) ("192.168.1.7"), 8091, (char *) buffer);
}

void test4(JNIEnv *env, jobject myHttpJNI, jbyteArray message) {
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
	char * buffer = (char *) ("HTTP/1.1 200 OK\r\nContent-Length: 4095\r\nETag: 54800b5f-fff\r\nConnection: keep-alive\r\n\r\nabcdefghhjk");
	Log(strlen(buffer));
	HashTable * headMap = parseResponseHead(buffer, strlen(buffer));
	JSObject * jsObject;
	jsObject = headMap->get(HttpMark);
	Log((char *) ("HttpMark: "), jsObject->number);

	jsObject = headMap->get(ContentLengthMark);
	if (jsObject == NULL) {
		Log((char *) ("ContentLengthMark @@@: "), (char *) ("NULL"));
	} else {
		Log((char *) ("ContentLengthMark @@@: "), jsObject->number);
	}
	jsObject = headMap->get(HeadLengthMark);
	if (jsObject == NULL) {
		Log((char *) ("HeadLengthMark @@@: "), (char *) ("NULL"));
	} else {
		char * lineKey = (char *) JSMalloc(50 * sizeof(char));
		strcopy(buffer + jsObject->number, lineKey, strlen(buffer) - jsObject->number);
		Log(HeadLengthMark, lineKey);
	}
	jsObject = headMap->get(ETagMark);
	if (jsObject == NULL) {
		Log((char *) ("ETagMark @@@: "), (char *) ("NULL"));
	} else {
		Log((char *) ("ETagMark @@@: "), ETagMark);
	}
}

void test7() {

	int fp = 0;
	if ((fp = open((char *) ("/storage/sdcard0/welinks/test.js"), O_CREAT | O_RDWR, 777)) < 0) {
		Log((char *) " Can not open !");
	}
	struct stat stat_data;
	if ((fstat(fp, &stat_data)) < 0) {
		Log((char *) " fstat error !");
	}

	void* start_fp;
	if ((start_fp = mmap(NULL, 1024 * 3, PROT_READ | PROT_WRITE, MAP_SHARED, fp, 0)) == (void *) -1) {
		Log((char *) " mmap error !");
	}
	ftruncate(fp, 1); //增加文件大小
	char * buffer = (char *) start_fp;
	for (int i = 1; i < 1024 * 3; i++) {
		*(buffer + i) = 80;
	}
	*(buffer + 1024 * 3 - 1) = 0;
	ftruncate(fp, 1024 * 3); //增加文件大小
//	memcpy(start_fp, "12345678901234567890", 7);

	munmap(start_fp, 1024 * 3);
	close(fp);
}

HashTable * parseResponseHead(char * buffer, int length) {
	HashTable * headMap = new HashTable();
	headMap->initialize();

	char * lastLine = buffer;
	char * point = buffer;
	int lineNumber = 0;
	for (int i = 0; i < length - 1; i++) {
		point++;
		if (*point == '\n') {
			if (*(point - 1) == '\r') {
				resolveLine(lastLine, point - lastLine, lineNumber, headMap);
				if (point - lastLine == 1) {
					JSObject * jsObject = new JSObject();
					jsObject->number = i + 2;
					headMap->set(HeadLengthMark, jsObject);
					break;
				}
				lastLine = point + 1;
			}
		}
	}
	return headMap;
}

void resolveLine(char * start, int length, int lineNumber, HashTable * headMap) {
	char * lineKey = (char *) JSMalloc(50 * sizeof(char));
	char * lineValue = (char *) JSMalloc(50 * sizeof(char));
	char * point = start;
	bool isKeyValue = false;
	for (int i = 0; i < length; i++) {
		point++;
		if (*point == 58) {
			isKeyValue = true;
			strcopy(start, lineKey, i + 1);
			if (strcmp(lineKey, ContentLengthMark) == 0) {
				strcopy(start + i + 1, lineValue, length - i - 1);
				int content_Length = parseStringToNubmer(lineValue, length - i - 1);
				JSObject * jsObject = new JSObject();
				jsObject->number = content_Length;
				headMap->set(ContentLengthMark, jsObject);
			} else if (strcmp(lineKey, ETagMark) == 0) {
				strcopy(start + i + 1, lineValue, length - i - 1);
				int content_Length = parseStringToNubmer(lineValue, length - i - 1);
				JSObject * jsObject = new JSObject();
				jsObject->number = 2;
				headMap->set(ETagMark, jsObject);
			}
		}
	}
	if (isKeyValue == false) {
		strcopy(start, lineKey, 4);
		if (strcmp(lineKey, HttpMark) == 0) {
			JSObject * jsObject = new JSObject();
			jsObject->number = 1;
			headMap->set(HttpMark, jsObject);
		}
	}
}

