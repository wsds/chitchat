#include <test.h>

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

	struct tcp_info info1;
	int tcp_info_length = sizeof(info1);
	getsockopt(socketFD, SOL_TCP, TCP_INFO, (void *) &info1, &tcp_info_length);
	Log((char *) "tcpi_rtt*:  ", info1.tcpi_rtt);
	Log((char *) "tcpi_rttvar*:  ", info1.tcpi_rttvar);
	Log((char *) "tcpi_rcv_rtt*:  ", info1.tcpi_rcv_rtt);

	char * receiveBuffer = (char *) JSMalloc(1024 * sizeof(char));
	int receiveLength = recv(socketFD, receiveBuffer, 1024, 0);
	Log(receiveLength);

	struct tcp_info info2;
	int tcp_info_length1 = sizeof(info2);
	getsockopt(socketFD, SOL_TCP, TCP_INFO, (void *) &info2, &tcp_info_length1);
	Log((char *) "tcpi_rtt:  ", info2.tcpi_rtt);
	Log((char *) "tcpi_rttvar:  ", info2.tcpi_rttvar);
	Log((char *) "tcpi_rcv_rtt:  ", info2.tcpi_rcv_rtt);

	Log((char *) "test001.....");
	close(socketFD);
}

