#include "JSString.h"

JSString::JSString(){
	this->type = JSSTRING;
}

JSString::JSString(char* char_string){
	this->type = JSSTRING;
	this->char_string = char_string;
	this->length = strlen(char_string);
}