#ifndef MEMORYMANAGEMENT_H
#define MEMORYMANAGEMENT_H

#include <stdlib.h>
#include <sys/mman.h>
#include <unistd.h>

void* JSMalloc(size_t size);

void JSFree(void* pointer);

void* JSMalloc(size_t size, int FD, int offset);

void JSFree(void* pointer, size_t size, int FD, size_t fileLength);

#endif /* MEMORYMANAGEMENT_H */

