#include "Queue.h"

JSObject* Queue::take() {
	if (this->length > 0) {
		this->length--;
//		Log((char *) ("offer take%:"), this->head % this->max_size);
		JSObject* object = this->elements[this->head % this->max_size];
//		if (object == NULL) {
//			Log((char *) ("take NULL"));
//		}
		this->elements[this->head % this->max_size] = NULL;
		this->head++;
		return object;
	} else {
		return NULL;
	}
}

bool Queue::offer(JSObject * object) {
	if (this->max_size <= this->length) {
		this->resize(); //synchronous
	}

	this->length++;
//	Log((char *) ("offer tail:"), this->tail);
//	Log((char *) ("offer max size :"), this->max_size);
//	Log((char *) ("offer offer%:"), this->tail % this->max_size);
	this->elements[this->tail % this->max_size] = object;
	this->tail++;
	if (this->length > this->threshold) {
		this->resize(); //asynchronous
	}

	return true;

}

bool Queue::resize() {
	JSObject** old_elements = this->elements;
	this->max_size = this->max_size * 2;
	this->threshold = (int) (this->max_size * 0.8);

	int mem_size = this->max_size * sizeof(void*);

	this->elements = (JSObject**) JSMalloc(mem_size);

	int i = 0; //~~~~~~~~~~~~~~~Need Memory Management~~~~~~~~~~~~~~~~~
	for (i = this->head; i < this->tail; i++) {
		this->elements[i % this->max_size] = old_elements[i % this->max_size];
	}
	JSFree((void*) old_elements);

	return true;
}

bool Queue::initialize() {
	this->max_size = 8;
	this->length = 0;
	this->tail = 0;
	this->head = 0;
	this->threshold = (int) (this->max_size * 0.8);

	int mem_size = this->max_size * sizeof(void*);

	this->elements = (JSObject**) JSMalloc(mem_size);

	this->is_initialized = true;

	return true;
}

bool Queue::free() {
	//JSFree(this->elements);
	return true;
}
