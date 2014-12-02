#include <jni.h>
#include <stdlib.h>

#include "lib/Log.h"
#include "data_core/base/HashTable.h"
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include <sys/epoll.h>
#define MAXBUFLEN 	1024

void test(signed char * message);
static void make_sendipv4addr(struct sockaddr_in *addr, int remoteport);
int setSend(const char *ipAddr);
int sendPacket(int sockd, const char *str);
static void make_recvipv4addr(struct sockaddr_in *addr, const int localport);
int recvPacket(int sockd);
static unsigned short GetSocketPort(int sd);

//#define  LOG_TAG    "OpenHttp"
//#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
extern "C"
JNIEXPORT jint Java_com_open_clib_MyHttpJNI_nativeSend(JNIEnv *env, jobject obj,
		jbyteArray ip, jbyteArray url, jint method, jbyteArray header,
		jbyteArray body, jint start, jint length, jint id) {

	char * ip_buffer;
	char * url_buffer;
	char * header_buffer;

	signed char * body_buffer = (signed char*) malloc(length * sizeof(char));
	env->GetByteArrayRegion(body, start, length, body_buffer);
	Log("hello");

	return (jint) 1;
}

extern "C"
JNIEXPORT jint Java_com_open_clib_MyHttpJNI_test(JNIEnv *env, jobject obj,
		jbyteArray message) {

	int length = env->GetArrayLength(message);
	signed char * body_buffer = (signed char*) malloc(
			length + 1 * sizeof(char));
	env->GetByteArrayRegion(message, 0, length, body_buffer);
	body_buffer[length] = 0;
	Log((const char*) body_buffer);

	test(body_buffer);
	return (jint) 1;
}

void test(signed char * message) {
	int socket = setSend("192.168.1.7");
	sendPacket(socket,
			"GET /index.html HTTP/1.1\r\nHost: www.testhttp.com\r\n\r\n\r\n\r\n");
	char target[15] = "";
	parseNubmerToString((int)GetSocketPort(socket), target);
//	parseNubmerToString(9555444, target);
	Log((const char*) "Connected @ ");
	Log((const char*) target);
	recvPacket(socket);
}
static void make_sendipv4addr(struct sockaddr_in *addr, int remoteport) {
	memset(addr, 0, sizeof(struct sockaddr_in));
	addr->sin_family = AF_INET;
	addr->sin_port = htons(remoteport);
}

int setSend(const char *ipAddr) {
	int sockd = 0;
	char target[15] = "";
	struct sockaddr_in addr;
	make_sendipv4addr(&addr, 80);
	sockd = socket(AF_INET, SOCK_STREAM, 0);
	addr.sin_addr.s_addr = inet_addr("192.168.1.7");

//	int optval = 1;
//	struct sockaddr_in serveraddr;
//	make_recvipv4addr(&serveraddr, 6868);
//	setsockopt(optval, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof(optval));
//	if (-1
//			== bind(sockd, (struct sockaddr *) &serveraddr,
//					sizeof(serveraddr))) {
//		Log((const char*) "bind fail !\r\n");
//		return -1;
//	}
//	Log((const char*) "bind ok !\r\n");

//	if (-1 == listen(sockd, 5)) {
//		Log((const char*) "listen fail !\r\n");
//		return -1;
//	}
//	Log((const char*) "listen ok\r\n");

	int status = connect(sockd, (struct sockaddr *) &addr, sizeof(addr));

	if (status != 0) {

		parseNubmerToString(-status, target);
		Log((const char*) target);
		Log((const char*) "Connect fail!\n");
		return -1;
	}
	Log((const char*) "Connected\n");


	return sockd;
}


//返回当前socket绑定的端口
static unsigned short GetSocketPort(int sd) {
    unsigned short port = 0;
    struct sockaddr_in address;
    socklen_t addressLength = sizeof(address);
    if (-1 == getsockname(sd, (struct sockaddr*) &address, &addressLength)) {
    } else {
        port = ntohs(address.sin_port);
    }
    return port;
}


int sendPacket(int sockd, const char *str) {
	char data[MAXBUFLEN] = { 0 };
	strcpy(data, str);
	send(sockd, data, strlen(data), 0);
	return 1;
}

static void make_recvipv4addr(struct sockaddr_in *addr, const int localport) {
	memset(addr, 0, sizeof(struct sockaddr_in));

	addr->sin_family = AF_INET;
	addr->sin_port = htons(localport);
	addr->sin_addr.s_addr = INADDR_ANY;
}

int setupRecv() {
	int optval = 1;
	int socklsn = 0;
	struct sockaddr_in serveraddr;

	make_recvipv4addr(&serveraddr, 6868);

	socklsn = socket(AF_INET, SOCK_STREAM, 0);

	setsockopt(optval, SOL_SOCKET, SO_REUSEADDR, &optval, sizeof(optval));

	bind(socklsn, (struct sockaddr *) &serveraddr, sizeof(serveraddr));
	return socklsn;
}

int recvPacket(int sockd) {
	char packet[MAXBUFLEN] = { 0 };
	char * packetPtr = packet;

	int nBytesNeed = MAXBUFLEN;
	int nBytesRecv = 10;

	while (nBytesRecv > 0) {
		Log((const char*) "ready to recv");
		nBytesRecv = recv(sockd, packetPtr, MAXBUFLEN, 0);
		Log((const char*) "received");
		Log((const char*) packetPtr);

	}

	return 1;
}
