#ifndef CLOX_COMPILER_h
#define CLOX_COMPILER_h

#include "vm.h"

bool compile(const char *source, Chunk *chunk);

#endif