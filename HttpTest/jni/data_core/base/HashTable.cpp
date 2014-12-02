#include "HashTable.h"

JSObject* HashTable::get(char* key)
{
	unsigned int hash = dictGenHashFunction(key, strlen(key));
	int index = hash%max_size;
	if (elements[index] != NULL){
		HashEntry* brother = elements[index];
		do{
			if (strcmp(brother->key, key) == 0){
				return brother->value;
			}
			brother = brother->next;
		} while (brother != NULL);
	}

	return NULL;
}

JSObject* HashTable::set(char* key, JSObject* value)
{
	HashEntry* element = new HashEntry();//get from HashEntry pool//to do

	unsigned int hash = dictGenHashFunction(key, strlen(key));
	int index = hash%max_size;

	if (elements[index] != NULL){
		HashEntry* brother = elements[index];
		do{
			if (strcmp(brother->key, key) == 0){
				//free the old value "brother->value"
				//todo
				JSObject* oldObject = brother->value;
				brother->value = value;
				return oldObject;//replace entry
			}
			brother = brother->next;
		} while (brother != NULL);
	}

	element->key = key;
	element->value = value;
	element->hash = hash;

	if (elements[index] == NULL){
		elements[index] = element;
	}

	else{
		int level = 1;
		HashEntry* brother = elements[index];
		while (brother->next != NULL) {
			level++;
			brother = brother->next;
		}
		brother->next = element;
		element->level = level;
	}

	this->length++;
	if (this->length > this->threshold)
	{
		this->resize();//asynchronous//todo
	}

	return NULL;//new entry
}

JSObject*  HashTable::del(char* key)
{
	unsigned int hash = dictGenHashFunction(key, strlen(key));
	int index = hash%max_size;
	if (elements[index] != NULL){
		HashEntry** brother = elements + index;
		do{
			if (strcmp((*brother)->key, key) == 0){
				HashEntry* old_element = *brother;
				//free the old element, put them into a HashEntry pool to reuse them.
				//todo

				(*brother) = (*brother)->next;
				return old_element->value;
			}
			brother = &((*brother)->next);
		} while ((*brother) != NULL);
	}


	return NULL;
}

bool HashTable::resize()
{
	int old_index = 0;//~~~~~~~~~~~~~~~Need Memory Management~~~~~~~~~~~~~~~~~
	int index = 0;
	HashEntry* element = NULL;
	HashEntry* old_element = NULL;
	HashEntry* brother = NULL;
	int level = 1;
	int old_max_size = this->max_size;

	HashEntry** old_elements = this->elements;

	this->max_size = this->max_size * 2 + 7;
	this->threshold = (int)(this->max_size * 0.8);

	int mem_size = this->max_size*sizeof(void*);

	this->elements = (HashEntry**)JSMalloc(mem_size);

	for (old_index = 0; old_index < old_max_size; old_index++)
	{
		element = old_elements[old_index];
		if (element != NULL){
			do{

				//put old element into the new space
				index = element->hash%this->max_size;
				if (elements[index] == NULL){
					elements[index] = element;
				}

				else{
					level = 1;
					brother = elements[index];
					while (brother->next != NULL) {
						level++;
						brother = brother->next;
					}
					brother->next = element;
					element->level = level;
				}
				//put old element into the new space
				old_element = element;
				element = element->next;
				old_element->next = NULL;
			} while (element != NULL);
		}

	}
	JSFree((void*)old_elements);

	return true;
}

bool HashTable::initialize()
{
	this->max_size = 1 + 7;
	this->length = 0;
	this->threshold = (int)(this->max_size * 0.8);

	int mem_size = this->max_size*sizeof(void*);

	this->elements = (HashEntry**)JSMalloc(mem_size);
	//for (int i = 0; i < 50; i++){
	//	this->elements[i] = NULL;
	//}

	this->is_initialized = true;

	return true;

}


static unsigned int dict_hash_function_seed = 5381;
/* MurmurHash2, by Austin Appleby
* Note - This code makes a few assumptions about how your machine behaves -
* 1. We can read a 4-byte value from any address without crashing
* 2. sizeof(int) == 4
*
* And it has a few limitations -
*
* 1. It will not work incrementally.
* 2. It will not produce the same results on little-endian and big-endian
*    machines.
*/
unsigned int dictGenHashFunction(const void *key, int len) {
	/* 'm' and 'r' are mixing constants generated offline.
	They're not really 'magic', they just happen to work well.  */
	unsigned int seed = dict_hash_function_seed;
	const unsigned int m = 0x5bd1e995;
	const int r = 24;

	/* Initialize the hash to a 'random' value */
	unsigned int h = seed ^ len;

	/* Mix 4 bytes at a time into the hash */
	const unsigned char *data = (const unsigned char *)key;

	while (len >= 4) {
		unsigned int k = *(unsigned int*)data;

		k *= m;
		k ^= k >> r;
		k *= m;

		h *= m;
		h ^= k;

		data += 4;
		len -= 4;
	}

	/* Handle the last few bytes of the input array  */
	switch (len) {
	case 3: h ^= data[2] << 16;
	case 2: h ^= data[1] << 8;
	case 1: h ^= data[0]; h *= m;
	};

	/* Do a few final mixes of the hash to ensure the last few
	* bytes are well-incorporated. */
	h ^= h >> 13;
	h *= m;
	h ^= h >> 15;

	return (unsigned int)h;
}


int strcmp(char *src, char *dst)
{

	int ret = 0;

	while (!(ret = *src - *dst) && *dst)

		++src, ++dst;

	if (ret < 0)

		ret = -1;

	else if (ret > 0)

		ret = 1;

	return(ret);
}

int strlen(char *str)
{
	char *eos = str;

	while (*eos++);

	return(eos - str - 1);
}

int strcopy(char *source, char *target)
{
	char *eos = source;

	while (*eos){
		*target = *eos;
		eos++;
		target++;
	}

	*target = '\0';

	return(eos - source - 1);
}

int strcopy(char *source, char *target, int length)
{
	*(target + length) = '\0';

	while (length >= 0){
		length--;
		*(target + length) = *(source + length);
	}

	return length;
}
int strappend(char *target, char *source){

	int offset = strlen(target);
	target = target + offset;
	return strcopy(source, target);
}

void strclear(char *str)
{
	*str = '\0';
}

int parseNubmerToString(int number, char * target){
	char buf[15] = "";//need memory optimize
	int len = 0;
	while (number != 0){
		buf[len++] = number % 10 + NUMBERCHARSTART;
		number /= 10;
	}
	for (int j = len - 1; j >= 0; j--){
		target[len - j - 1] = buf[j];
	}
	target[len] = '\0';
	return len;
}

int parseStringToNubmer(char* string, int length){
	char number_char;
	int result = 0;
	for (int i = 0; i < length; i++){
		number_char = string[i];
		if (number_char >= NUMBERCHARSTART&&number_char <= NUMBERCHAREND){
			result = number_char - NUMBERCHARSTART + result * 10;
		}
		else{
			//parse error
		}
	}
	return result;
}

bool isNumber(char *string)
{
	char *eos = string;

	while (*eos){
		if (*eos >= NUMBERCHARSTART&&NUMBERCHAREND >= *eos){
		}
		else{
			return false;
		}
		eos++;
	}

	return true;
}
