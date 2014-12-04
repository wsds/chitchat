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

	this->httpEntitiesQueue = new Queue();
	this->httpEntitiesQueue->initialize();

	this->epollFD = epoll_create(1024);

	epollLooperPthread = new pthread_t();
	int ret = pthread_create(epollLooperPthread, NULL, epollLooperThread, (void *) 1);
	sleep(1);
	return true;
}

bool OpenHttp::free() {
	return true;
}
int OpenHttp::openSend(char * ip, int remotePort, char * buffer) {

	JSObject * port = this->portPool->pop();
	HttpEntity * httpEntity = new HttpEntity();
	httpEntity->ip = (const char *) ip;
	httpEntity->remotePort = remotePort;
	httpEntity->data = buffer;
	if (port == NULL) {
		this->httpEntitiesQueue->offer(httpEntity);
		this->setState(httpEntity, httpEntity->status->Queueing);
	} else {
		this->setState(httpEntity, httpEntity->status->Started);

		httpEntity = this->intializeHttpEntity(httpEntity, port);
		this->httpEntitiesMap->set(httpEntity->socketFD, httpEntity);
		this->startConnect(httpEntity);
	}

	//TO DO reuse the httpEntity
	return 1;
}

HttpEntity * OpenHttp::intializeHttpEntity(HttpEntity * httpEntity, JSObject * port) {

	int socketFD = socket(AF_INET, SOCK_STREAM, 0);
	if (socketFD < 0) {
		return NULL;
	}

	httpEntity->localPort = port;

	httpEntity->socketFD = socketFD;
	httpEntity->remoteAddress = new sockaddr_in();
	memset(httpEntity->remoteAddress, 0, sizeof(struct sockaddr_in));
	httpEntity->remoteAddress->sin_family = AF_INET;
	httpEntity->remoteAddress->sin_port = htons(httpEntity->remotePort);
	httpEntity->remoteAddress->sin_addr.s_addr = inet_addr(httpEntity->ip);

	httpEntity->socketFD = socketFD;
	httpEntity->localAddress = new sockaddr_in();
	memset(httpEntity->localAddress, 0, sizeof(struct sockaddr_in));
	httpEntity->localAddress->sin_family = AF_INET;
	httpEntity->localAddress->sin_port = htons(httpEntity->localPort->number);
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

	int flags = fcntl(httpEntity->socketFD, F_GETFL, 0);
	flags |= O_NONBLOCK;
	fcntl(httpEntity->socketFD, F_SETFL, flags);

	httpEntity->event = new epoll_event();
	httpEntity->event->data.fd = (httpEntity->socketFD);
	httpEntity->event->events = EPOLLIN | EPOLLOUT | EPOLLET;

	return httpEntity;
}

int OpenHttp::startConnect(HttpEntity * httpEntity) {
	Log((char*) "startConnect");
	httpEntity->dataLength = strlen(httpEntity->data);
	httpEntity->sentLength = 0;

	httpEntity->packegesNum = httpEntity->dataLength / this->PackegeSize;
	httpEntity->lastPackegeSize = httpEntity->dataLength % this->PackegeSize;
	if (httpEntity->lastPackegeSize != 0) {
		httpEntity->packegesNum = httpEntity->dataLength / this->PackegeSize + 1;
	}

	epoll_ctl(this->epollFD, EPOLL_CTL_ADD, httpEntity->socketFD, httpEntity->event);

	int status = connect(httpEntity->socketFD, (sockaddr *) httpEntity->remoteAddress, sizeof(sockaddr_in));

	if (status != 0) {
		if (errno == EINPROGRESS) {
			this->setState(httpEntity, httpEntity->status->Connecting);
			Log((char*) "正在连接");
		} else {
			Log((char*) "Connect fail!");
			return 0;
		}
	}
	return 1;
}

void OpenHttp::sendPackeges(HttpEntity * httpEntity) {
	Log((char*) "sendPackeges");
//	Log(httpEntity->packegesNum);
//	Log(httpEntity->sentLength);
//	Log(httpEntity->dataLength);
	if (httpEntity->packegesNum <= 0 || httpEntity->sentLength >= httpEntity->dataLength) {
		if (httpEntity->sentLength >= httpEntity->dataLength) {
			this->setState(httpEntity, httpEntity->status->Waiting);
		}
		return;
	}
	this->setState(httpEntity, httpEntity->status->Sending);
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
	if (httpEntity->sentLength >= httpEntity->dataLength) {
		this->setState(httpEntity, httpEntity->status->Sent);
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
void OpenHttp::receivePackage(HttpEntity * httpEntity) {
	char * packet = (char *) JSMalloc(this->MaxBufflen * sizeof(char));
	char * packetPtr = packet;

	int nBytesNeed = this->MaxBufflen;
	int nBytesRecv = 1;
	while (nBytesRecv > 0) {
		Log((char*) "ready to recv");
		nBytesRecv = recv(httpEntity->socketFD, packetPtr, this->MaxBufflen, 0);
		*(packetPtr + nBytesRecv) = 0;
		if (nBytesRecv <= 0) {
			if (nBytesRecv == -1) {
				if (errno == EAGAIN || errno == EWOULDBLOCK) {
//					printf(" recv finish detected, quit.../n ");
					Log((char*) " recv finish detected, quit.../n");
//					parseResponseBody(packetPtr);
					break;
				}
			} else {
				break;
			}
		}

		Log((char*) "received<<<<<<<<<<<<<<<<<<<", strlen(packetPtr));
	}
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
//		Log((char*) "epollLooper events");

		for (int i = 0; i < numEvents; ++i) {
			epoll_event * event = this->epoll_events + i;
			Log((char*) "resolve event>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", event->events);
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
			if (event->events & EPOLLHUP) {
				Log((char *) ("event: EPOLLHUP"));
			}
			if (event->events & EPOLLERR) {
				Log((char *) ("event: EPOLLERR"));
			}
			if (event->events & EPOLLIN) //接收到数据，读socket
			{
//				Log((char*)"resolve event  接收到数据");
//				recvPacket(this->epoll_events[i].data.fd);
				HttpEntity * httpEntity = (HttpEntity *) this->httpEntitiesMap->get(event->data.fd);
				if (httpEntity != NULL) {
					if (httpEntity->status->state == httpEntity->status->Waiting || httpEntity->status->state == httpEntity->status->receiving) {
						this->receivePackage(httpEntity);
					}
//					this->setState(httpEntity, httpEntity->status->receiving);
//					this->setState(httpEntity, httpEntity->status->received);
				}
			}
			if (event->events & EPOLLOUT) {
				HttpEntity * httpEntity = (HttpEntity *) this->httpEntitiesMap->get(event->data.fd);
				if (httpEntity != NULL) {
					if (httpEntity->status->state == httpEntity->status->Connecting) {
						int error = 0;
						socklen_t ilen = sizeof(error);
						int ret = getsockopt(event->data.fd, SOL_SOCKET, SO_ERROR, &error, &ilen);
						if (ret < 0) {
							//说明链接建立失败，close(fd);
							Log((char *) ("连接失败！1"));
						} else if (error != 0) {
							//说明链接建立失败，close(fd);
							Log((char *) ("连接失败！2"), error);
						} else {
							//说明链接建立成功。即可以向fd上写数据。
							Log((char *) ("连接成功！"));
							this->setState(httpEntity, httpEntity->status->Connected);
						}
					}
					if (httpEntity->status->state == httpEntity->status->Connected || httpEntity->status->state == httpEntity->status->Sending) {
						httpEntity->isSocketBufferFull = false;
						this->sendPackeges(httpEntity);
					}
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
void OpenHttp::setState(HttpEntity * httpEntity, int state) {
	httpEntity->status->state = state;

}
