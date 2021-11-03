import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;


public class Scheduler{
	private HashMap<Long,PCB> pcbInfo = new HashMap<>();
	//Optimized by the scheduler. Priorities are determined whether if its a sys operation or I/O. This is a subset of the processes queue
	private Queue<Process> readyQueue;
	private Queue<Process> waitingQueue;
	public Boolean quantumStatus = true;
		
	Scheduler(){
		this.readyQueue = new PriorityQueue<>((p1,p2)->{
			return pcbInfo.get(p2.getPID()).getPriority() - pcbInfo.get(p1.getPID()).getPriority();
		});
		this.waitingQueue = new PriorityQueue<>((p1,p2)->{
			return pcbInfo.get(p2.getPID()).getPriority() - pcbInfo.get(p1.getPID()).getPriority();
		});
	}
	public void addToPQ(Process p) {
		this.readyQueue.add(p);
	}
	public void addPCBInfo(PCB pcb) {
		this.pcbInfo.put(pcb.getProcessPID(), pcb);
	}
	public Process getProcess() {
		return this.readyQueue.poll();
	}
	public void setReadyQueue(Queue<Process> rq, HashMap<Long,PCB> pcbList) {
		this.readyQueue = rq;
		this.pcbInfo = pcbList;
	}
	public Queue<Process> getReadyQueue(){
		return this.readyQueue;
	}

	public void setWaitingQueue(Queue<Process> waitingQueue, HashMap<Long,PCB> pcbList) {
		this.waitingQueue= waitingQueue;
		this.pcbInfo = pcbList;
		
	}
	public Queue<Process> getWaitingQueue(){
		return this.waitingQueue;
	}
	public Boolean getQuantumStatus() {
		return this.quantumStatus;
	}
	public void startQuantumClock() {
		quantumStatus = true;
		Timer timer = new Timer();
		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				quantumStatus = true;
				timer.cancel();
			}
			
		};
		timer.schedule(tt, 4);
		
		
		
	}

}
/*
new Comparator<Process>() {

@Override
public int compare(Process p1, Process p2) {
	if(pcbInfo.get(p1.getPID()).getPriority() < pcbInfo.get(p2.getPID()).getPriority() ) {
		return -1;
	}
	if(pcbInfo.get(p1.getPID()).getPriority() > pcbInfo.get(p2.getPID()).getPriority()) {
		return 1;
	}
	return 0;
}

}*/