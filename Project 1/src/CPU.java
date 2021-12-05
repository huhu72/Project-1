import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

//move all process created relations to the process class/main class.
//Wait for all processes to be put in the ready queue and then start running them.
//Same logic applies to the Dispatcher(Wait for all processes to be created before Dispatcher puts them in the ready queue and Scheduler organizes them).

public class CPU extends Thread {
	static Boolean inCS = false;
	static Boolean waitingForCS = false;
	private static Queue<Process> processes = new LinkedList<>();// AKA job queue. where does this go if the input for
																	// the
	// Scheduler is only the readyQueue

	// private Queue<Process> deviceQueue = new LinkedList<>();//FIFO. after pcb
	// gets processed, it goes back to the ready queue and the deviceQueue runs the
	// next process
	private static HashMap<Long, PCB> pcbList = new HashMap<>();
	Scanner input = new Scanner(System.in);
	static Timer t = new Timer();
	static Process processInCS;
	static int counter = 0;
	Thread processThreadArray[] = new Thread[4];

//CPu tells Dispatcher there are no processes running, from that, the Dispatcher should send another profess from the Scheduler to the cpu
	// When process is recieved from Dispatcher, remove its PCB and itself from the
	// jobQueue and pcblist
	@Override
	public void run() {

		Runnable process;

		// System.out.println("running");
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				Iterator<PCB> it = pcbList.values().iterator();

				while (it.hasNext()) {
					it.next().scheduleInfo.incrementPriority();
				}
				// System.out.println("Priority has been incremented");
			}

		};
		CPU.t.scheduleAtFixedRate(tt, 10000, 10000);
		if (CPU.inCS) {
			processThreadArray[0] = new Thread(dispatchProcess());
			for (int i = 1; i < 4; i++) {
				processThreadArray[i] = new Thread(dispatchProcess());
			}
			for (Thread t : processThreadArray) {
				t.start();
			}
		} else {
			for (int i = 0; i < 4; i++) {
				processThreadArray[i] = new Thread(dispatchProcess());
			}
			for (Thread t : processThreadArray) {
				t.start();
			}
		}
		/*
		 * while (!Dispatcher.getReadyQueue().isEmpty() ||
		 * !Dispatcher.getWaitingQueue().isEmpty()
		 * 
		 * || !Semaphore.list.isEmpty()) {
		 * 
		 * ArrayList<Runnable> runnableProcesses = new ArrayList<>();
		 * System.out.println("                                                        "
		 * + counter); counter++;
		 */
		/*
		 * If theres a process in its critical section, it needs to add it back into a
		 * thread so that it can continue running its critical section
		 */

		/*
		 * if (CPU.inCS) { runnableProcesses.add(dispatchProcess()); //
		 * System.out.println(runnableProcesses.get(0).getPriority()); for (int i = 0; i
		 * < 3; i++) { process = dispatchProcess();
		 * 
		 * if (process != null) runnableProcesses.add(process); //
		 * System.out.println(runnableProcesses.get(i+1)); } } else { for (int i = 0; i
		 * < 4; i++) { process = dispatchProcess();
		 * 
		 * if (process != null) runnableProcesses.add(process); //
		 * System.out.println(runnableProcesses.get(i)); } } for (int i = 0; i <
		 * runnableProcesses.size(); i++) { processThread = new
		 * Thread(runnableProcesses.get(i)); processThread.start();
		 * 
		 * print(); }
		 */

		// print();
	}

	public static Runnable dispatchProcess() {
		Runnable runnableProcess;
		Process process;
		// Grabs from the ready queue if the waiting queue is empty(Usually when the cpu
		// is first initiated)
		if (Dispatcher.getWaitingQueue().isEmpty() && !Dispatcher.getReadyQueue().isEmpty()) {
			System.out.println(
					"                                                                                                                                 from the ready queue");
			process = Dispatcher.getProcess();
			runnableProcess = process;
			if (process != null) {
				System.out.println("						The waiting queue is empty, grabbing "
						+ process.getProcessName() + " from the ready queue");
				return runnableProcess;
			} else {
				return null;
			}

			// System.out.println("Grabbing " + process.getProcessName() + " from the ready
			// queue");
			/*
			 * If both the ready and waiting queue is empty, then signal the semahpore so
			 * that it can remove it from its own waiting queue and populate it into the
			 * ready queue. This is needed just in-case there aren't enough signal calls to
			 * pull all the Processes out of its own waiting queue
			 */
		} else if (Dispatcher.getWaitingQueue().isEmpty() && Dispatcher.getReadyQueue().isEmpty()) {
			System.out.println(
					"                                                                                                                                 from the ready queue bc signal");
			Semaphore.signal();
			process = Dispatcher.getProcess();
			runnableProcess = process;
			if (process != null) {
				System.out.println("						The waiting and ready queue is empty is empty, grabbing "
						+ process.getProcessName() + " from the ready queue after calling signal");
				return runnableProcess;
			} else {
				return null;
			}
			/*
			 * Grabs the process in the waiting queue since the queue isnt empty
			 */
		} else {
			System.out.println(
					"                                                                                                                                 from the wait queue");

			process = Dispatcher.getProcessFromWaitingQueue();
			runnableProcess = process;
			if (process != null) {
				System.out.println(
						"						Grabbing " + process.getProcessName() + " from the wating queue");
				return runnableProcess;
			} else {
				return null;
			}

		}

	}

	public static void runProcesses(Process process) throws InterruptedException {
		PCB pcb;
		ArrayList<Command> commands;
		int CS;
		int CE;

		// The PCB should be updated from the Dispatcher class once its sent over there
		// This is so that the current pcb has information on the counters from when it
		// was sent to the Dispatcher
		pcb = CPU.pcbList.get(process.getPID());
		pcb.setState(STATE.RUN);
		commands = process.getCommands();
		CS = process.getCritStart();
		CE = process.getCritEnd();

		if (commands.get(0).command.equals("I/O") && pcb.programCounter.getCommandCounter() == 1) {
			System.out.println(process.getProcessName()
					+ " has been sent to the waiting queue because the first command is a I/O instruction");
			Dispatcher.addToWaitingQueue(process, pcb);

		}

		System.out.println("Running " + process.getProcessName());

		Scheduler s = new Scheduler();
		s.run(process);
		/*
		 * while(s.getQuantumStatus()) { if (pcb.programCounter.getCommandCounter() ==
		 * CS && CPU.processInCS != process) { if (!CPU.inCS) { Semaphore.wait(process);
		 * CPU.inCS = true; s.killQuantumTimer(process); CPU.processInCS = process;
		 * System.out.println(process.getProcessName() +
		 * " is now in CS and timer should be killed"); } else {
		 * Semaphore.wait(process); CPU.waitingForCS = true; break;
		 * 
		 * }
		 * 
		 * } }
		 */
		while (s.getQuantumStatus()) {
			if (inCS && Semaphore.list.contains(process)) {
				System.out.println("						There is a process in cs");
				break;
			}
			if (pcb.programCounter.getCommandCounter() == CS && pcb.programCounter.getCyclesRan() == 0) {
				Semaphore.wait(process);
				s.killQuantumTimer(process);
				if(Semaphore.list.contains(process)) {
					break;
				}else {
				CPU.inCS = true;
				}

			}
			// If the process runs all of the cyles in the command, it will increament the
			// program counter
			// and exit out of the loop
			if (pcb.programCounter.getCyclesRan() == commands.get(pcb.programCounter.getCommandCounter() - 1).cycle) {

				// System.out.println("Timer should be killed");
				s.killQuantumTimer(process);
				pcb.programCounter.incrementProgramCounter();
				pcb.programCounter.setCyclesRan(0);
				pcbList.put(pcb.getProcessPID(), pcb);
				System.out.println(process.getProcessName() + " has ran all cycles before the quantum time ran out and "
						+ process.getProcessName() + " timer has been killed");
				if (pcb.programCounter.getCommandCounter() == CE) {
					System.out.println("												"
							+ pcb.getProcess().getProcessName() + " has called signal()");
					Semaphore.signal();
					CPU.inCS = false;
					System.out.println(process.getProcessName() + " is out of CS");
					CPU.pcbList.put(pcb.getProcessPID(), pcb);
				} else {
					break;
				}
			} else {
				System.out.println(
						"Command for " + process.getProcessName() + ": " + pcb.programCounter.getCommandCounter());
				pcb.programCounter.incrementProgramCycle();
				CPU.pcbList.put(pcb.getProcessPID(), pcb);
				// System.out.println("On " + pcb.programCounter.getCyclesRan() + "/"
				// + commands.get(pcb.programCounter.getCommandCounter() - 1).cycle + " cycle");
			}

		}
		if (!Semaphore.list.contains(pcb.getProcess())) {

			// If the process is out of commands to run
			if (pcb.programCounter.getCommandCounter() > commands.size()) {
				pcb.setState(STATE.EXIT);
				CPU.pcbList.put(pcb.getProcessPID(), pcb);
				// System.out.println(pcb.getChildPID());
				if (pcbList.get(pcb.getChildPID()) != null) {
					PCB childPCB = pcbList.get(pcb.getChildPID());
					childPCB.programCounter.setCounter(3);
					childPCB.setState(STATE.EXIT);
					CPU.pcbList.put(childPCB.getProcessPID(), childPCB);
					System.out.println(process.getProcessName() + " and its child "
							+ childPCB.getProcess().getProcessName() + "has been terminated");
				} else {
					System.out.println(process.getProcessName() + " been terminated");
				}
				if (!Dispatcher.getReadyQueue().isEmpty() || !Dispatcher.getWaitingQueue().isEmpty()
						|| !Semaphore.list.isEmpty()) {
					System.out.println("						Thread is being re assigned from the terminated stage");
					Runnable rp = dispatchProcess();
					System.out.println(rp);
					Thread currentThread = Thread.currentThread();
					currentThread = new Thread(rp);
					currentThread.start();
				}
				// if it ran all its cycles but not all of the commands, add it to the respected
				// queue based on the next command
			}
			if (CPU.pcbList.get(process.getPID()).getState() != STATE.EXIT) {
				Command nextCommand = commands.get(pcb.programCounter.getCommandCounter() - 1);

				if (pcb.programCounter.getCyclesRan() < nextCommand.cycle) {

					if (nextCommand.command.equals("I/O")) {
						System.out.println(process.getProcessName()
								+ " has been sent to the waiting queue because the next command is an I/O command");
						Dispatcher.addToWaitingQueue(process, pcb);
					} else {
						// if there are still cycles to be ran in the current command and the command is
						// Calculate
						// Dont need to increment the program counter if the process still have cycles
						// to run but the pcb needs to be updated
						// in the Dispatcher class
						System.out.println(process.getProcessName()
								+ " has been sent to the ready queue because the next command is an calculate command");
						Dispatcher.addToReadyQueue(process, pcb);
					}
				}
				// Process is on its last command
				else {
					if (commands.get(pcb.programCounter.getCommandCounter()) != null) {

						pcb.programCounter.setCyclesRan(0);
						if (nextCommand.command.equals("CALCULATE")) {
							System.out.println(process.getProcessName()
									+ " is on its last command and has been sent to the ready queue based on the next command");
							Dispatcher.addToReadyQueue(process, pcb);
							// break;
						} else {
							System.out.println(process.getProcessName()
									+ " is on its last command has been sent to the waiting queue");
							Dispatcher.addToWaitingQueue(process, pcb);
							// break;
						}
					}

				}
				System.out.println("						Thread is being re assigned");
				Thread currentThread = Thread.currentThread();
				currentThread = new Thread(dispatchProcess());
				currentThread.start();
			}
			/*
			 * System.out.println("finished"); System.out.println("The ready queue: " +
			 * Dispatcher.getReadyQueue()); System.out.println("The waiting queue: " +
			 * Dispatcher.getWaitingQueue());
			 */
			// }
			// in a couple of cycles, increment the priorities of all processes
			// break;
			// System.out.println("all processes have been terminated");
			t.cancel();
			print();
		}

	}

	public static void print() {
		System.out.println("All processes:");
		for (Process p : processes) {
			double percentage = (((double) CPU.pcbList.get(p.getPID()).programCounter.getCommandCounter() - 1)
					/ p.getCommands().size()) * 100;
			System.out.println(p.getProcessName() + " " + percentage + "% completed");
			System.out.println("     State: " + CPU.pcbList.get(p.getPID()).getState());
		}
	}

	public static PCB getPCB(Long pid) {
		return CPU.pcbList.get(pid);
	}

	public void addToProcessQueue(Process p) {
		this.processes.add(p);

		// System.out.println(Scheduler.getProcess());

		/*
		 * for(Process process : processes) {
		 * System.out.println(getPCB(process.getPID()));
		 * //System.out.println("PID under CPU:" + process.getPID()); }
		 */

	}

	public void addPCB(PCB pcb) {
		this.pcbList.put(pcb.getProcessPID(), pcb);

	}

	public HashMap<Long, PCB> getPCBList() {
		return this.pcbList;
	}

	public static void updatePCBList(Process p, PCB pcb) {
		CPU.pcbList.put(p.getPID(), pcb);
	}

	public Queue<Process> getJobQueue() {
		return this.processes;
	}

	public void setPCBList(HashMap<Long, PCB> pcbList) {
		CPU.pcbList = pcbList;
	}

	public static void updatePCBList(PCB pcb) {
		CPU.pcbList.put(pcb.getProcessPID(), pcb);
	}

}
