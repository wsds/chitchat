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

#define IN6ADDR_LOOPBACK_INIT { { { 0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1 } } }

#include <sys/socket.h>

#include <arpa/inet.h>

#include <linux/net.h>
#include <sys/types.h>
#include <net/inet_connection_sock.h>

//#include <net/sock.h>
//#include <fcntl.h>
#include <linux/tcp.h>
#include <netinet/tcp.h>

void test001();

#endif /* TEST_H */
