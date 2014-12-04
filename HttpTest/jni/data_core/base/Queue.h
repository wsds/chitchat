#ifndef QUEUE_H
#define QUEUE_H

#include "JSObject.h"
#include "MemoryManagement.h"
#include "../../lib/Log.h"

#ifndef NULL
#define NULL 0
#endif /* NULL */

class Queue {
public:
	//store the orginal type of data directly should be supported. or supported by another class. 
	JSObject** elements; //~~~~~~~~~~~~~~~Memory Management~~~~~~~~~~~~~~~~~
	//JSObject* elements[50];//~~~~~~~~~~~~~~~Memory Management~~~~~~~~~~~~~~~~~//for debug

	int length;
	int tail;
	int head;

	int max_size;
	int threshold;

	bool is_initialized = false;

	//API
	//pop O(1)
	JSObject* take();

	//push O(1)
	bool offer(JSObject* object);

	//resize O(n)
	bool resize();

	//initialize default size=8;
	bool initialize();

	bool free();

};

#endif /* QUEUE_H */

