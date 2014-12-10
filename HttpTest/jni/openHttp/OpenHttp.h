#ifndef OPENHTTP_H
#define OPENHTTP_H

#include "../data_core/base/JSObject.h"
#include "../data_core/base/MemoryManagement.h"
#include "../data_core/base/LIST.h"
#include "../data_core/base/HashTable.h"
#include "../data_core/base/Queue.h"
#include "lib/Log.h"

#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

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
	int remotePort;
	JSObject * localPort;
	char * sendData;

	int socketFD;
	sockaddr_in * remoteAddress;
	sockaddr_in * localAddress;

	epoll_event * event;

	int sendDataLength = 0;
	int sentLength = 0;
	int sendPackegesNum = 0;
	int sendLastPackegeSize = 0;
	int sentRuturnLength = 0;
	bool isSendBufferFull = false;
	Status * status = new Status();

	HashTable * responseHeadMap;

//	char * receiveData;
	int receivePackagesNumber;
	int receivedLength;
	int receiveContentLength;
	int receiveHeadLength;
	char * receivETag;

	int receiveFD;
	char * receiveBuffer;
	int receiveOffset;
	char * receiveFileBuffer;

	int sendFD;
	int sendOffser;
	char * sendFileBuffer;
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
	LIST * portPool;

	int startPortNumber = 9060;
	int portPoolSize = 10;

	int isReUsedPort = 1;
	int sendBuffSize = 1024;
	int PackegeSize = 1024;
	int MaxBufflen = 10240;

	bool is_initialized = false;

	char * HttpMark = (char *) ("HTTP");
	char * ContentLengthMark = (char *) ("Content-Length");
	char * HeadLengthMark = (char *) ("Head-Length");
	char * ETagMark = (char *) ("ETag");

	char * lineKey;
	char * lineValue;

	bool initialize();
	bool freeHttpEntity(HttpEntity * httpEntity);

	int openSend(char * ip, int remotePort, char * buffer);
	int openDownload(char * ip, int remotePort, char * head, char * body, char * path);
	void openSend(HttpEntity * httpEntity);
	HttpEntity * intializeHttpEntity(HttpEntity * httpEntity, JSObject * port);
	int startConnect(HttpEntity * httpEntity);
	void sendPackeges(HttpEntity * httpEntity);
	int sendPackege(HttpEntity * httpEntity, const void * buffer, int PackegeSize);
	void receivePackage(HttpEntity * httpEntity);
	void parseResponseBody(char * buffer);

	void setState(HttpEntity * httpEntity, int state);

	HashTable * parseResponseHead(char * buffer, int length);
	void resolveLine(char * start, int length, int lineNumber, HashTable * headMap);
	bool setReceiceHead(HttpEntity * httpEntity, HashTable * headMap);

	void mapReceiveFile(HttpEntity * httpEntity);
	bool checkReceive(HttpEntity * httpEntity, int receiveLength);
	void unMapReceiveFile(HttpEntity * httpEntity);

	int openUpload(char * ip, int remotePort, char * head, char * body, char * path);

	void openSend(HttpEntity * httpEntity, JSObject * port);

	void nextHttpEntity();

	HttpEntity * getNewHttpEntity();

	void closeSocketFd(HttpEntity * httpEntity);

	Queue * httpEntitiesQueue;
	HashTable *httpEntitiesMap;
	Queue * httpEntitiedOldQueue;
	int MaxEvent = 100;
	int epollFD = 0;
	epoll_event * epoll_events;
	pthread_t * epollLooperPthread;

	void epollLooper(int epollFD);
};
extern "C" {
extern void CallBack(int type);
}
#endif /* OPENHTTP_H */

