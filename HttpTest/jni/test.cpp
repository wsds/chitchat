#include <test.h>

void test321415() {
	Log((char *) "test321415");
	JSON * json = new JSON();
	json->initialize();
	char * jsString = stringifyJSON(json);
	Log(jsString);
	JSObject * jsobj = new JSObject();
	jsobj->type = 3;
//	jsobj->number = 10;
	jsobj->char_string = (char *) "ABC123";
	json->set((char *) "length", jsobj);
	jsString = stringifyJSON(json);
	Log(jsString);
}

void test001231() {

//	MySocket * mySocket = NULL;
//	void *sk = (void *)mySocket->sk;

//	Socket123 * iSocket;

//	void *sk = (void *)iSocket->sk;

	int socketFD = socket(AF_INET, SOCK_STREAM, 0);
	if (socketFD < 0) {
		return;
	}

	sockaddr_in * remoteAddress = new sockaddr_in();
	memset(remoteAddress, 0, sizeof(struct sockaddr_in));
	remoteAddress->sin_family = AF_INET;
	int remotePort = 80;
	remoteAddress->sin_port = htons(remotePort);
	const char * remoteIp = (const char *) ("192.168.1.7"); ///61.135.169.121
	remoteAddress->sin_addr.s_addr = inet_addr(remoteIp);

	sockaddr_in * localAddress = new sockaddr_in();
	memset(localAddress, 0, sizeof(struct sockaddr_in));
	localAddress->sin_family = AF_INET;
	int localPort = 8090;
	localAddress->sin_port = htons(localPort);
	localAddress->sin_addr.s_addr = htonl(INADDR_ANY);
	int isReUsedPort = 1;
	setsockopt(socketFD, SOL_SOCKET, SO_REUSEADDR,
			(const void *) &(isReUsedPort), sizeof(int));
//
//	setsockopt(socketFD, SOL_SOCKET, SO_SNDBUF, &(1024), sizeof(int));

//bind
	if (-1 == bind(socketFD, (sockaddr *) localAddress, sizeof(sockaddr_in))) {
		Log((char*) "bind fail !");
		return;
	}
	Log((char*) "bind OK !");
	//connect
	int status = connect(socketFD, (sockaddr *) remoteAddress,
			sizeof(sockaddr_in));

	if (status != 0) {
		if (errno == EINPROGRESS) {
			Log((char*) "正在连接");
		} else { //EADDRNOTAVAIL 99
			Log((char*) "Connect fail!");
			return;
		}
	}
	Log((char*) "Connect OK!");

	char * buffer =
			(char *) ("GET /index.html HTTP/1.1\r\nHost: 192.168.1.7\r\nConnection: keep-alive\r\nContent-Length: 0\r\n\r\n");
	int sentPackegeLength = send(socketFD, buffer, strlen(buffer), 0);

	tcp_info * info1 = new tcp_info();
	int tcp_info_length = sizeof(tcp_info);
	getsockopt(socketFD, SOL_TCP, TCP_INFO, (void *) &info1, &tcp_info_length);
	Log((char *) "tcpi_rtt*:  ", info1->tcpi_rtt);
	Log((char *) "tcpi_rttvar*:  ", info1->tcpi_rttvar);
	Log((char *) "tcpi_rcv_rtt*:  ", info1->tcpi_rcv_rtt);

	char * receiveBuffer = (char *) JSMalloc(1024 * sizeof(char));
	int receiveLength = recv(socketFD, receiveBuffer, 1024, 0);
	Log(receiveLength);

	tcp_info * info2 = new tcp_info();
	int tcp_info_length1 = sizeof(tcp_info);
	getsockopt(socketFD, SOL_TCP, TCP_INFO, (void *) info2, &tcp_info_length1);
	Log((char *) "tcpi_rtt:  ", info2->tcpi_rtt);
	Log((char *) "tcpi_rttvar:  ", info2->tcpi_rttvar);
	Log((char *) "tcpi_rcv_rtt:  ", info2->tcpi_rcv_rtt);

	Log((char *) "test001.....");
	close(socketFD);
}

void test001232() {
	timeval * tv = new timeval();
	gettimeofday(tv, NULL);
	Log(tv->tv_sec);
	Log(tv->tv_usec);

	long millisecond = (tv->tv_sec - 1418882358) * 1000 + tv->tv_usec / 1000;

	Log(millisecond);
}

void test321414() {
	Log((char *) ("test321414"));
	char* path = (char *) "/sdcard/welinks/test321414.txt";

	int receiveFD = open(path, O_CREAT | O_RDWR, 777);

	if (receiveFD < 0) {
		Log((char *) ("Download File,Can not open !"));
		Log((char *) "errno:", errno);
		return;
	}
	ftruncate(receiveFD, 4095);
	char *pointer;
	pointer = (char *) mmap(NULL, 50, PROT_READ | PROT_WRITE, MAP_SHARED,
			receiveFD, 0);

	if ((pointer) == (void *) -1) {
		return;
	}
//	memset(pointer, 0, size);
	Log((char *) ("test321414   001"));
	*(pointer) = 55;
	Log((char *) ("test321414   001.1"));
	*(pointer + 10) = 48;
	*(pointer + 11) = 58;
	Log((char *) ("test321414   002"));
	*(pointer + 2896) = 58;
	Log((char *) ("test321414   003"));
	*(pointer + 4095) = 58;
	Log((char *) ("test321414   003.003"));
	ftruncate(receiveFD, 4097);
	*(pointer + 4096) = 58;
	Log((char *) ("test321414   003.004"));
	ftruncate(receiveFD, 50000);
	*(pointer + 45000) = 58;
	Log((char *) ("test321414   004"));
	ftruncate(receiveFD, 64000);
	munmap(pointer, 64000);
	close(receiveFD);

}
