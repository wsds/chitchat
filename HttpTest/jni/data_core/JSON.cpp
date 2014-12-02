#include "JSON.h"
#include <iostream>
#include "../swift/interface/JSFunction.h"
#include "../swift/interface/JSClass.h"

JSON::JSON(){
	this->type = JSJSON;
	//this->initialize();
}


bool JSON::initialize()
{
	this->list = new LIST();//to optimize the memory use, get it from a managed pool instead of new it.//to do
	this->list->initialize();
	this->hashTable = new HashTable();
	this->hashTable->initialize();

	this->length = 0;
	this->is_initialized = true;

	return true;
}

bool JSON::free()
{
	//JSFree(this->elements);
	return true;
}


//list
JSObject* JSON::pop()
{
	JSObject* object = this->list->pop();
	if (object != NULL&&object->type == JSKEYVALUE){
		this->hashTable->del(((JSKeyValue*)object)->key);
	}
	this->length--;
	return object;
}

bool JSON::push(JSObject* object)
{
	this->list->push(object);
	if (object->type == JSKEYVALUE){
		this->hashTable->set(((JSKeyValue*)object)->key, object);
	}
	this->length++;
	return true;
}

bool JSON::insert(JSObject* object, int index)
{
	this->list->insert(object, index);
	if (object->type == JSKEYVALUE){
		this->hashTable->set(((JSKeyValue*)object)->key, object);
	}
	this->length++;
	return true;
}

bool JSON::replace(JSObject* object, int index)
{
	this->list->replace(object, index);
	if (object->type == JSKEYVALUE){
		this->hashTable->set(((JSKeyValue*)object)->key, object);
	}
	return true;
}

JSObject*  JSON::del(int index)
{
	JSObject* object = this->list->del(index);

	if (object != NULL)//deleted
	{
		if (object->type == JSKEYVALUE){
			this->hashTable->del(((JSKeyValue*)object)->key);
		}
		this->length--;
	}
	else //not found
	{
	}
	return object;
}

JSObject* JSON::find(int index)
{
	JSObject* object = this->list->find(index);
	return object;
}


//hash
JSObject* JSON::get(char* key)
{
	JSObject* object = this->hashTable->get(key);
	return object;
}

bool JSON::set(char* key, JSObject* value)
{
	JSKeyValue* object = new JSKeyValue();
	object->key = key;
	object->value = value;

	JSObject* oldObject = this->hashTable->set(((JSKeyValue*)object)->key, (JSObject*)object);

	if (oldObject == NULL)//new entry
	{
		this->list->push((JSObject*)object);
		this->length++;
	}
	else//replace entry
	{
		object->free();
		object = (JSKeyValue*)this->hashTable->get(key);
		int index = this->list->findValue((JSObject*)oldObject);
		//free the old object //to do
		if (index == -1){
			return false;
		}
		this->list->replace((JSObject*)object, index);
	}

	return true;
}

JSObject* JSON::del(char* key)
{
	JSObject* object = this->hashTable->del(key);

	if (object != NULL)//deleted
	{
		int index = this->list->findValue((JSObject*)object);
		this->list->del(index);
		this->length--;
	}
	else //not found
	{
	}
	return object;
}





int stringifyJSObject(JSObject* object, char* string, int index);


char* funcStr = "func";
char* nullStr = "NULL";

int stringifyJSObject(JSObject* object, char* string1, int index){
	//int index = 0;
	int offset = 0;
	if (object == NULL){

		int i = 0;
		while (nullStr[i]){
			string1[index + i] = nullStr[i];
			i++;
		}
		index = index + i;
		offset = offset + i;
	}
	else if (object->type == JSNUMBER){
		JSNumber * js_number = (JSNumber*)object;
		offset = parseNubmerToString(((JSObject*)js_number)->number, string1 + index);
	}
	else if (object->type == JSSTRING){
		JSString * js_string = (JSString*)object;
		if (js_string->length == 1){
			string1[index] = DOUBLEQUOTES;
			string1[index + 1] = DOUBLEQUOTES;
			index = index + 2;
			offset = offset + 2;
		}
		else{
			bool hasQuote = false;
			for (int j = 0; j < js_string->length; j++){
				if (((JSObject*)js_string)->char_string[j] == SINGLEQUOTE || ((JSObject*)js_string)->char_string[j] == DOUBLEQUOTES){
					hasQuote = true;
					break;
				}
			}
			if (hasQuote){
				string1[index] = DOUBLEQUOTES;
				string1[index + 1] = DOUBLEQUOTES;
				string1[index + 2] = DOUBLEQUOTES;
				index = index + 3;
				offset = offset + 3;
			}
			else{
				string1[index] = DOUBLEQUOTES;
				index = index + 1;
				offset = offset + 1;
			}
			for (int j = 0; j < js_string->length; j++){
				string1[index + j] = ((JSObject*)js_string)->char_string[j];
			}
			index = index + js_string->length;
			offset = offset + js_string->length;
			if (hasQuote){
				string1[index] = DOUBLEQUOTES;
				string1[index + 1] = DOUBLEQUOTES;
				string1[index + 2] = DOUBLEQUOTES;
				index = index + 3;
				offset = offset + 3;
			}
			else{
				string1[index] = DOUBLEQUOTES;
				index = index + 1;
				offset = offset + 1;
			}
		}

	}
	else if (object->type == JSKEYVALUE){
		JSKeyValue * key_value = (JSKeyValue*)object;
		string1[index] = DOUBLEQUOTES;
		index++;
		char* key = key_value->key;
		int i = 0;
		while (key[i]){
			string1[index + i] = key[i];
			i++;
		}
		index = index + i;
		string1[index] = DOUBLEQUOTES;

		string1[index + 1] = COLON;
		index = index + 2;

		offset = 3 + i;
		//parse value
		offset = offset + stringifyJSObject(key_value->value, string1, index);
	}
	else if (object->type == JSFUNCTION){
		JSFunction * jsFunction = (JSFunction*)object;

		int i = 0;
		while (funcStr[i]){
			string1[index + i] = funcStr[i];
			i++;
		}
		index = index + i;
		offset = offset + i;
	}
	else if (object->type == JSJSON){
		string1[index] = LEFTBRACKET;
		index++;
		int sub_offset = 0;
		JSObject *sub_object;
		for (int i = 0; i < ((JSON*)object)->list->length; i++){
			if (i != 0){
				string1[index + sub_offset] = COMMA;
				sub_offset++;
			}
			sub_object = ((JSON*)object)->list->find(i);
			sub_offset = sub_offset + stringifyJSObject(sub_object, string1, index + sub_offset);
		}

		string1[index + sub_offset] = RIGHTBRACKET;
		offset = sub_offset + 2;
	}
	return offset;
}

char* stringifyJSON(JSON* json){
	char* string = new char[100];
	int index = 0;
	int offset = stringifyJSObject((JSObject*)json, string, index);
	string[offset] = STREND;
	return string;
}



JSObject * cloneJSObject(JSObject* object, JSON * parent){
	JSObject * clone = NULL;
	if (object == NULL){
		return NULL;
	}
	else if (object->type == JSNUMBER){
		JSNumber * js_number = new JSNumber();
		js_number->number = ((JSNumber *)object)->number;
		clone = js_number;
	}
	else if (object->type == JSSTRING){
		JSString * js_string = new JSString();
		js_string->char_string = (char *)JSMalloc(strlen(((JSString *)object)->char_string) + 1);
		strcopy(((JSString *)object)->char_string, js_string->char_string);
		js_string->length = ((JSString *)object)->length;
		clone = js_string;
	}
	else if (object->type == JSFUNCTION){
		JSFunction * jsFunction = new JSFunction();

		jsFunction->function_name = ((JSFunction *)object)->function_name;
		jsFunction->function = ((JSFunction *)object)->function;
		jsFunction->functionDefinition = ((JSFunction *)object)->functionDefinition;
		jsFunction->closure = new Closure();
		jsFunction->closure->variables = parent;

		clone = jsFunction;
	}
	else if (object->type == JSCLASS){
		JSClass * jsClass = new JSClass();

		jsClass->className = (char *)JSMalloc(strlen(((JSClass *)object)->className) + 1);
		strcopy(((JSClass *)object)->className, jsClass->className);

		jsClass->children = new JSON();
		jsClass->children->initialize();
		cloneJSON((JSON *)object, jsClass->children);

		clone = jsClass;
	}
	else if (object->type == JSJSON){
		JSON * json = new JSON();
		json->initialize();
		cloneJSON((JSON *)object, json);
		clone = json;
	}
	return clone;
}

void cloneJSON(JSON * json, JSON * clone){

	JSObject * sub_object = NULL;
	for (int i = 0; i < json->list->length; i++){
		sub_object = json->list->find(i);
		if (sub_object->type == JSKEYVALUE){
			JSKeyValue * key_value = (JSKeyValue*)sub_object;

			char * key = NULL;
			key = (char *)JSMalloc(strlen(key_value->key) + 1);
			strcopy(key_value->key, key);
			JSObject * value = cloneJSObject(key_value->value, clone);
			clone->set(key, value);
		}
		else{
			JSObject * cloneObject = cloneJSObject(sub_object, clone);
			clone->push(cloneObject);
		}

	}
}

JSON* cloneJSON(JSON * json){
	JSON * clone = new JSON();
	clone->initialize();
	cloneJSON(json, clone);
	return clone;
}


char* JSON::stringify()
{
	JSString * js_string = new JSString();
	((JSObject*)js_string)->char_string = new char[100];
	js_string->length = 0;
	//stringifyJSON(this, js_string);
	((JSObject*)js_string)->char_string[js_string->length] = STREND;
	return ((JSObject*)js_string)->char_string;
}

bool JSON::parse(char* string)
{
	parseJSON(string);
	return true;
}

int parsingStatus = 1;//[1:normal,2:quote_start,3:string,4:second_quote_start,5:quote_stop,6:second_quote_stop],[1-2,2-3,3-1,2-4,4-5,5-3,4-1]
int QuoteStatus = 11;//[ 11 = ' , 13 = ''' , 21 = " , 23 = """]
int last_COMMA_index = 0;


int json_indicators_stack_size = 10;
int json_indicators_stack_top = 0;

int key_value_stack_size = 10;
int key_value_stack_top = 0;

class KeyValueIndicator{
public:
	int json_indicators_stack_top;
	JSKeyValue* key_value;
};

JSON* parseJSON(char* string){

	char localChar;
	int string_length = strlen(string);
	last_COMMA_index = 0;

	JSONIndicator* json_indicator = NULL;
	JSONIndicator* object_indicator = NULL;
	JSON* result_json = NULL;

	json_indicators_stack_top = 0;
	key_value_stack_top = 0;

	//JSKeyValue * jsKeyValue = NULL;

	int mem_size = json_indicators_stack_size*sizeof(JSONIndicator*);
	JSONIndicator** json_indicators = (JSONIndicator**)JSMalloc(mem_size);

	int key_value_mem_size = key_value_stack_size*sizeof(KeyValueIndicator*);
	KeyValueIndicator** key_value_indicators = (KeyValueIndicator**)JSMalloc(key_value_mem_size);

	for (int i = 0; i< string_length; i++){
		localChar = string[i];
		if (parsingStatus == 1){
			//*************************************parsingStatus == 1*****************************************************************************************************
			if (localChar == COMMA){
				if (i>last_COMMA_index){
					object_indicator = new JSONIndicator();//get from pool//to do
					object_indicator->head = last_COMMA_index;
					object_indicator->tail = i;
					object_indicator->quotes_count = json_indicator->quotes_count;
					json_indicator->quotes_count = 0;

					last_COMMA_index = i + 1;

					JSObject* object = parseObject(string, object_indicator, false);
					if (key_value_stack_top > 0 && key_value_indicators[key_value_stack_top - 1]->key_value->value == NULL){
						if (object != NULL){
							key_value_indicators[key_value_stack_top - 1]->key_value->value = object;
							key_value_stack_top--;
						}
						else{
							//report error,value cannot be NULL
						}
					}
					else{
						if (object != NULL){
							((JSON*)(json_indicator->json))->push(object);
						}
					}

				}
			}
			else if (localChar == COLON){
				if (i > last_COMMA_index){
					object_indicator = new JSONIndicator();//get from pool//to do
					object_indicator->head = last_COMMA_index;
					object_indicator->tail = i;
					object_indicator->quotes_count = json_indicator->quotes_count;
					json_indicator->quotes_count = 0;

					last_COMMA_index = i + 1;

					JSObject* object = parseObject(string, object_indicator, true);
					if (key_value_stack_top < key_value_stack_size){
						if (object != NULL){
							key_value_indicators[key_value_stack_top] = new KeyValueIndicator();
							key_value_indicators[key_value_stack_top]->key_value = (JSKeyValue *)object;
							key_value_indicators[key_value_stack_top]->json_indicators_stack_top = json_indicators_stack_top;
							key_value_indicators[key_value_stack_top]->key_value->value = NULL;
							((JSON*)(json_indicator->json))->push(object);
							key_value_stack_top++;
						}
						else{
							//report error,key cannot be NULL
						}
					}
					else{
						//report error;
					}
				}
			}
			else if (localChar == SINGLEQUOTE){
				QuoteStatus = 11;
				parsingStatus = 2;
			}
			else if (localChar == DOUBLEQUOTES){
				QuoteStatus = 21;
				parsingStatus = 2;
			}
			else if (localChar == LEFTBRACKET){
				if (json_indicators_stack_top < json_indicators_stack_size){


					JSON* json = new JSON();
					json->initialize();

					json->json_indicator = new JSONIndicator();//get from pool//to do
					json->json_indicator->head = i;
					json->json_indicator->json = (JSObject*)json;
					json->json_indicator->quotes_count = 0;

					json_indicators[json_indicators_stack_top] = json->json_indicator;
					json_indicators_stack_top++;

					if (i == 0 && json_indicator == NULL){
						result_json = json;
					}
					else{
						if (key_value_stack_top > 0 && string[i - 1] == COLON){
							key_value_indicators[key_value_stack_top - 1]->key_value->value = (JSObject*)json;
							key_value_indicators[key_value_stack_top - 1]->json_indicators_stack_top = json_indicators_stack_top;
						}
						else{
							((JSON*)(json_indicator->json))->push((JSObject*)json);
						}
					}

					json_indicator = json->json_indicator;

					last_COMMA_index++;
				}
				else{
					//report error
				}
			}
			else if (localChar == RIGHTBRACKET){

				object_indicator = new JSONIndicator();//get from pool//to do
				object_indicator->head = last_COMMA_index;
				object_indicator->tail = i;
				object_indicator->quotes_count = json_indicator->quotes_count;
				json_indicator->quotes_count = 0;


				JSObject* object = parseObject(string, object_indicator, false);
				if (key_value_stack_top > 0 && key_value_indicators[key_value_stack_top - 1]->json_indicators_stack_top == json_indicators_stack_top){
					if (key_value_indicators[key_value_stack_top - 1]->key_value->value == NULL){
						if (object != NULL){
							key_value_indicators[key_value_stack_top - 1]->key_value->value = object;
						}
						else{
							//report error,value cannot be NULL
						}
					}
					else if (key_value_stack_top > 0){
						if (object != NULL){
							((JSON*)(json_indicator->json))->push(object);
						}
					}
					key_value_stack_top--;
				}
				else{
					if (object != NULL){
						((JSON*)(json_indicator->json))->push(object);
					}
				}

				last_COMMA_index = i + 1;


				if (json_indicators_stack_top > 1){
					//resolve the last element spited by COMMA
					json_indicator = json_indicators[json_indicators_stack_top - 1];
					json_indicator->tail = i;


					json_indicator = json_indicators[json_indicators_stack_top - 2];
					//json_indicator->json = parseJSON(string, json_indicator);
					json_indicators_stack_top--;
				}
				else if (json_indicators_stack_top = 1){
					if (i + 1 == string_length){
						//resolve the last element spited by COMMA
						int p = 10;
						p++;
						//break;
						return result_json;
					}
					else{
						//report error
					}
				}
				else{
					//report error
				}
			}
			//*************************************parsingStatus == 1*****************************************************************************************************

		}
		else if (parsingStatus == 3){

			if ((localChar == SINGLEQUOTE && QuoteStatus == 11) || (localChar == DOUBLEQUOTES && QuoteStatus == 21)){
				json_indicator->quotes_count = 1;
				parsingStatus = 1;
			}
			else if ((localChar == SINGLEQUOTE && QuoteStatus == 13) || (localChar == DOUBLEQUOTES && QuoteStatus == 23)){
				parsingStatus = 5;
			}
		}
		else if (parsingStatus == 2){
			if ((localChar == SINGLEQUOTE && QuoteStatus == 11) || (localChar == DOUBLEQUOTES && QuoteStatus == 21)){
				parsingStatus = 4;
			}
			else{
				parsingStatus = 3;
			}
		}
		else if (parsingStatus == 4){
			if (localChar == SINGLEQUOTE && QuoteStatus == 11){
				parsingStatus = 3;
				QuoteStatus = 13;
			}
			else if (localChar == DOUBLEQUOTES && QuoteStatus == 21){
				parsingStatus = 3;
				QuoteStatus = 23;
			}
			else{
				json_indicator->quotes_count = 1;
				parsingStatus = 1;
				if (localChar == COMMA){
					if (i > last_COMMA_index){
						object_indicator = new JSONIndicator();//get from pool//to do
						object_indicator->head = last_COMMA_index;
						object_indicator->tail = i;
						object_indicator->quotes_count = json_indicator->quotes_count;
						json_indicator->quotes_count = 0;

						last_COMMA_index = i + 1;

						JSObject* object = parseObject(string, object_indicator, false);
						if (key_value_stack_top > 0 && key_value_indicators[key_value_stack_top - 1]->key_value->value == NULL){
							if (object != NULL){
								key_value_indicators[key_value_stack_top - 1]->key_value->value = object;
								key_value_stack_top--;
							}
							else{
								//report error,value cannot be NULL
							}
						}
						else{
							if (object != NULL){
								((JSON*)(json_indicator->json))->push(object);
							}
						}

					}
				}
				else if (localChar == COLON){
					if (i > last_COMMA_index){
						object_indicator = new JSONIndicator();//get from pool//to do
						object_indicator->head = last_COMMA_index;
						object_indicator->tail = i;
						object_indicator->quotes_count = json_indicator->quotes_count;
						json_indicator->quotes_count = 0;

						last_COMMA_index = i + 1;

						JSObject* object = parseObject(string, object_indicator, true);
						if (key_value_stack_top < key_value_stack_size){
							if (object != NULL){
								key_value_indicators[key_value_stack_top] = new KeyValueIndicator();
								key_value_indicators[key_value_stack_top]->key_value = (JSKeyValue *)object;
								key_value_indicators[key_value_stack_top]->json_indicators_stack_top = json_indicators_stack_top;
								key_value_indicators[key_value_stack_top]->key_value->value = NULL;
								((JSON*)(json_indicator->json))->push(object);
								key_value_stack_top++;
							}
							else{
								//report error,key cannot be NULL
							}
						}
						else{
							//report error;
						}
					}
				}
				else if (localChar == RIGHTBRACKET){

					object_indicator = new JSONIndicator();//get from pool//to do
					object_indicator->head = last_COMMA_index;
					object_indicator->tail = i;
					object_indicator->quotes_count = json_indicator->quotes_count;
					json_indicator->quotes_count = 0;


					JSObject* object = parseObject(string, object_indicator, false);
					if (key_value_stack_top > 0 && key_value_indicators[key_value_stack_top - 1]->json_indicators_stack_top == json_indicators_stack_top){
						if (key_value_indicators[key_value_stack_top - 1]->key_value->value == NULL){
							if (object != NULL){
								key_value_indicators[key_value_stack_top - 1]->key_value->value = object;
							}
							else{
								//report error,value cannot be NULL
							}
						}
						else if (key_value_stack_top > 0){
							if (object != NULL){
								((JSON*)(json_indicator->json))->push(object);
							}
						}
						key_value_stack_top--;
					}
					else{
						if (object != NULL){
							((JSON*)(json_indicator->json))->push(object);
						}
					}

					last_COMMA_index = i + 1;

					if (json_indicators_stack_top > 1){
						//resolve the last element spited by COMMA
						json_indicator = json_indicators[json_indicators_stack_top - 1];
						json_indicator->tail = i;


						json_indicator = json_indicators[json_indicators_stack_top - 2];
						//json_indicator->json = parseJSON(string, json_indicator);
						json_indicators_stack_top--;
					}
					else if (json_indicators_stack_top = 1){
						if (i + 1 == string_length){
							//resolve the last element spited by COMMA
							int p = 10;
							p++;
							//break;
							return result_json;
						}
						else{
							//report error
						}
					}
					else{
						//report error
					}
				}
			}
		}
		else if (parsingStatus == 5){
			if ((localChar == SINGLEQUOTE && QuoteStatus == 13) || (localChar == DOUBLEQUOTES && QuoteStatus == 23)){
				parsingStatus = 6;
			}
			else{
				parsingStatus = 3;
			}
		}
		else if (parsingStatus == 6){
			if ((localChar == SINGLEQUOTE && QuoteStatus == 13) || (localChar == DOUBLEQUOTES && QuoteStatus == 23)){
				json_indicator->quotes_count = 3;
				parsingStatus = 1;
			}
			else{
				parsingStatus = 3;
			}
		}


	}

	JSONIndicator* e[7];

	JSON* j[7];
	for (int i = 0; i < 7; i++){
		e[i] = json_indicators[i];
		j[i] = (JSON*)json_indicators[i]->json;
	}
	return NULL;

}

JSObject* parseObject(char* string, JSONIndicator* object_indicator, bool isJSKeyValue){
	JSObject* object;
	int start = object_indicator->head;
	int end = object_indicator->tail;
	char localChar;
	while (true){
		localChar = string[start];
		if (localChar == TAB || localChar == COMMA || localChar == ENTER || localChar == BR || localChar == BLANK){
			start++;
		}
		else{
			break;
		}
	}

	while (true){
		localChar = string[end - 1];
		if (localChar == TAB || localChar == COMMA || localChar == ENTER || localChar == BR || localChar == BLANK){
			end--;
		}
		else{
			break;
		}
	}
	if (start >= end){
		return NULL;
	}

	int length = end - start - object_indicator->quotes_count * 2;
	//std::cout << length << std::endl;
	char *tartget_string = (char*)JSMalloc(length + 1);
	for (int i = start + object_indicator->quotes_count; i < end - object_indicator->quotes_count; i++){
		tartget_string[i - (start + object_indicator->quotes_count)] = string[i];
	}
	tartget_string[length] = STREND;

	if (isJSKeyValue == false){

		//object = new JSObject();

		if (object_indicator->quotes_count != 0){
			//std::cout << "string: ";
			JSString * js_string = new JSString();
			((JSObject*)js_string)->char_string = tartget_string;
			js_string->length = length;
			object = (JSObject *)js_string;
		}
		else{
			JSNumber * js_number = new JSNumber();
			((JSObject*)js_number)->number = parseStringToNubmer(tartget_string, length);
			object = (JSObject *)js_number;

			//std::cout << "number: " << js_nubmer->number << std::endl;
		}


	}
	else{
		object = (JSObject*)new JSKeyValue();

		//std::cout << "JSKey: ";
		//std::cout << tartget_string << std::endl;
		((JSKeyValue*)object)->key = tartget_string;
	}

	//std::cout << "target_string:" << tartget_string << std::endl;

	return object;
}



void testJSONParse(){

	char * json_str = "[a:1,'''abc''',123,[1,2,[a:1,b:[123,[1,12],456],123],'a':2,'b:3':\"123456\"],'''a''', '''b''']";
	char * json_str1 = "[123,[1,12,13,[123,[1,12],456],14,15,16,17],456,789,101112]";
	char * json_str2 = "[123,[1,'''abc'''],456,\"def\",'''b''','''abc''']";
	char * json_str3 = "[123,[1,'''abc'''],\"a\":\"\"\"456\"\"\",\"def\"]";
	char * json_str4 = "[123,567:[012,456,'123123123'],567]";
	char * json_str5 = "[123,567:[012,[\"hello\"],456,'''''',123:[123,'123123'],123],567]";

	JSON* json = parseJSON(json_str5);

	std::cout << stringifyJSON(json) << std::endl;

}