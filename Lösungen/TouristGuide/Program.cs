using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace TouristGuide
{
	class Program
	{


		static Semaphore oneGuideOnly = new Semaphore(1, 1);
		static Semaphore visitorsWaiting = new Semaphore(0, 10000);
		static Semaphore memberReady = new Semaphore(0, 4);
		static Semaphore groupGo = new Semaphore(0, 4);

		static Barrier barrier = new Barrier(5);

		static void Join(String me)
		{
			Console.WriteLine("joined: " + me);
		}

		static void Visitor(int id)
		{
			visitorsWaiting.WaitOne();

			Join("Visitor" + id);

			barrier.SignalAndWait();

			//memberReady.Release();
			//groupGo.WaitOne();
		}

		static void Guide(int id)
		{
			oneGuideOnly.WaitOne();
				visitorsWaiting.Release(4);
				Join("Guide" + id);

				barrier.SignalAndWait();

				//memberReady.WaitOne();
				//memberReady.WaitOne();
				//memberReady.WaitOne();
				//memberReady.WaitOne();
				Console.WriteLine("======= Guide" + id + ": Group moving out =======");
				//groupGo.Release(4);
			oneGuideOnly.Release();
		}


		static void Main(string[] args)
		{
			Thread[] visitors = new Thread[5];
			Thread[] guides = new Thread[2];

			for (int i = 0; i < visitors.Length; i++)
			{
				int id = i;
				visitors[i] = new Thread(new ThreadStart(() => { while (true) Visitor(id); }));
				visitors[i].Start();
			}
			for (int i = 0; i < guides.Length; i++)
			{
				int id = i;
				guides[i] = new Thread(new ThreadStart(() => { while (true) Guide(id); }));
				guides[i].Start();
			}


			Semaphore s = new Semaphore(0, 1);
			s.WaitOne();	//forever



		}
	}
}
