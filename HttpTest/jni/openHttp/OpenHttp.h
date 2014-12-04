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
	int Queueing = 0, Started = 1, Connecting = 2, Connected = 3, Sending = 4, Sent = 5, Waiting = 5, receiving = 6, received = 7;
	int Failed = 10;
	int state = Queueing;
};
class HttpEntity: public JSObject {
public:
	const char * ip;
	int remotePort;
	JSObject * localPort;
	char * data;

	int socketFD;
	sockaddr_in * remoteAddress;
	sockaddr_in * localAddress;

	epoll_event * event;

	int dataLength = 0;
	int sentLength = 0;
	int packegesNum = 0;
	int lastPackegeSize = 0;
	int sentRuturnLength = 0;
	const char *dataBuffer;
	bool isSocketBufferFull = false;
	Status * status = new Status();
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

	bool initialize();
	bool free();

	int openSend(char * ip, int remotePort, char * buffer);
	HttpEntity * intializeHttpEntity(HttpEntity * httpEntity, JSObject * port);
	int startConnect(HttpEntity * httpEntity);
	void sendPackeges(HttpEntity * httpEntity);
	int sendPackege(HttpEntity * httpEntity, const void * buffer, int PackegeSize);
	void receivePackage(HttpEntity * httpEntity);
	void parseResponseBody(char * buffer);

	void setState(HttpEntity * httpEntity, int state);
	Queue * httpEntitiesQueue;
	HashTable *httpEntitiesMap;
	int MaxEvent = 100;
	int epollFD = 0;
	epoll_event * epoll_events;
	pthread_t * epollLooperPthread;

	void epollLooper(int epollFD);
};

#endif /* OPENHTTP_H */

