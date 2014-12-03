#ifndef OPENHTTP_H
#define OPENHTTP_H

#include "../data_core/base/JSObject.h"
#include "../data_core/base/MemoryManagement.h"
#include "../data_core/base/LIST.h"
#include "../data_core/base/HashTable.h"
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

class HttpEntity: public JSObject {
public:
	const char * ip;
	JSObject * port;
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

	bool is_initialized = false;

	bool initialize();
	bool free();

	int openSend(const char * ip, char * buffer);
	HttpEntity * intializeHttpEntity(const char * ip, char * buffer);
	int sendData(HttpEntity * httpEntity);
	void sendPackeges(HttpEntity * httpEntity);
	int sendPackege(HttpEntity * httpEntity, const void * buffer, int PackegeSize);

	HashTable *httpEntitiesMap;
	int MaxEvent = 100;
	int epollFD = 0;
	epoll_event * epoll_events;
	pthread_t * epollLooperPthread;

	void epollLooper(int epollFD);
};



#endif /* OPENHTTP_H */

