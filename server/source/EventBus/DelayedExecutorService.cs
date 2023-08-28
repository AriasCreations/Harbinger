using Harbinger.EventsBus.Events;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Harbinger.EventsBus
{
    public class DelayedExecutorService
    {
        private static CancellationTokenSource cancel;
        private static List<Runner> taskList = new();
        [Subscribe(Priority.Very_High)]
        public static void onTick(ServerTickEvent evt)
        {
            if (cancel.IsCancellationRequested)
            {
                return;
            }

            foreach(Runner runner in taskList.ToArray())
            {
                if(runner.tick())
                {
                    if (!runner.Repeats)
                    {
                        taskList.Remove(runner);
                    }
                    else runner.resetTick();

                    runner.run();
                }
            }

        }

        public static void setCancellationToken(CancellationTokenSource source)
        {
            cancel = source;
        }

        [Subscribe(Priority.High)]
        public static void onStart(StartupEvent evt)
        {
            EventBus.PRIMARY.Scan(typeof(DelayedExecutorService));
        }

        public static void Schedule(Runner task)
        {
            taskList.Add(task);

            task.resetTick();
        }

        public static void ScheduleRepeating(Runner task)
        {
            taskList.Add(task);
            task.Repeats = true;

            task.resetTick();
        }

        public static void Cancel(Runner task)
        {
            taskList.Remove(task);
        }
    }

    /// <summary>
    /// Contains data for a task execution
    /// </summary>
    public class Runner
    {
        /// <summary>
        /// Ticks down in milliseconds until the task executes.
        /// </summary>
        public TimeSpan executeAfter;

        private int milliseconds;
        public void resetTick()
        {
            milliseconds = executeAfter.Milliseconds;
        }

        public bool tick()
        {
            milliseconds -= ServerTickEvent.TickMilliseconds;
            if (milliseconds <= 0) return true;
            else return false;
        }

        /// <summary>
        /// The task to execute
        /// </summary>
        public ThreadStart start;

        /// <summary>
        /// Runner name
        /// </summary>
        public string Name;

        /// <summary>
        /// Repeating task until server termination
        /// </summary>
        public bool Repeats = false;

        /// <summary>
        /// Is set by the internal task
        /// </summary>
        public bool Finished = true;

        public void run()
        {
            if (!Finished) return;

            Finished = false;
            Thread X = new Thread((Z) =>
            {
                if(Z is Runner ts)
                {
                    Thread N = new Thread(ts.start);
                    N.Start();
                    N.Join();

                    ts.setDone();
                }
            });
            X.Start(this);
        }

        /// <summary>
        /// Executed by the internal task when execution has finished
        /// </summary>
        public void setDone()
        {
            Finished = true;
        }
    }
}
