#include "OpenHttp.h"

OpenHttp * OpenHttp::instance = NULL;

bool OpenHttp::initialize() {

	Log((char *) "OpenHttp initialize");
	if (this->is_initialized == true) {
		return true;
	}
	this->is_initialized = true;

	this->portPool = new Queue();
	this->portPool->initialize();

	for (int i = 0; i < portPoolSize; i++) {
		JSObject * port = new JSObject();
		port->number = startPortNumber + i;
		this->portPool->offer(port);
	}

	this->httpEntitiesMap = new HashTable();
	this->httpEntitiesMap->initialize();

	this->httpEntitiesQueue = new Queue();
	this->httpEntitiesQueue->initialize();

	this->httpEntitiesIdMap = new HashTable();
	this->httpEntitiesIdMap->initialize();

	this->httpEntitiedOldQueue = new Queue();
	this->httpEntitiedOldQueue->initialize();

	this->lineKey = (char *) JSMalloc(50 * sizeof(char));
	this->lineValue = (char *) JSMalloc(50 * sizeof(char));

	this->iTimeval = new timeval();
	gettimeofday(this->iTimeval, NULL);
	this->start_time = this->iTimeval->tv_sec;

	this->i_tcp_info = new tcp_info();
	this->tcp_info_length = sizeof(tcp_info);

	this->epollFD = epoll_create(1024);

	epollLooperPthread = new pthread_t();
	int ret = pthread_create(epollLooperPthread, NULL, epollLooperThread, (void *) 1);
	sleep(1);
	return true;
}

int OpenHttp::openSend(char * ip, int remotePort, char * buffer, int length, int id) {

	HttpEntity * httpEntity = this->getNewHttpEntity();
	httpEntity->ip = (const char *) ip;
	httpEntity->remotePort = remotePort;
	httpEntity->sendData = buffer;
	httpEntity->sendDataLength = length;
	httpEntity->id = id;
	httpEntity->type = 0;

	this->openSend(httpEntity);

//TO DO reuse the httpEntity
	return 1;
}

int OpenHttp::openUpload(char * ip, int remotePort, char * head, char * path, int id, int head_length, int start, int length) {
	Log((char *) "openUpload");
	HttpEntity * httpEntity = this->getNewHttpEntity();
	httpEntity->ip = (const char *) ip;
	httpEntity->remotePort = remotePort;
	httpEntity->sendData = head;
	httpEntity->sendDataLength = head_length;
	httpEntity->path = path;
	httpEntity->id = id;
	httpEntity->sendFileStart = start;
	httpEntity->sendFileLength = length;
	httpEntity->type = 1;

	httpEntity->sendFD = open(path, O_RDWR, 777);
	if (httpEntity->sendFD < 0) {
		Log((char *) ("Upload File,Can not open !"));
		this->setState(httpEntity, httpEntity->status->Failed);
		return 0;
	}

	httpEntity->sendFileBuffer = (char *) mmap(NULL, httpEntity->sendFileLength, PROT_READ, MAP_SHARED, httpEntity->sendFD, httpEntity->sendFileStart);
	if ((httpEntity->sendFileBuffer) == (void *) -1) {
		Log((char*) ("httpEntity->sendFileBuffer mmap Failed"));
		Log(httpEntity->sendFileStart);
		Log(httpEntity->sendFileLength);
		this->setState(httpEntity, httpEntity->status->Failed);
		return 0;
	}

	this->openSend(httpEntity);

	return 1;
}

int OpenHttp::openDownload(char * ip, int remotePort, char * body, char * path, int id, int length) {

	Log((char*) "openDownload");

	HttpEntity * httpEntity = this->getNewHttpEntity();
	httpEntity->ip = (const char *) ip;
	httpEntity->remotePort = remotePort;
	httpEntity->sendData = body;
	httpEntity->sendDataLength = length;
	httpEntity->id = id;
	httpEntity->path = path;
	httpEntity->type = 2;

	httpEntity->receiveFD = open(path, O_CREAT | O_RDWR, 777);

	if (httpEntity->receiveFD < 0) {
		Log((char *) ("Download File,Can not open !"));
		Log((char *) "errno:", errno);
		this->setState(httpEntity, httpEntity->status->Failed);
		return 0;
	}

	ftruncate(httpEntity->receiveFD, 1);

	this->openSend(httpEntity);
	return 1;
}

int OpenHttp::openLongPull(char * ip, int remotePort, char * buffer, int length, int id) {
	HttpEntity * httpEntity = this->getNewHttpEntity();
	httpEntity->ip = (const char *) ip;
	httpEntity->remotePort = remotePort;
	httpEntity->sendData = buffer;
	httpEntity->sendDataLength = length;
	httpEntity->id = id;
	httpEntity->type = 3;

	this->openSend(httpEntity);
	return 1;
}

void OpenHttp::openSend(HttpEntity * httpEntity) {
	Log((char*) "openSend");
	this->setState(httpEntity, httpEntity->status->Queueing);
	this->httpEntitiesQueue->offer(httpEntity);
	this->openSendQueue();
}

void OpenHttp::openSendQueue() {
	Log((char*) "openSendQueue");
	bool isEmpty = false;
	while (!isEmpty) {
		if (this->portPool->length > 0 && this->httpEntitiesQueue->length > 0) {
			JSObject * port = this->portPool->take();
			if (port != NULL) {
				HttpEntity * httpEntity = (HttpEntity *) this->httpEntitiesQueue->take();
				if (httpEntity != NULL) {
					this->openSendHttpEntity(httpEntity, port);
				} else {
					isEmpty = true;
				}
			} else {
				Log((char *) "PORT NULL");
				isEmpty = true;
			}
		} else {
			isEmpty = true;
		}
	}
}

void OpenHttp::openSendHttpEntity(HttpEntity * httpEntity, JSObject * port) {
	Log((char*) "openSendHttpEntity");
	this->setState(httpEntity, httpEntity->status->Started);
	httpEntity = this->intializeHttpEntity(httpEntity, port);
	this->httpEntitiesMap->set(httpEntity->socketFD, httpEntity);
	this->httpEntitiesIdMap->set(httpEntity->id, httpEntity);
	this->startConnect(httpEntity);
}

HttpEntity * OpenHttp::getNewHttpEntity() {
	HttpEntity * httpEntity = (HttpEntity *) this->httpEntitiedOldQueue->take();
	if (httpEntity == NULL) {
		httpEntity = new HttpEntity();
	}
	return httpEntity;
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
	httpEntity->localAddress->sin_addr.s_addr = htonl(INADDR_ANY); //INADDR_LOOPBACK

	setsockopt(httpEntity->socketFD, SOL_SOCKET, SO_REUSEADDR, &(this->isReUsedPort), sizeof(int));

//	setsockopt(httpEntity->socketFD, SOL_SOCKET, SO_SNDBUF, &this->sendBuffSize, sizeof(int));

//	setsockopt(httpEntity->socketFD, SOL_SOCKET, SO_RCVBUF, &this->sendBuffSize, sizeof(int));

//	struct tcp_info info;
//	int tcp_info_length = sizeof(info);
//	getsock / 0;opt(httpEntity->socketFD, SOL_TCP, TCP_INFO, (void *) &info, &tcp_info_length);

//超时时间
//	struct timeval timeout = { 10, 0 };
//设置发送超时
//	setsockopt(httpEntity->socketFD, SOL_SOCKET, SO_SNDTIMEO, (char *) &timeout, sizeof(timeout));
//设置接收超时
//	setsockopt(httpEntity->socketFD, SOL_SOCKET, SO_RCVTIMEO, (char *) &timeout, sizeof(timeout));

	if (-1 == bind(httpEntity->socketFD, (sockaddr *) httpEntity->localAddress, sizeof(sockaddr_in))) {
		Log((char*) "bind fail !");
		closeSocketFd(httpEntity);
//		int i = 10 / 0;
//		Log("bind>?????");
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
//	httpEntity->sendDataLength = 1443;
	httpEntity->sentLength = 0;

	if (httpEntity->type == 1) {
		httpEntity->sendPackegesNum = httpEntity->sendFileLength / this->PackegeSize + 1;
		httpEntity->sendLastPackegeSize = httpEntity->sendFileLength % this->PackegeSize;
		if (httpEntity->sendLastPackegeSize != 0) {
			httpEntity->sendPackegesNum = httpEntity->sendPackegesNum + 1;
		}
	} else {
		httpEntity->sendPackegesNum = httpEntity->sendDataLength / this->PackegeSize;
		httpEntity->sendLastPackegeSize = httpEntity->sendDataLength % this->PackegeSize;
		if (httpEntity->sendLastPackegeSize != 0) {
			httpEntity->sendPackegesNum = httpEntity->sendPackegesNum + 1;
		}
	}

	httpEntity->receiveBuffer = (char *) JSMalloc(this->MaxBufflen * sizeof(char));

	httpEntity->receivePackagesNumber = 0;
	httpEntity->receivedLength = 0;
	httpEntity->receiveContentLength = 0;
	httpEntity->receiveHeadLength = 0;
	httpEntity->receiveOffset = 0;
	httpEntity->receiveFileBuffer == NULL;

	// init json the of response headers
	httpEntity->receiveHeaders = new JSON();
	httpEntity->receiveHeaders->initialize();

	epoll_ctl(this->epollFD, EPOLL_CTL_ADD, httpEntity->socketFD, httpEntity->event);

	int status = connect(httpEntity->socketFD, (sockaddr *) httpEntity->remoteAddress, sizeof(sockaddr_in));

	if (status != 0) {
		if (errno == EINPROGRESS) {
			this->setState(httpEntity, httpEntity->status->Connecting);
			Log((char*) "正在连接");
		} else { //EADDRNOTAVAIL 99
			Log((char*) "Connect fail!");
			Log((char *) "connect errno:", errno);
//			closeSocketFd(httpEntity);
//			this->httpEntitiesQueue->offer(httpEntity);
//			this->portPool->offer(httpEntity->localPort);
			int i = 10 / 0;
			Log(i);
			return 0;
		}
	}
	Log((char *) "ip******", (char *) httpEntity->ip);
	Log((char *) "port******", httpEntity->remotePort);
	Log((char *) "localport******", httpEntity->localPort->number);
	return 1;
}

void OpenHttp::sendPackeges(HttpEntity * httpEntity) {
	Log((char*) "sendPackeges");

	this->setState(httpEntity, httpEntity->status->Sending);

	char * buffer = httpEntity->sendData + httpEntity->sentLength;

	while (httpEntity->sentLength < httpEntity->sendDataLength) {

		int size = this->PackegeSize;
		if ((this->PackegeSize + httpEntity->sentLength) > httpEntity->sendDataLength) {
			size = httpEntity->sendDataLength % this->PackegeSize;
			if (size == 0) {
				break;
			}
		}

		int sentPackegeLength = this->sendPackege(httpEntity, buffer, size);

		if (httpEntity->isSendBufferFull) {
			break;
		}
		httpEntity->sentLength += sentPackegeLength;
		buffer = buffer + size;
		httpEntity->send_percent = (float) httpEntity->sentLength / (httpEntity->sendDataLength + httpEntity->sendFileLength);
	}

	if (httpEntity->sentLength != httpEntity->sendDataLength) {
		//report error
	}
	if (httpEntity->type != 1) {
		if (httpEntity->sentLength >= (httpEntity->sendDataLength + httpEntity->sendFileLength)) {
			this->setState(httpEntity, httpEntity->status->Sent);
			Log((char *) ("发送完成"), httpEntity->sentLength);
		}
		return;
	}

	buffer = httpEntity->sendFileBuffer + httpEntity->sentLength - httpEntity->sendDataLength;

	while (httpEntity->sentLength < (httpEntity->sendDataLength + httpEntity->sendFileLength)) {

		int size = this->PackegeSize;
		if ((this->PackegeSize + httpEntity->sentLength) > (httpEntity->sendDataLength + httpEntity->sendFileLength)) {
			size = httpEntity->sendFileLength % this->PackegeSize;
			if (size == 0) {
				break;
			}
		}

		int sentPackegeLength = this->sendPackege(httpEntity, buffer, size);

		if (httpEntity->isSendBufferFull) {
			break;
		}
		httpEntity->sentLength += sentPackegeLength;
		buffer = buffer + size;
		httpEntity->send_percent = (float) httpEntity->sentLength / (httpEntity->sendDataLength + httpEntity->sendFileLength);
	}

	if (httpEntity->sentLength >= (httpEntity->sendDataLength + httpEntity->sendFileLength)) {
		this->setState(httpEntity, httpEntity->status->Sent);
		Log((char *) ("发送完成"), httpEntity->sentLength);
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
		} else if (errno == EPIPE) {
			//关闭socket后，进入wait状态，继续读写就会出现该异常，解决方法如下
//			shutdown(httpEntity->socketFD, SHUT_RDWR);
		} else {
			Log((char *) ("sendPackege errno == other"), errno);
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
			bool flag = false;
			receiveLength = recv(httpEntity->socketFD, httpEntity->receiveBuffer, this->MaxBufflen, 0);
			Log(httpEntity->receiveBuffer);
			if (checkReceive(httpEntity, receiveLength)) {
				this->setState(httpEntity, httpEntity->status->Receiving);
				HashTable * hashTable = this->parseResponseHead(httpEntity->receiveBuffer, receiveLength);
				flag = setReceiceHead(httpEntity, hashTable);
				if (!flag) {
					Log((char*) "XXXXXXXXXXXXXXXXXXXXXXXX");
					this->setState(httpEntity, httpEntity->status->Failed);
					break;
				}
			} else {
				break;
			}
			httpEntity->receivePackagesNumber++;
			httpEntity->receivedLength += receiveLength;

			httpEntity->receive_percent = (float) (httpEntity->receivedLength - httpEntity->receiveHeadLength) / httpEntity->receiveContentLength;
		} else {
//			Log(httpEntity->receiveBuffer);
			if (httpEntity->receivedLength >= httpEntity->receiveContentLength + httpEntity->receiveHeadLength) {
				if (httpEntity->type == 2) {
					this->unMapReceiveFile(httpEntity);
				}
				this->setState(httpEntity, httpEntity->status->Received);
				Log((char *) ("接收完成"));
				break;
			}

			int length = httpEntity->receiveContentLength + httpEntity->receiveHeadLength - httpEntity->receivedLength;

			if (httpEntity->type == 2) {
				this->mapReceiveFile(httpEntity);
				char* receiveBuffer = httpEntity->receiveFileBuffer + httpEntity->receivedLength - httpEntity->receiveHeadLength;
				receiveLength = recv(httpEntity->socketFD, receiveBuffer, length, 0);
			} else {
				mapReceiveData(httpEntity);
				char* receiveBuffer = httpEntity->receiveDataBuffer + httpEntity->receivedLength - httpEntity->receiveHeadLength;
				receiveLength = recv(httpEntity->socketFD, receiveBuffer, length, 0);
			}

			if (!checkReceive(httpEntity, receiveLength)) {
				break;
			}

			httpEntity->receivePackagesNumber++;
			httpEntity->receivedLength += receiveLength;
			httpEntity->receive_percent = (float) (httpEntity->receivedLength - httpEntity->receiveHeadLength) / httpEntity->receiveContentLength;
			this->setState(httpEntity, httpEntity->status->Receiving);
		}
	}
}

void OpenHttp::mapReceiveData(HttpEntity * httpEntity) {
	if (httpEntity->receiveDataBuffer == NULL) {
		httpEntity->receiveDataBuffer = (char *) JSMalloc(httpEntity->receiveContentLength);
		memcpy(httpEntity->receiveDataBuffer, httpEntity->receiveBuffer + httpEntity->receiveHeadLength, httpEntity->receivedLength - httpEntity->receiveHeadLength);
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
				Log((char *) ("tcp 超时."));
				Log((char *) "error:*zero*");
				if (httpEntity->type == 3) {
					//TODO TCP timeout
				}
			} else {
				Log((char *) "error:", receiveLength);
			}
			Log((char*) "YYYYYYYYYYYYYYYYYYYYYY");
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
//				HttpEntity * httpEntity = (HttpEntity *) this->httpEntitiesMap->get(event->data.fd);
//				if (httpEntity != NULL) {
//					closeSocketFd(httpEntity);
//					this->httpEntitiesQueue->offer(httpEntity);
//					this->nextHttpEntity();
//				}
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
//					httpEntity->status->state == httpEntity->status->Sent ||
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
						}
					}
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
	if (httpEntity->status->state == state) {
		return;
	}
	httpEntity->status->state = state;
	const signed char * buffer = (const signed char *) ("");
	if (httpEntity->status->state == httpEntity->status->Queueing) {
		httpEntity->status->time_queueing = this->getCurrentMillisecond();

	} else if (httpEntity->status->state == httpEntity->status->Started) {
		httpEntity->status->time_started = this->getCurrentMillisecond();

	} else if (httpEntity->status->state == httpEntity->status->Connecting) {
		httpEntity->status->time_connecting = this->getCurrentMillisecond();

	} else if (httpEntity->status->state == httpEntity->status->Connected) {
		httpEntity->status->time_connected = this->getCurrentMillisecond();

	} else if (httpEntity->status->state == httpEntity->status->Sending) {
		httpEntity->status->time_sending = this->getCurrentMillisecond();

	} else if (httpEntity->status->state == httpEntity->status->Sent) {
		httpEntity->status->time_sent = this->getCurrentMillisecond();

	} else if (httpEntity->status->state == httpEntity->status->Receiving) {
		httpEntity->status->time_receiving = this->getCurrentMillisecond();

	} else if (httpEntity->status->state == httpEntity->status->Received) {
		httpEntity->status->time_received = this->getCurrentMillisecond();

		if (httpEntity->type != 1 && httpEntity->type != 2) {
			char * data = (char *) (httpEntity->receiveBuffer + httpEntity->receiveHeadLength);
			JSKeyValue * jskeyvalue = new JSKeyValue();
			jskeyvalue->key = (char *) "result";
			JSString * jsstring = new JSString(data);
			jskeyvalue->value = jsstring;
			httpEntity->receiveHeaders->push(jskeyvalue);
//			char * base64 = this->base64_encode(data, strlen(data));
//			Log("base64:", base64);
		}
		const signed char * responseInfo = (const signed char *) stringifyJSON(httpEntity->receiveHeaders);
		CallBack(httpEntity->id, httpEntity->status->state, responseInfo, httpEntity->partId);
		if (httpEntity->type != 3) {
			this->closeSocketFd(httpEntity);
		}
		this->onEndConnect(httpEntity);

	} else if (httpEntity->status->state == httpEntity->status->Failed) {
		httpEntity->status->time_failed = this->getCurrentMillisecond();

	} else if (httpEntity->status->state == httpEntity->status->Timeout) {
		httpEntity->status->time_timeout = this->getCurrentMillisecond();
		closeSocketFd(httpEntity);

	}
}
void OpenHttp::onEndConnect(HttpEntity * httpEntity) {
	JSObject * localPort = httpEntity->localPort;
	this->portPool->offer(localPort);
	this->openSendQueue();
}

void OpenHttp::closeSocketFd(HttpEntity * httpEntity) {
//  直接关闭socket
//	linger m_sLinger;
//	m_sLinger.l_onoff = 0;
//	m_sLinger.l_linger = 0;
//	setsockopt(httpEntity->socketFD, SOL_SOCKET, SO_LINGER, (const char*) &m_sLinger, sizeof(linger));
	//关闭socket的读写
//	shutdown(httpEntity->socketFD, SHUT_RDWR);
	free(httpEntity->receiveBuffer);
	free(httpEntity->receiveDataBuffer);
	free(httpEntity->sendData);
	free(httpEntity->localAddress);
	free(httpEntity->remoteAddress);
	close(httpEntity->socketFD);
	close(httpEntity->sendFD);
	close(httpEntity->receiveFD);
//	JSObject * localPort = httpEntity->localPort;
//	this->portPool->offer(localPort);
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
				char * length_string = (char *) JSMalloc(50 * sizeof(char));
				strcopy(start + i + 3, length_string, length - i - 4);
				int content_Length = parseStringToNubmer(length_string, length - i - 1);
				JSObject * jsObject = new JSObject();
				jsObject->number = content_Length;
				jsObject->char_string = length_string;
				headMap->set(this->ContentLengthMark, jsObject);
			} else if (strcmp(this->lineKey, this->ETagMark) == 0) {
				char * etag_string = (char *) JSMalloc(50 * sizeof(char));

				strcopy(start + i + 4, etag_string, length - i - 6);
//				Log("----------------------------");
//				Log(etag_string);
//				int i = 10/0;
//				Log(i);
				JSObject * jsObject = new JSObject();
				jsObject->char_string = etag_string;
				headMap->set(this->ETagMark, jsObject);
			} else if (strcmp(this->lineKey, this->DateMark) == 0) {
//				char * date_string = (char *) JSMalloc(50 * sizeof(char));
//				strcopy(start + i + 3, date_string, length - i - 4);
//				JSObject * jsObject = new JSObject();
//				jsObject->char_string = date_string;
//				headMap->set(this->DateMark, jsObject);
			} //else if (strcmp(this->lineKey, this->ContentTypeMark) == 0) {
//				char * contentType_string = (char *) JSMalloc(50 * sizeof(char));
//				strcopy(start + i + 1, contentType_string, length - i - 1);
//				JSObject * jsObject = new JSObject();
//				jsObject->char_string = contentType_string;
//				headMap->set(this->ContentTypeMark, jsObject);
//			}
			else if (strcmp(this->lineKey, this->ServerMark) == 0) {
//				char * server_string = (char *) JSMalloc(50 * sizeof(char));
//				strcopy(start + i + 3, server_string, length - i - 4);
//				JSObject * jsObject = new JSObject();
//				jsObject->char_string = server_string;
//				headMap->set(this->ServerMark, jsObject);
			} else if (strcmp(this->lineKey, this->ConnectionMark) == 0) {
//				char * connection_string = (char *) JSMalloc(50 * sizeof(char));
//				strcopy(start + i + 3, connection_string, length - i - 4);
//				JSObject * jsObject = new JSObject();
//				jsObject->char_string = connection_string;
//				headMap->set(this->ConnectionMark, jsObject);
			}
		}
	}
	if (isKeyValue == false) {
		strcopy(start, this->lineKey, 4);
		if (strcmp(this->lineKey, this->HttpMark) == 0) { // Format : HTTP/1.1 404 Not Found    Status Code
			char * server_string = (char *) JSMalloc(50 * sizeof(char));
			strcopy(start + 9, server_string, 3);

			JSObject * jsObject = new JSObject();
			jsObject->number = 1;
			headMap->set(this->HttpMark, jsObject);

			JSObject * char_jsObject = new JSObject();
			char_jsObject->char_string = server_string;
			headMap->set(this->StatusCode, char_jsObject);
		}
	}
}

bool OpenHttp::setReceiceHead(HttpEntity * httpEntity, HashTable * headMap) {
	JSObject * jsObject = headMap->get(this->HttpMark);
	if (jsObject == NULL) {
		Log((char *) "HttpMark");
		return false;
	}

	jsObject = headMap->get(this->StatusCode);
	if (jsObject == NULL) {
		Log((char *) "StatusCode");
		return false;
	} else {
		JSKeyValue * jsKeyValue = new JSKeyValue();
		jsKeyValue->key = this->StatusCode;
		JSString * jsString = new JSString(jsObject->char_string);
		jsKeyValue->value = jsString;
		httpEntity->receiveHeaders->push(jsKeyValue);
	}

	jsObject = headMap->get(this->ContentLengthMark);
	if (jsObject == NULL) {
		Log((char *) "ContentLengthMark");
		return false;
	} else {
		httpEntity->receiveContentLength = jsObject->number;
		JSKeyValue * jsKeyValue = new JSKeyValue();
		jsKeyValue->key = this->ContentLengthMark;
		JSString * jsNumber = new JSString(jsObject->char_string);
		jsKeyValue->value = jsNumber;
		httpEntity->receiveHeaders->push(jsKeyValue);
	}
	jsObject = headMap->get(this->HeadLengthMark);
	if (jsObject == NULL) {
		Log((char *) "HeadLengthMark");
		return false;
	} else {
		httpEntity->receiveHeadLength = jsObject->number;
	}
	jsObject = headMap->get(this->ETagMark);
	if (jsObject == NULL) {
	} else {
		httpEntity->receivETag = jsObject->char_string;
		JSKeyValue * jsKeyValue = new JSKeyValue();
		jsKeyValue->key = this->ETagMark;
		JSString * jsString = new JSString(jsObject->char_string);
		jsKeyValue->value = jsString;
		httpEntity->receiveHeaders->push(jsKeyValue);
	}
	jsObject = headMap->get(this->DateMark);
	if (jsObject == NULL) {
	} else {
		JSKeyValue * jsKeyValue = new JSKeyValue();
		jsKeyValue->key = this->DateMark;
		JSString * jsString = new JSString(jsObject->char_string);
		jsKeyValue->value = jsString;
		httpEntity->receiveHeaders->push(jsKeyValue);
	}
	jsObject = headMap->get(this->ContentTypeMark);
	if (jsObject == NULL) {
	} else {
		JSKeyValue * jsKeyValue = new JSKeyValue();
		jsKeyValue->key = this->ContentTypeMark;
		JSString * jsString = new JSString(jsObject->char_string);
		jsKeyValue->value = jsString;
		httpEntity->receiveHeaders->push(jsKeyValue);
	}
	jsObject = headMap->get(this->ServerMark);
	if (jsObject == NULL) {
	} else {
		JSKeyValue * jsKeyValue = new JSKeyValue();
		jsKeyValue->key = this->ServerMark;
		JSString * jsString = new JSString(jsObject->char_string);
		jsKeyValue->value = jsString;
		httpEntity->receiveHeaders->push(jsKeyValue);
	}
	jsObject = headMap->get(this->ConnectionMark);
	if (jsObject == NULL) {
	} else {
		JSKeyValue * jsKeyValue = new JSKeyValue();
		jsKeyValue->key = this->ConnectionMark;
		JSString * jsString = new JSString(jsObject->char_string);
		jsKeyValue->value = jsString;
		httpEntity->receiveHeaders->push(jsKeyValue);
	}
	return true;
}

long OpenHttp::getCurrentMillisecond() {
	gettimeofday(this->iTimeval, NULL);

	long millisecond = (this->iTimeval->tv_sec - this->start_time) * 1000 + this->iTimeval->tv_usec / 1000;
	return millisecond;
}
