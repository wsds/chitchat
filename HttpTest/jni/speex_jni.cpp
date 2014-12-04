#include <jni.h>
#include <stdlib.h>

#include "lib/Log.h"
#include "data_core/base/HashTable.h"
#include "data_core/base/Queue.h"
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "openHttp/OpenHttp.h"
#include <fcntl.h>

#include "data_core/base/JSObject.h"

#include <sys/epoll.h>
#include <pthread.h>

#include <errno.h>
#define MAXBUFLEN 	1024
#define MAXEVENTS 100

void test(signed char * message);
static void make_sendipv4addr(struct sockaddr_in *addr, int remoteport);
int setSend(const char *ipAddr);
int sendPackege(int sockd, const void * buffer, int PackegeSize, unsigned int mode);
static void make_recvipv4addr(struct sockaddr_in *addr, const int localport);
int recvPacket(int sockd);
static unsigned short GetSocketPort(int sd);
int setNonBlocking(int sock);
void setEpoll(int sock);
int sendData(int sockd, const char *buffer);
void sendPackeges(int sockd, const char *buffer);
void test2();
void test3();
void test4(JNIEnv *env, jobject myHttpJNI, jbyteArray message);
void test5();
void test6();
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
extern "C"
JNIEXPORT jint Java_com_open_clib_MyHttpJNI_nativeSend(JNIEnv *env, jobject obj, jbyteArray ip, jint port, jbyteArray url, jint method, jbyteArray header, jbyteArray body, jint start, jint length, jint id) {

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

extern "C"
JNIEXPORT jint Java_com_open_clib_MyHttpJNI_test(JNIEnv *env, jobject obj, jbyteArray message, jobject myHttpJNI) {

	int length = env->GetArrayLength(message);
	signed char * body_buffer = (signed char*) malloc(length + 1 * sizeof(char));
	env->GetByteArrayRegion(message, 0, length, body_buffer);
	body_buffer[length] = 0;
	Log((char*) body_buffer);

//	test(body_buffer);
//	test4(env, myHttpJNI, message);
//	test(body_buffer);
	test6();
	return (jint) 1;
}

void test(signed char * message) {
	char * title = (char *) ("GET /test2.html HTTP/1.1\r\nHost: 192.168.1.7\r\nConnection: keep-alive\r\nContent-Length: 0\r\n\r\n");
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
	openHttp->openSend((char *) ("192.168.1.7"), 80, title);
}

void *epollLooperThread1(void *arg) {
	epollLooper(epollFD);
}
void test2() {

	char * buffer = (char*) malloc(1024 * 3 * sizeof(char));
	for (int i = 1; i < 1024 * 3 - 2; i++) {
		*(buffer + i) = i / 100 + 1;
	}
	*(buffer + 1024 * 3 - 1) = 0;

	sendingSocketFD = setSend("192.168.1.7");
	setEpoll(sendingSocketFD);

	pthread_t epollLooperPthread;
	int ret = pthread_create(&epollLooperPthread, NULL, epollLooperThread1, (void *) 1);
	sleep(1);
	sendData(sendingSocketFD, buffer);

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

void epollLooper(int epollFD) {

	struct epoll_event events[MAXEVENTS];
	int numEvents = 0;
	Log((char*) "epollLooper started ! ");
	while (true) {
		numEvents = epoll_wait(epollFD, events, MAXEVENTS, 1000);
		Log((char*) "epollLooper events");

		for (int i = 0; i < numEvents; ++i) {
			Log((char*) "resolve event");
			if (events[i].data.fd == listeningSocketFD) {
				Log((char*) "resolve event  新建连接");
				struct sockaddr clientAddress;
				socklen_t clientaddrLen = (socklen_t) sizeof(clientAddress);
				connectingSocketFD = accept(listeningSocketFD, &clientAddress, &(clientaddrLen));
				epoll_event* clientEvent = new epoll_event();
				clientEvent->data.fd = connectingSocketFD;
				clientEvent->events = EPOLLIN | EPOLLET;
				epoll_ctl(epollFD, EPOLL_CTL_ADD, connectingSocketFD, clientEvent);
			}
			if (events[i].events & EPOLLIN) //接收到数据，读socket
			{
				Log((char*) "resolve event  接收到数据");
				recvPacket(events[i].data.fd);
			}
			if (events[i].events & EPOLLOUT) {
				Log((char*) "resolve event EPOLLOUT");

				struct epoll_event event = events[i];
				if (events[i].data.fd == sendingSocketFD) {
					isSocketBufferFull = false;
					Log((char*) "缓冲区可写");
					sendPackeges(sendingSocketFD, dataBuffer);
					event.events = EPOLLIN | EPOLLET;
//					epoll_ctl(epollFD, EPOLL_CTL_MOD, sendingSocketFD, &event);
				}

			}

			{
				//其他的处理
				Log((char*) "事件@");
				parseNubmerToString(events[i].events, target);
				Log((char*) target);
			}
		}
	}
}

int sendData(int sockd, const char *buffer) {
	Log((char*) "sendData");
	dataBuffer = buffer;
	dataLength = strlen(dataBuffer);
	sentLength = 0;

	packegesNum = dataLength / PackegeSize;
	lastPackegeSize = dataLength % PackegeSize;
	if (lastPackegeSize != 0) {
		packegesNum = dataLength / PackegeSize + 1;
	}

	sendPackeges(sockd, dataBuffer);
//	send(sockd, str, strlen(str), 0);
	return 1;
}

void sendPackeges(int sockd, const char *buffer) {
	if (packegesNum <= 0 || sentLength >= dataLength) {
		return;
	}
	buffer = buffer + sentLength;
	Log((char*) "sendPackeges");
	for (int i = sentLength / PackegeSize; i < packegesNum - 1; i++) {

		int sentPackegeLength = sendPackege(sockd, buffer, PackegeSize, 0);

		if (isSocketBufferFull) {
			return;
		}
		sentLength += sentPackegeLength;
		buffer = buffer + PackegeSize;
	}

	if (lastPackegeSize != 0) {
		sentLength += sendPackege(sockd, buffer, lastPackegeSize, 0);
	} else {
		sentLength += sendPackege(sockd, buffer, PackegeSize, 0);
	}
}

int sendPackege(int sockd, const void * buffer, int PackegeSize, unsigned int mode) {
	Log((char*) "send ont Packege");
	int sentPackegeLength = send(sockd, buffer, PackegeSize, 0);
	if (sentPackegeLength == -1) {
		if (errno == EAGAIN) {
			sentPackegeLength = PackegeSize;
			isSocketBufferFull = true;
			Log((char*) "缓冲区已满");
			sentPackegeLength = 0;
		} else if (errno == ECONNRESET) {
			// 对端重置,对方发送了RST
		} else if (errno == EINTR) {
			// 被信号中断
		} else {
			sentPackegeLength = 0;
		}
	}
	return sentPackegeLength;
}

static void make_sendipv4addr(struct sockaddr_in *addr, int remoteport) {
	memset(addr, 0, sizeof(struct sockaddr_in));
	addr->sin_family = AF_INET;
	addr->sin_port = htons(remoteport);
}

int setSend(const char *ipAddr) {
	int sockd = 0;
	struct sockaddr_in addr;
	make_sendipv4addr(&addr, 8091);
	sockd = socket(AF_INET, SOCK_STREAM, 0);
	addr.sin_addr.s_addr = inet_addr("192.168.1.7");

	struct sockaddr_in serveraddr;
	make_recvipv4addr(&serveraddr, 6868);
	int isReUsedPort = 1;
	int sendBuffSize = 1024;
	setsockopt(sockd, SOL_SOCKET, SO_REUSEADDR, &(isReUsedPort), sizeof(isReUsedPort));
	setsockopt(sockd, SOL_SOCKET, SO_SNDBUF, &sendBuffSize, sizeof(sendBuffSize));

	if (-1 == bind(sockd, (struct sockaddr *) &serveraddr, sizeof(serveraddr))) {
		Log((char*) "bind fail !\r\n");
		return -1;
	}
	Log((char*) "bind ok !\r\n");

//	if (-1 == listen(sockd, 5)) {
//		Log((char*) "listen fail !\r\n");
//		return -1;
//	}
//	Log((char*) "listen ok\r\n");

	int status = connect(sockd, (struct sockaddr *) &addr, sizeof(addr));

	if (status != 0) {

		parseNubmerToString(errno, target);
		Log((char*) target);
		Log((char*) "Connect fail!\n");
		return -1;
	}
	Log((char*) "Connected\n");
	setNonBlocking(sockd);
	if ((errno == EAGAIN) || (errno == EWOULDBLOCK)) {
//non-blocking模式下无新connection请求，跳出while (1)
	}
	return sockd;
}

void setEpoll(int sock) {
	epollFD = epoll_create(1024);
	struct epoll_event event;

	event.data.fd = (sock);
	event.events = EPOLLIN | EPOLLOUT | EPOLLET;

	epoll_ctl(epollFD, EPOLL_CTL_ADD, sock, &event);

	parseNubmerToString(epollFD, target);
	Log((char*) "setEpoll@");
	Log((char*) target);

}

int setNonBlocking(int sock) {
	int flags = 0;

	flags = fcntl(sock, F_GETFL, 0);
	flags |= O_NONBLOCK;
	fcntl(sock, F_SETFL, flags);

	return 1;
}

//返回当前socket绑定的端口
static unsigned short GetSocketPort(int sd) {
	unsigned short port = 0;
	struct sockaddr_in address;
	socklen_t addressLength = sizeof(address);
	if (-1 == getsockname(sd, (struct sockaddr*) &address, &addressLength)) {
	} else {
		port = ntohs(address.sin_port);
	}
	return port;
}

//int sendPacket(int sockd, const char *str) {
////	char data[MAXBUFLEN] = { 0 };
////	strcpy(data, str);
//	send(sockd, str, strlen(str), 0);
//	parseNubmerToString(strlen(str), target);
//	Log((char*) target);
//	return 1;
//}

static void make_recvipv4addr(struct sockaddr_in *addr, const int localport) {
	memset(addr, 0, sizeof(struct sockaddr_in));

	addr->sin_family = AF_INET;
	addr->sin_port = htons(localport);
	addr->sin_addr.s_addr = INADDR_ANY;
}

int setupRecv() {
	int optval = 1;
	int socklsn = 0;
	struct sockaddr_in serveraddr;

	make_recvipv4addr(&serveraddr, 6868);

	socklsn = socket(AF_INET, SOCK_STREAM, 0);

	setsockopt(optval, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof(optval));

	bind(socklsn, (struct sockaddr *) &serveraddr, sizeof(serveraddr));
	return socklsn;
}

int recvPacket(int sockd) {
	char packet[MAXBUFLEN] = { 0 };
	char * packetPtr = packet;

	int nBytesNeed = MAXBUFLEN;
	int nBytesRecv = 10;

	while (nBytesRecv > 0) {
		Log((char*) "ready to recv");
		nBytesRecv = recv(sockd, packetPtr, MAXBUFLEN, 0);
		Log((char*) "received");
		Log((char*) packetPtr);
	}

	return 1;
}
