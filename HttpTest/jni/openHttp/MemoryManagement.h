#ifndef MEMORYMANAGEMENT_H
#define MEMORYMANAGEMENT_H

#include <stdlib.h>

void* JSMalloc(size_t size);

void JSFree(void* pointer);

#endif /* MEMORYMANAGEMENT_H */

