#ifndef OPENHTTP_H
#define OPENHTTP_H

#include "../data_core/base/JSObject.h"
#include "../data_core/base/MemoryManagement.h"
#include "../data_core/base/List.h"
#include "../data_core/base/HashTable.h"
#include "../data_core/base/Queue.h"
#include "../data_core/JSON.h"
#include "lib/Log.h"

#include <jni.h>

#include <sys/socket.h>
#include <string.h>

#include <netinet/in.h>
#include <arpa/inet.h>

#include <linux/tcp.h>

#include <fcntl.h>

#include <sys/epoll.h>
#include <pthread.h>

#include <errno.h>

#ifndef NULL
#define NULL 0
#endif /* NULL */

class Status {
public:
	int Queueing = 0, Started = 1, Connecting = 2, Connected = 3, Sending = 4, Sent = 5, Waiting = 5, Receiving = 6, Received = 7;
	int Failed = 10, Timeout = 11;
	long time_queueing = 0, time_started = 0, time_connecting = 0, time_connected = 0, time_sending = 0, time_sent = 0, time_waiting = 0, time_receiving = 0, time_received = 0;
	long time_failed = 0, time_timeout = 0;
	int state = Queueing;
};
class DataContainer: public JSObject {
public:
	char * data;
	int length;
	int maxLength = 10 * 1024;
};
class HttpEntity: public JSObject {
public:
	const char * ip;
	sockaddr_in * remoteAddress;
	sockaddr_in * localAddress;
	int remotePort;
	JSObject * localPort;

	/*
	 * 0 API||1 UPLOAD||2 DOWNLOAD||3 LONGPULL
	 */
	int type = 0;
	int id;
	int socketFD;

	epoll_event * event;

	bool keep_alive = false;

	int sendDataLength = 0;
	int sentLength = 0;
	char * sendData;

	char * path;
	int sendFD;
	char * sendFileBuffer;
	int sendFileStart = 0;
	int sendFileLength = 0;

	int sendPackegesNum = 0;
	int sendLastPackegeSize = 0;

	float send_percent = 0;

	bool isSendBufferFull = false;
	Status * status = new Status();

	HashTable * responseHeadMap;

//	char * receiveData;
	int receivePackagesNumber;
	int receivedLength;
	int receiveContentLength;
	int receiveHeadLength;
	char * receivETag = NULL;

	int receiveFD;
	char * receiveBuffer;
	int receiveOffset;
	char * receiveFileBuffer;
	char * receiveDataBuffer;

	float receive_percent = 0;

	JSON * receiveHeaders;

	int partId;

};

void *epollLooperThread(void *arg);

class OpenHttp {
public:

	static OpenHttp *instance;

	static OpenHttp * getInstance() {
		if (instance == NULL) {
			instance = new OpenHttp();
		}
		return instance;
	}
	JavaVM * callback_jvm;
	_jobject * callback_object;
	_jmethodID * callback_method;

	const char base[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

	Queue * portPool;

	int startPortNumber = 9060;
	int portPoolSize = 10;

	int isReUsedPort = 1;
	int sendBuffSize = 1024;
	int PackegeSize = 1024;
	int MaxBufflen = 10240;

	bool is_initialized = false;

	char * HttpMark = (char *) ("HTTP");
	char * StatusCode = (char *) ("StatusCode");
	char * ContentLengthMark = (char *) ("Content-Length");
	char * HeadLengthMark = (char *) ("Head-Length");
	char * ETagMark = (char *) ("ETag");
	char * DateMark = (char *) ("Date");
	char * ContentTypeMark = (char *) ("Content-Type");
	char * ServerMark = (char *) ("Server");
	char * ConnectionMark = (char *) ("Connection");

	char * lineKey;
	char * lineValue;

	bool initialize();
	bool freeHttpEntity(HttpEntity * httpEntity);

	int openSend(char * ip, int remotePort, char * buffer, int length, int id);
	int openDownload(char * ip, int remotePort, char * body, char * path, int id, int length);
	int openUpload(char * ip, int remotePort, char * head, char * path, int id, int head_length, int start, int length);
	int openLongPull(char * ip, int remotePort, char * buffer, int length, int partId);
	void openSend(HttpEntity * httpEntity);
	HttpEntity * intializeHttpEntity(HttpEntity * httpEntity, JSObject * port);
	int startConnect(HttpEntity * httpEntity);
	void sendPackeges(HttpEntity * httpEntity);
	void sendUploadPackeges(HttpEntity * httpEntity);
	int sendPackege(HttpEntity * httpEntity, const void * buffer, int PackegeSize);
	void receivePackage(HttpEntity * httpEntity);
	void parseResponseBody(char * buffer);

	void setState(HttpEntity * httpEntity, int state);

	HashTable * parseResponseHead(char * buffer, int length);
	void resolveLine(char * start, int length, int lineNumber, HashTable * headMap);
	bool setReceiceHead(HttpEntity * httpEntity, HashTable * headMap);

	void mapReceiveFile(HttpEntity * httpEntity);
	void mapReceiveData(HttpEntity * httpEntity);
	bool checkReceive(HttpEntity * httpEntity, int receiveLength);
	void unMapReceiveFile(HttpEntity * httpEntity);
	void onEndConnect(HttpEntity * httpEntity);

	void openSendQueue();
	void openSendHttpEntity(HttpEntity * httpEntity, JSObject * port);

	HttpEntity * getNewHttpEntity();

	void closeSocketFd(HttpEntity * httpEntity);

	timeval * iTimeval;
	long start_time;
	long getCurrentMillisecond();

	char * base64_encode(const char* data, int data_len);

	tcp_info * i_tcp_info;
	int tcp_info_length;

	Queue * httpEntitiesQueue;
	HashTable *httpEntitiesMap;
	HashTable *httpEntitiesIdMap;
	Queue * httpEntitiedOldQueue;
	int MaxEvent = 100;
	int epollFD = 0;
	epoll_event * epoll_events;
	pthread_t * epollLooperPthread;

	void epollLooper(int epollFD);
};
extern "C" {
extern void CallBack(int id, int type, const signed char * responseInfo, int partId);
}
#endif /* OPENHTTP_H */

