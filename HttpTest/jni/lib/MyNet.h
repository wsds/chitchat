#ifndef _MY_NET_H
#define _MY_NET_H

//#include "MyTypes.h"
//
typedef enum {
	SS_FREE = 0, /* not allocated		*/
	SS_UNCONNECTED, /* unconnected to any socket	*/
	SS_CONNECTING, /* in process of connecting	*/
	SS_CONNECTED, /* connected to socket		*/
	SS_DISCONNECTING /* in process of disconnecting	*/
} socket_state;

struct rcu_head {
	struct rcu_head *next;
	void (*func)(struct rcu_head *head);
};

struct socket_wq {
	void * 	wait;
	struct fasync_struct	*fasync_list;
	struct rcu_head		rcu;
};



#define kmemcheck_bitfield_begin(name)	\
	int name##_begin[0];

#define kmemcheck_bitfield_end(name)	\
	int name##_end[0];


struct MySocket {
	socket_state		state;

	kmemcheck_bitfield_begin(type);
	short			type;
	kmemcheck_bitfield_end(type);

	unsigned long		flags;

	struct socket_wq __rcu;
	struct socket_wq *wq;

	struct file		*file;
	struct sock		*sk;
	const struct proto_ops	*ops;
};


#endif /* _MY_NET_H */
