#ifndef TEST_H
#define TEST_H

#define NULL 0
//
//#define BITS_PER_LONG 32
//#define HZ 100
//#define CONFIG_TREE_RCU 1
//#define CONFIG_LBDAF 1
//
//#define u8 uint8_t
//#define u16 uint16_t
//#define s32 int32_t
//#define u32 uint32_t
//#define s64 int64_t
//#define u64 u_int64_t
#include <jni.h>
#include <errno.h>
#include <sys/endian.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <time.h>
#include "data_core/base/MemoryManagement.h"
//#include "lib/MyNet.h"
////
//#include <arpa/inet.h>
////
//#include <linux/net.h>
//#include <sys/types.h>
////#include <net/inet_connection_sock.h>
//
//#include <net/sock.h>
////#include <fcntl.h>
#include <linux/tcp.h>
//#include <netinet/tcp.h>

#include "lib/Log.h"

#include <stdlib.h>
#include <sys/mman.h>
#include <unistd.h>
#include <fcntl.h>

void test001231();

void test001232();

void test321414();

void test321415();

#endif /* TEST_H */
