#ifndef commonH
#define commonH

#include <stdio.h>
#include <stdlib.h>
#include <windows.h>
#include <iostream>
#include <random>
#include <time.h>

#undef max


struct TJob
{
	int x,y,result;
	char op;
};

static const char* pipeName = R"(\\.\pipe\pipetest)";
static const size_t BufferSize = 1024;


inline bool WriteData(HANDLE h, const void*data, size_t dataSize)
{
	DWORD bytes;
	return WriteFile(h, data, dataSize,&bytes,nullptr) && (size_t)bytes == dataSize;
}

inline bool ReadData(HANDLE h, void*data, size_t dataSize)
{
	DWORD bytes;
	return ReadFile(h, data, dataSize,&bytes,nullptr) && (size_t)bytes == dataSize;
}

template <typename T> bool WritePOD(HANDLE h, const T&item)	{return WriteData(h,&item,sizeof(item));}
template <typename T> bool ReadPOD(HANDLE h, T&item)	{return ReadData(h,&item,sizeof(item));}


#endif

