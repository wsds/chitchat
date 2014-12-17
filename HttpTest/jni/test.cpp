#include <test.h>

void test001() {
//	struct socket * iSocket = NULL;
//	void *sk = (void *)iSocket->sk;
//	struct tcp_info info;
//	int tcp_info_length = sizeof(info);
//	getsockopt(httpEntity->socketFD, SOL_TCP, TCP_INFO, (void *) &info, &tcp_info_length);
}
static struct inet_sock *inet_sk(const struct sock *sk) {
	return (struct inet_sock *) sk;
}
int tcp_getsockopt(struct sock *sk, int level, int optname, char __user *optval, int __user *optlen) {
	struct inet_connection_sock *icsk = inet_csk(sk);

	if (level != SOL_TCP) {
		return icsk->icsk_af_ops->getsockopt(sk, level, optname, optval, optlen);
		getsockopt(sk, level, optname, optval, optlen);
	}

	return do_tcp_getsockopt(sk, level, optname, optval, optlen);
}
static int do_tcp_getsockopt(struct sock *sk, int level, int optname, char __user *optval, int __user *optlen) {
	struct inet_connection_sock *icsk = inet_csk(sk);
	struct tcp_sock *tp = tcp_sk(sk);
	int val, len;

	if (get_user(len, optlen))
		return -EFAULT;

	len = min_t(unsigned int, len, sizeof(int));
	if (len < 0)
		return -EINVAL;

	struct tcp_info info;

	if (get_user(len, optlen))
		return -EFAULT;

	tcp_get_info(sk, &info); /* 获取TCP连接的详细信息！*/

	len = min_t(unsigned int, len, sizeof(info));

	if (put_user(len, optlen))
		return -EFAULT;

	if (copy_to_user(optval, &info, len))
		return -EFAULT;

	return 0;
}
