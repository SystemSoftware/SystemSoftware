// Child.cpp : Defines the entry point for the console application.
//

//#include "stdafx.h"

#include "../common.h"

int main()
{

	
    HANDLE pipe = CreateFileA(pipeName,
                           GENERIC_READ | GENERIC_WRITE, 
						   0,
						   NULL,
						   OPEN_EXISTING,
						   0,
						   NULL);

	
	if (pipe == INVALID_HANDLE_VALUE)
	{
		std::cerr << "Failed to access named pipe '"<<pipeName<<"' ("<<GetLastError()<<")" << std::endl;
		return -1;
	}


	TJob nextJob;
	DWORD bytes;
	while (true)
	{
		if (!ReadPOD(pipe,nextJob))
		{
			perror("child read");
			CloseHandle(pipe);
			exit(1);
		}
		switch (nextJob.op)
		{
			case '+':
				nextJob.result = nextJob.x + nextJob.y;
			break;
			case '-':
				nextJob.result = nextJob.x - nextJob.y;
			break;
			case '*':
				nextJob.result = nextJob.x * nextJob.y;
			break;
			case '/':
				nextJob.result = nextJob.x / nextJob.y;
			break;
			case '%':
				nextJob.result = nextJob.x % nextJob.y;
			break;
		}
		if (!WritePOD(pipe,nextJob))
		{
			perror("child write");
			CloseHandle(pipe);
			exit(1);
		}
	}

	CloseHandle(pipe);
	return 0;
}

