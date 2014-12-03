#include "OpenHttp.h"

OpenHttp * OpenHttp::instance = NULL;

bool OpenHttp::initialize() {

	if (this->is_initialized == true) {
		return true;
	}
	this->is_initialized = true;
	this->portPool = new LIST();
	this->portPool->initialize();
	for (int i = 0; i < portPoolSize; i++) {
		JSObject * port = new JSObject();
		port->number = startPortNumber + i;
		this->portPool->push(port);
	}

	this->httpEntitiesMap = new HashTable();
	this->httpEntitiesMap->initialize();

	this->epollFD = epoll_create(1024);

	epollLooperPthread = new pthread_t();
	int ret = pthread_create(epollLooperPthread, NULL, epollLooperThread, (void *) 1);
	sleep(1);
	return true;
}

bool OpenHttp::free() {
	return true;
}
int OpenHttp::openSend(const char * ip, char * buffer) {
	HttpEntity * httpEntity = this->intializeHttpEntity(ip, buffer);
	//TO DO reuse the httpEntity
	if (httpEntity != NULL) {
		this->httpEntitiesMap->set(httpEntity->socketFD, httpEntity);
		Log((char*) "httpEntitiesMap->set@");
		Log(httpEntity->socketFD);
		this->sendData(httpEntity);
	}
	return 1;
}

HttpEntity * OpenHttp::intializeHttpEntity(const char * ip, char * buffer) {
	JSObject * port = this->portPool->pop();
	if (port == NULL) {
		return NULL;
	}

	int socketFD = socket(AF_INET, SOCK_STREAM, 0);
	if (socketFD < 0) {
		return NULL;
	}

	HttpEntity * httpEntity = new HttpEntity();
	httpEntity->ip = ip;
	httpEntity->data = buffer;
	httpEntity->port = port;

	httpEntity->socketFD = socketFD;
	httpEntity->remoteAddress = new sockaddr_in();
	memset(httpEntity->remoteAddress, 0, sizeof(struct sockaddr_in));
	httpEntity->remoteAddress->sin_family = AF_INET;
	httpEntity->remoteAddress->sin_port = htons(8091);
	httpEntity->remoteAddress->sin_addr.s_addr = inet_addr("192.168.1.7");

	httpEntity->socketFD = socketFD;
	httpEntity->localAddress = new sockaddr_in();
	memset(httpEntity->localAddress, 0, sizeof(struct sockaddr_in));
	httpEntity->localAddress->sin_family = AF_INET;
	httpEntity->localAddress->sin_port = htons(httpEntity->port->number);
	httpEntity->localAddress->sin_addr.s_addr = INADDR_ANY;

	setsockopt(httpEntity->socketFD, SOL_SOCKET, SO_REUSEADDR, &(this->isReUsedPort), sizeof(int));
	setsockopt(httpEntity->socketFD, SOL_SOCKET, SO_SNDBUF, &this->sendBuffSize, sizeof(int));

	if (-1 == bind(httpEntity->socketFD, (sockaddr *) httpEntity->localAddress, sizeof(sockaddr_in))) {
		char target[15] = "";
		Log(sizeof(httpEntity->localAddress));
		Log((char*) "bind fail !");
		return NULL;
	}
	Log((char*) "bind ok !");

	int status = connect(httpEntity->socketFD, (sockaddr *) httpEntity->remoteAddress, sizeof(sockaddr_in));

	if (status != 0) {
		Log((char*) "Connect fail!");
		return NULL;
	}
	Log((char*) "Connected");

	int flags = fcntl(httpEntity->socketFD, F_GETFL, 0);
	flags |= O_NONBLOCK;
	fcntl(httpEntity->socketFD, F_SETFL, flags);

	httpEntity->event = new epoll_event();
	httpEntity->event->data.fd = (httpEntity->socketFD);
	httpEntity->event->events = EPOLLIN | EPOLLOUT | EPOLLET;
	epoll_ctl(this->epollFD, EPOLL_CTL_ADD, httpEntity->socketFD, httpEntity->event);

	return httpEntity;
}

int OpenHttp::sendData(HttpEntity * httpEntity) {
	Log((char*) "sendData");
	httpEntity->dataLength = strlen(httpEntity->data);
	httpEntity->sentLength = 0;

	httpEntity->packegesNum = httpEntity->dataLength / this->PackegeSize;
	httpEntity->lastPackegeSize = httpEntity->dataLength % this->PackegeSize;
	if (httpEntity->lastPackegeSize != 0) {
		httpEntity->packegesNum = httpEntity->dataLength / this->PackegeSize + 1;
	}

	this->sendPackeges(httpEntity);
	return 1;
}

void OpenHttp::sendPackeges(HttpEntity * httpEntity) {
	Log((char*) "sendPackeges");
	Log(httpEntity->packegesNum);
	Log(httpEntity->sentLength);
	Log(httpEntity->dataLength);
	Log((char*) "@@@@@@@@@@@@@@@@@@@@@@@@@");
	if (httpEntity->packegesNum <= 0 || httpEntity->sentLength >= httpEntity->dataLength) {
		return;
	}
	char * buffer = httpEntity->data + httpEntity->sentLength;
	for (int i = httpEntity->sentLength / this->PackegeSize; i < httpEntity->packegesNum - 1; i++) {

		int sentPackegeLength = this->sendPackege(httpEntity, buffer, this->PackegeSize);

		if (httpEntity->isSocketBufferFull) {
			return;
		}
		httpEntity->sentLength += sentPackegeLength;
		buffer = buffer + this->PackegeSize;
	}

	if (httpEntity->lastPackegeSize != 0) {
		httpEntity->sentLength += this->sendPackege(httpEntity, buffer, httpEntity->lastPackegeSize);
	} else {
		httpEntity->sentLength += this->sendPackege(httpEntity, buffer, this->PackegeSize);
	}
}

int OpenHttp::sendPackege(HttpEntity * httpEntity, const void * buffer, int PackegeSize) {
	Log((char*) "send one Packege");
	int sentPackegeLength = send(httpEntity->socketFD, buffer, PackegeSize, 0);
	if (sentPackegeLength == -1) {
		if (errno == EAGAIN) {
			httpEntity->isSocketBufferFull = true;
			Log((char*) "缓冲区已满");

		} else if (errno == ECONNRESET) {
			// 对端重置,对方发送了RST
		} else if (errno == EINTR) {
			// 被信号中断
		} else {
			sentPackegeLength = 0;
		}
		sentPackegeLength = 0;
	}
	return sentPackegeLength;
}

void *epollLooperThread(void *arg) {
	OpenHttp *openHttp = OpenHttp::getInstance();
	openHttp->epollLooper(openHttp->epollFD);
}

void OpenHttp::epollLooper(int epollFD) {

	this->epoll_events = (epoll_event*) JSMalloc(this->MaxEvent * sizeof(epoll_event));
	int numEvents = 0;
	Log((char*) "epollLooper started ! ");
	while (true) {
		numEvents = epoll_wait(this->epollFD, this->epoll_events, this->MaxEvent, 1000);
		Log((char*) "epollLooper events");

		for (int i = 0; i < numEvents; ++i) {
			Log((char*) "resolve event");
			epoll_event * event = this->epoll_events + i;
//			if (event->data.fd == listeningSocketFD) {
////				Log((char*)"resolve event  新建连接");
////				struct sockaddr clientAddress;
////				socklen_t clientaddrLen = (socklen_t) sizeof(clientAddress);
////				connectingSocketFD = accept(listeningSocketFD, &clientAddress, &(clientaddrLen));
////				epoll_event* clientEvent = new epoll_event();
////				clientEvent->data.fd = connectingSocketFD;
////				clientEvent->events = EPOLLIN | EPOLLET;
////				epoll_ctl(epollFD, EPOLL_CTL_ADD, connectingSocketFD, clientEvent);
//			}
			if (event->events & EPOLLIN) //接收到数据，读socket
			{
//				Log((char*)"resolve event  接收到数据");
//				recvPacket(this->epoll_events[i].data.fd);
			}
			if (event->events & EPOLLOUT) {
				Log((char*) "resolve event 缓冲区可写@");
				Log(event->data.fd);
				Log(this->httpEntitiesMap->length);
				HttpEntity * httpEntity = (HttpEntity *) this->httpEntitiesMap->get(event->data.fd);
				Log((char*) "httpEntitiesMap->get@");
				Log(event->data.fd);
				if (httpEntity != NULL) {
					httpEntity->isSocketBufferFull = false;
					Log((char*) "@@@@@@@@@@@###########################@");
					Log(event->data.fd);
					this->sendPackeges(httpEntity);
				}
			}

			{
				//其他的处理
//				Log((char*)"事件@");
////				parseNubmerToString(this->epoll_events[i].events, target);
//				Log(target);
			}
		}
	}
}
