#include "MemoryManagement.h"


void* JSMalloc(size_t size)
{
	void *pointer = malloc(size);

//	for (unsigned int i = 0; i < size; i++){
//		*((char*)pointer + i) = 0;
//	}
	memset(pointer, 0, size);
	return pointer;
}

void JSFree(void* pointer)
{
	free(pointer);
}
