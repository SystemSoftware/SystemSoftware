// Parent.cpp : Defines the entry point for the console application.
//

#include "../common.h"


std::mt19937	rnd((long)time(nullptr));


int Random(int min, int max)
{
	int range = max - min;
	int rs = (int)std::lround(float(rnd()) / float(rnd.max()) * range);
	return rs + min;
}






int main()
{

	
    HANDLE pipe = CreateNamedPipeA(pipeName,
                            PIPE_ACCESS_DUPLEX | PIPE_TYPE_BYTE | PIPE_READMODE_BYTE,
                            PIPE_WAIT,
                            1,
                            BufferSize,
                            BufferSize,
                            NMPWAIT_WAIT_FOREVER,
                            NULL);

	
	//CreateNamedPipeA(pipeName,PIPE_ACCESS_DUPLEX,PIPE_TYPE_BYTE,PIPE_UNLIMITED_INSTANCES,BufferSize,BufferSize,1000,nullptr);
	if (pipe == INVALID_HANDLE_VALUE)
	{
		std::cerr << "Failed to create named pipe '"<<pipeName<<"' ("<<GetLastError()<<")" << std::endl;
		return -1;
	}

	std::cout << "Attempting to connect named pipe '"<<pipeName<<"'"<<std::endl;
	if (!ConnectNamedPipe(pipe,nullptr))
	{
		std::cerr << "Failed to connected named pipe" << std::endl;
		return -1;
	}

	char ops[] = "+-*/%";
	int numOps = (int)strlen(ops);
		
	TJob job;
	for (int i = 0; i < 100; i++)
	{
		job.op = ops[Random(0,numOps-1)];
		job.x = Random(-100,100);
		job.y = Random(1,100);
			
		DWORD bytes;
		if (!WritePOD(pipe,job))
		{
			perror("parent write");
			exit(1);
		}
		if (!ReadPOD(pipe,job))
		{
			perror("parent read");
			exit(1);
		}
		std::cout << job.x << ' '<<job.op<<' '<<job.y<< " = "<<job.result << std::endl;
	}
	DisconnectNamedPipe(pipe);
	CloseHandle(pipe);
	return 0;
}

