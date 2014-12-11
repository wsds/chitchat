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

	this->httpEntitiedOldQueue = new Queue();
	this->httpEntitiedOldQueue->initialize();

	this->lineKey = (char *) JSMalloc(50 * sizeof(char));
	this->lineValue = (char *) JSMalloc(50 * sizeof(char));

	this->epollFD = epoll_create(1024);

	epollLooperPthread = new pthread_t();
	int ret = pthread_create(epollLooperPthread, NULL, epollLooperThread, (void *) 1);
	sleep(1);
	return true;
}

int OpenHttp::openSend(char * ip, int remotePort, char * buffer) {

	HttpEntity * httpEntity = this->getNewHttpEntity();
	httpEntity->ip = (const char *) ip;
	httpEntity->remotePort = remotePort;
	httpEntity->sendData = buffer;

	this->openSend(httpEntity);

//TO DO reuse the httpEntity
	return 1;
}
int OpenHttp::openDownload(char * ip, int remotePort, char * head, char * body, char * path) {

	HttpEntity * httpEntity = this->getNewHttpEntity();
	httpEntity->ip = (const char *) ip;
	httpEntity->remotePort = remotePort;
	httpEntity->sendData = head;

	httpEntity->receiveFD = open(path, O_CREAT | O_RDWR, 777);
	if (httpEntity->receiveFD < 0) {
		Log((char *) ("Download File,Can not open !"));
		this->setState(httpEntity, httpEntity->status->Failed);
		return 0;
	}

	ftruncate(httpEntity->receiveFD, 1);

	this->openSend(httpEntity);
	return 1;
}
int OpenHttp::openUpload(char * ip, int remotePort, char * head, char * body, char * path) {
	HttpEntity * httpEntity = this->getNewHttpEntity();
	httpEntity->ip = (const char *) ip;
	httpEntity->remotePort = remotePort;
	httpEntity->sendData = body;
	httpEntity->sendFD = open(path, O_RDWR, 777);
	if (httpEntity->sendFD < 0) {
		Log((char *) ("Upload File,Can not open !"));
		this->setState(httpEntity, httpEntity->status->Failed);
		return 0;
	}

	this->openSend(httpEntity);

	return 1;
}

void OpenHttp::openSend(HttpEntity * httpEntity) {
	JSObject * port = this->portPool->pop();

	if (port == NULL) {
		this->httpEntitiesQueue->offer(httpEntity);
		this->setState(httpEntity, httpEntity->status->Queueing);
	} else {
		this->setState(httpEntity, httpEntity->status->Started);
		httpEntity = this->intializeHttpEntity(httpEntity, port);
		this->httpEntitiesMap->set(httpEntity->socketFD, httpEntity);
		this->startConnect(httpEntity);
	}
}

HttpEntity * OpenHttp::getNewHttpEntity() {
	HttpEntity * httpEntity = (HttpEntity *) this->httpEntitiedOldQueue->take();
	if (httpEntity == NULL) {
		httpEntity = new HttpEntity();
	}
	return httpEntity;
}

void OpenHttp::nextHttpEntity() {
	if (this->portPool->length > 0 && this->httpEntitiesQueue->length > 0) {
		JSObject * port = this->portPool->pop();
		if (port != NULL) {
			HttpEntity * httpEntity = (HttpEntity *) this->httpEntitiesQueue->take();
			this->openSend(httpEntity, port);
		}
	}
}

void OpenHttp::openSend(HttpEntity * httpEntity, JSObject * port) {
	this->setState(httpEntity, httpEntity->status->Started);
	httpEntity = this->intializeHttpEntity(httpEntity, port);
	this->httpEntitiesMap->set(httpEntity->socketFD, httpEntity);
	this->startConnect(httpEntity);
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

//	httpEntity->socketFD = socketFD;
	httpEntity->localAddress = new sockaddr_in();
	memset(httpEntity->localAddress, 0, sizeof(struct sockaddr_in));
	httpEntity->localAddress->sin_family = AF_INET;
	httpEntity->localAddress->sin_port = htons(httpEntity->localPort->number);
	httpEntity->localAddress->sin_addr.s_addr = INADDR_ANY;

	setsockopt(httpEntity->socketFD, SOL_SOCKET, SO_REUSEADDR, &(this->isReUsedPort), sizeof(int));
	setsockopt(httpEntity->socketFD, SOL_SOCKET, SO_SNDBUF, &this->sendBuffSize, sizeof(int));

	//超时时间
	//	struct timeval timeout = { 10, 0 };
	//设置发送超时
	//	setsockopt(httpEntity->socketFD, SOL_SOCKET, SO_SNDTIMEO, (char *) &timeout, sizeof(timeout));
	//设置接收超时
	//	setsockopt(httpEntity->socketFD, SOL_SOCKET, SO_RCVTIMEO, (char *) &timeout, sizeof(timeout));

	if (-1 == bind(httpEntity->socketFD, (sockaddr *) httpEntity->localAddress, sizeof(sockaddr_in))) {
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
	httpEntity->sendDataLength = strlen(httpEntity->sendData);
	httpEntity->sentLength = 0;

	httpEntity->sendPackegesNum = httpEntity->sendDataLength / this->PackegeSize;
	httpEntity->sendLastPackegeSize = httpEntity->sendDataLength % this->PackegeSize;
	if (httpEntity->sendLastPackegeSize != 0) {
		httpEntity->sendPackegesNum = httpEntity->sendDataLength / this->PackegeSize + 1;
	}

	httpEntity->receiveBuffer = (char *) JSMalloc(10240 * sizeof(char));

	httpEntity->receivePackagesNumber = 0;
	httpEntity->receivedLength = 0;
	httpEntity->receiveContentLength = 0;
	httpEntity->receiveHeadLength = 0;
	httpEntity->receiveOffset = 0;
	httpEntity->receiveFileBuffer == NULL;

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
	if (httpEntity->sendPackegesNum <= 0 || httpEntity->sentLength >= httpEntity->sendDataLength) {
		if (httpEntity->sentLength >= httpEntity->sendDataLength) {
			this->setState(httpEntity, httpEntity->status->Waiting);
		}
		return;
	}
	this->setState(httpEntity, httpEntity->status->Sending);
	char * buffer = httpEntity->sendData + httpEntity->sentLength;

	for (int i = httpEntity->sentLength / this->PackegeSize; i < httpEntity->sendPackegesNum - 1; i++) {

		int sentPackegeLength = this->sendPackege(httpEntity, buffer, this->PackegeSize);

		if (httpEntity->isSendBufferFull) {
			return;
		}
		httpEntity->sentLength += sentPackegeLength;
		buffer = buffer + this->PackegeSize;
	}
	if (httpEntity->sendLastPackegeSize != 0) {
		httpEntity->sentLength += this->sendPackege(httpEntity, buffer, httpEntity->sendLastPackegeSize);
	} else {
		httpEntity->sentLength += this->sendPackege(httpEntity, buffer, this->PackegeSize);
	}
	if (httpEntity->sentLength >= httpEntity->sendDataLength) {
		this->setState(httpEntity, httpEntity->status->Sent);
		Log((char *) ("发送完成"));
	}
}

int OpenHttp::sendPackege(HttpEntity * httpEntity, const void * buffer, int PackegeSize) {
	Log((char*) "send one Packege");
	int sentPackegeLength = send(httpEntity->socketFD, buffer, PackegeSize, 0);
	if (sentPackegeLength == -1) {
		if (errno == EAGAIN) {
			httpEntity->isSendBufferFull = true;
			Log((char*) "缓冲区已满");

		} else if (errno == ECONNRESET) {
			// 对端重置,对方发送了RST
			Log((char *) ("sendPackege errno == ECONNRESET"));
		} else if (errno == EINTR) {
			// 被信号中断
			Log((char *) ("sendPackege errno == EINTR"));
		} else {
			Log((char *) ("sendPackege errno == other"));
			sentPackegeLength = 0;
		}
		sentPackegeLength = 0;
	}
	return sentPackegeLength;
}
void OpenHttp::receivePackage(HttpEntity * httpEntity) {
	int receiveLength = 1;
	while (receiveLength > 0) {
		receiveLength = 0;
		if (httpEntity->receivePackagesNumber == 0) {
			receiveLength = recv(httpEntity->socketFD, httpEntity->receiveBuffer, this->MaxBufflen, 0);
			if (checkReceive(httpEntity, receiveLength)) {
				this->setState(httpEntity, httpEntity->status->Receiving);
				HashTable * hashTable = this->parseResponseHead(httpEntity->receiveBuffer, receiveLength);
				bool flag = setReceiceHead(httpEntity, hashTable);
				if (!flag) {
					this->setState(httpEntity, httpEntity->status->Failed);
					break;
				}
			} else {
				break;
			}
			httpEntity->receivePackagesNumber++;
			httpEntity->receivedLength += receiveLength;
		} else {
			Log(httpEntity->receiveBuffer);
			if (httpEntity->receivedLength >= httpEntity->receiveContentLength + httpEntity->receiveHeadLength) {
				this->unMapReceiveFile(httpEntity);
				this->setState(httpEntity, httpEntity->status->Received);
				Log((char *) ("接收完成"));
				break;
			}
			this->mapReceiveFile(httpEntity);
			int length = httpEntity->receiveContentLength + httpEntity->receiveHeadLength - httpEntity->receivedLength;
			receiveLength = recv(httpEntity->socketFD, httpEntity->receiveFileBuffer + httpEntity->receivedLength - httpEntity->receiveHeadLength, length, 0);
			if (!checkReceive(httpEntity, receiveLength)) {
				break;
			}
			this->setState(httpEntity, httpEntity->status->Receiving);
			httpEntity->receivePackagesNumber++;
			httpEntity->receivedLength += receiveLength;
		}
	}
}

void OpenHttp::unMapReceiveFile(HttpEntity * httpEntity) {
	if (httpEntity->receiveFileBuffer != NULL) {
		JSFree(httpEntity->receiveFileBuffer, httpEntity->receiveContentLength, httpEntity->receiveFD, httpEntity->receiveContentLength);
		httpEntity->receiveFileBuffer = NULL;
	}
}
void OpenHttp::mapReceiveFile(HttpEntity * httpEntity) {
	if (httpEntity->receiveFileBuffer == NULL) {
		httpEntity->receiveFileBuffer = (char *) JSMalloc(httpEntity->receiveContentLength, httpEntity->receiveFD, httpEntity->receiveOffset);
		memcpy(httpEntity->receiveFileBuffer, httpEntity->receiveBuffer + httpEntity->receiveHeadLength, httpEntity->receivedLength - httpEntity->receiveHeadLength);
	}
}
bool OpenHttp::checkReceive(HttpEntity * httpEntity, int receiveLength) {
	if (receiveLength <= 0) {
		if (receiveLength == -1) {
			if (errno == EAGAIN || errno == EWOULDBLOCK) {
				Log((char*) " recv finish detected, quit.../n");
				return false;
			} else {
				Log((char *) ("ERRNO:"), errno);
				return false;
			}
		} else {
			if (receiveLength == 0) {
				//TODO 超时处理
				Log((char *) ("tcp 超时."));
				Log((char *) "error:*zero*");
			} else {
				Log((char *) "error:", receiveLength);
			}
			this->setState(httpEntity, httpEntity->status->Failed);
			return false;
		}
	} else {
		return true;
	}
}

void * epollLooperThread(void *arg) {
	OpenHttp *openHttp = OpenHttp::getInstance();
	openHttp->epollLooper(openHttp->epollFD);
	return (void *) 1;
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
//				Log((char*)"resolve event  新建连接");
//				struct sockaddr clientAddress;
//				socklen_t clientaddrLen = (socklen_t) sizeof(clientAddress);
//				connectingSocketFD = accept(listeningSocketFD, &clientAddress, &(clientaddrLen));
//				epoll_event* clientEvent = new epoll_event();
//				clientEvent->data.fd = connectingSocketFD;
//				clientEvent->events = EPOLLIN | EPOLLET;
//				epoll_ctl(epollFD, EPOLL_CTL_ADD, connectingSocketFD, clientEvent);
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
//				recvPacket(this->epoll_events[i].sendData.fd);
				HttpEntity * httpEntity = (HttpEntity *) this->httpEntitiesMap->get(event->data.fd);
				if (httpEntity != NULL) {
					if (httpEntity->status->state == httpEntity->status->Waiting || httpEntity->status->state == httpEntity->status->Receiving) {
						this->receivePackage(httpEntity);
					} else {
						Log((char *) "Status：<<<", httpEntity->status->state);
					}
//					this->setState(httpEntity, httpEntity->status->receiving);
//					this->setState(httpEntity, httpEntity->status->received);
//					Log(httpEntity->status->state);
//					this->receivePackage(httpEntity);
				} else {
					Log((char *) ("HttpEntity NULL1"));
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
							Log("-------+++++-----------");
						}
					}
					Log("------------------");
					if (httpEntity->status->state == httpEntity->status->Connected || httpEntity->status->state == httpEntity->status->Sending) {
						httpEntity->isSendBufferFull = false;
						this->sendPackeges(httpEntity);
					}
				} else {
					Log((char *) ("HttpEntity NULL2"));
				}
			}

			{
				//其他的处理
//				Log((char*) "事件@");
//				char target[15] = "";
//				parseNubmerToString(this->epoll_events[i].events, target);
//				Log((char *) ("other:>>>"), target);
			}
		}
	}
}

void OpenHttp::setState(HttpEntity * httpEntity, int state) {
	httpEntity->status->state = state;
	const signed char * buffer = (char *) ("A");
	if (httpEntity->status->state == httpEntity->status->Connected) {
		CallBack(httpEntity->status->state, buffer, buffer, 0);
	} else if (httpEntity->status->state == httpEntity->status->Sending) {
		CallBack(httpEntity->status->state, buffer, buffer, 0);
	} else if (httpEntity->status->state == httpEntity->status->Sent) {
		CallBack(httpEntity->status->state, buffer, buffer, 0);
	} else if (httpEntity->status->state == httpEntity->status->Receiving) {
		CallBack(httpEntity->status->state, buffer, buffer, 0);
	} else if (httpEntity->status->state == httpEntity->status->Received) {
//		this->closeSocketFd(httpEntity);
		const signed char * buffer1 = httpEntity->receiveBuffer + httpEntity->receiveHeadLength;
		char * etag = httpEntity->receivETag;
		if (etag == NULL) {
			etag = "B";
		}
		CallBack(httpEntity->status->state, buffer1, (const signed char *) etag, 0);
//		this->freeHttpEntity(httpEntity);
		this->nextHttpEntity();
	} else if (httpEntity->status->state == httpEntity->status->Failed) {
		CallBack(httpEntity->status->state, buffer, buffer, 0);
		this->closeSocketFd(httpEntity);
	}
}

void OpenHttp::closeSocketFd(HttpEntity * httpEntity) {
	close(httpEntity->socketFD);
	JSObject * localPort = httpEntity->localPort;
	this->portPool->push(localPort);
}

bool OpenHttp::freeHttpEntity(HttpEntity * httpEntity) {
	if (httpEntity == NULL) {
		return false;
	}
	httpEntity->ip = NULL;
	httpEntity->remotePort = NULL;
	httpEntity->localPort = NULL;
	httpEntity->sendData = NULL;

	httpEntity->socketFD = NULL;
	httpEntity->remoteAddress = NULL;
	httpEntity->localAddress = NULL;

	httpEntity->event = NULL;

	httpEntity->sendDataLength = 0;
	httpEntity->sentLength = 0;
	httpEntity->sendPackegesNum = 0;
	httpEntity->sendLastPackegeSize = 0;
	httpEntity->sentRuturnLength = 0;
	httpEntity->isSendBufferFull = false;
	Status * status = new Status();
	httpEntity->status->state = httpEntity->status->Queueing;

	//TODO free items
	httpEntity->responseHeadMap = new HashTable();
	httpEntity->responseHeadMap->initialize();

	httpEntity->receivePackagesNumber = 0;
	httpEntity->receivedLength = 0;
	httpEntity->receiveContentLength = 0;
	httpEntity->receiveHeadLength = 0;
	httpEntity->receivETag = NULL;

	httpEntity->receiveFD = NULL;
	httpEntity->receiveBuffer = NULL;
	httpEntity->receiveOffset = NULL;
	httpEntity->receiveFileBuffer = NULL;

	httpEntity->sendFD = NULL;
	httpEntity->sendOffser = NULL;
	httpEntity->sendFileBuffer = NULL;
	this->httpEntitiedOldQueue->offer(httpEntity);
	return true;
}
HashTable * OpenHttp::parseResponseHead(char * buffer, int length) {
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
					headMap->set(this->HeadLengthMark, jsObject);
					break;
				}
				lineNumber++;
				lastLine = point + 1;
			}
		}
	}
	if (lineNumber <= 0) {
		return NULL;
	}

	return headMap;
}

void OpenHttp::resolveLine(char * start, int length, int lineNumber, HashTable * headMap) {
	char * point = start;
	bool isKeyValue = false;
	for (int i = 0; i < length; i++) {
		point++;
		if (*point == 58) {
			isKeyValue = true;
			strcopy(start, this->lineKey, i + 1);
			if (strcmp(this->lineKey, this->ContentLengthMark) == 0) {
				strcopy(start + i + 1, this->lineValue, length - i - 1);
				int content_Length = parseStringToNubmer(this->lineValue, length - i - 1);
				JSObject * jsObject = new JSObject();
				jsObject->number = content_Length;
				headMap->set(this->ContentLengthMark, jsObject);
			} else if (strcmp(this->lineKey, this->ETagMark) == 0) {
				char * etag_string = (char *) JSMalloc(50 * sizeof(char));
				strcopy(start + i + 1, etag_string, length - i - 1);
				JSObject * jsObject = new JSObject();
				jsObject->char_string = etag_string;
				headMap->set(this->ETagMark, jsObject);
			}
		}
	}
	if (isKeyValue == false) {
		strcopy(start, this->lineKey, 4);
		if (strcmp(this->lineKey, this->HttpMark) == 0) {
			JSObject * jsObject = new JSObject();
			jsObject->number = 1;
			headMap->set(this->HttpMark, jsObject);
		}
	}
}

bool OpenHttp::setReceiceHead(HttpEntity * httpEntity, HashTable * headMap) {
	JSObject * jsObject = headMap->get(HttpMark);
	if (jsObject == NULL) {
		return false;
	}

	jsObject = headMap->get(ContentLengthMark);
	if (jsObject == NULL) {
		return false;
	} else {
		httpEntity->receiveContentLength = jsObject->number;
//		Log((char *) ("ContentLengthMark"), jsObject->number);
	}
	jsObject = headMap->get(HeadLengthMark);
	if (jsObject == NULL) {
		return false;
	} else {
		httpEntity->receiveHeadLength = jsObject->number;
//		Log((char *) ("receiveHeadLength"), jsObject->number);
	}
	jsObject = headMap->get(ETagMark);
	if (jsObject == NULL) {
	} else {
		httpEntity->receivETag = jsObject->char_string;
//		Log((char *) ("receivETag"), jsObject->char_string);
	}
	return true;
}
