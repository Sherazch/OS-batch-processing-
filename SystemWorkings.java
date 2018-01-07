import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;


/*
This work is a soul property and work done by fawaz
@copyrights by Fawaz
*/

public class SystemWorkings extends Object implements Serializable{
	public static final int RAMSIZE = 192;
	public static final int RAMSIZE_OS = 32;
	public static final int RAMSIZE_USER = RAMSIZE - RAMSIZE_OS;
	public static int memoryusuage = 0;
	public static int processID = 1;
	public static Queue<Task> Jobqueue = new LinkedList<Task>();
	public static Queue<Work> Readyqueue = new LinkedList<Work>();
	public static int delay = 1;
	public static boolean Start = false;
	public static int Totaltime = 0;
	public static final int LONGTERM = delay * 100;
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		File file = new File("C:\\Users\\Sheraz Ahmed\\Downloads\\workvalues.txt");
	/************* 1.Load all jobs in the jobs file into a jobs queue in memory *************/ 
	    try {

	        Scanner input = new Scanner(file);
	        Task job = new Task();
	        while (input.hasNextLine()) {
	            String line = input.nextLine();
	            String[] parts = line.split(" ");
	            if(parts.length == 1) {
	            	try {
	            	int value = Integer.parseInt(parts[0]);
	            	if(value == -1) {
	            		Jobqueue.add(job);
	            	}
	            	else if(value > 0) {
	            		ArrayList<Integer> arl = new ArrayList<Integer>();
		            	arl.add(Integer.parseInt(parts[0]));  
		            	job.Bursts.add(arl);
	            	}
	            	}
	            	catch(NumberFormatException e) {
	            		job.Processname = parts[0];
	            	}
	            }
	            else {	            
	            	ArrayList<Integer> arl = new ArrayList<Integer>();
	            	arl.add(Integer.parseInt(parts[0]));  
	            	arl.add(Integer.parseInt(parts[1]));
	            	job.Bursts.add(arl);
	            }
	        }
	        input.close();
	    } 
	    catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }
	    
	 /************* 2.Start the system clock (in milliseconds)  *************/
	    EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                ActionListener actionListener = new ActionListener() {

                    public void actionPerformed(ActionEvent actionEvent) {
                    	Totaltime += delay;
                    	Scheduling(Totaltime);
                    	if(Readyqueue.size() == 0) {
                    		System.exit(0);
                    	}
                    }
                };
                Timer Systemclock = new Timer(delay, actionListener);
                Systemclock.start();
            }
        });
	    
	  /************* 3.Start the long term scheduler that check the first job in the job queue, check if there is enough memory for it and then  *************/
	    Task firstJob = Jobqueue.element();
	    CreateProcess(firstJob,0);
	    Jobqueue.remove();
	  /************* 4.Load the RAM with the maximum number of user programs, then go to sleep for 100ms *************/
	    while(!Jobqueue.isEmpty()) {
	    	Task job = Jobqueue.element();
		    if(!CreateProcess(job,0)) {
		    	break;
		    }
		    Jobqueue.remove();
	    }
	    Thread.sleep(100);
	   
	  // 5.Start the simulation run which consists of a simulation of the Machine Execution Cycle. 
	  // At each millisecond, the scheduler will check if a job CPU-burst has ended and if the I/O 
	  // burst of a process has ended. It should also check if any waiting process can be reactivated and put in the ready queue. 
	    Start = true;
	}
	public static boolean CreateProcess(Task job,int t) {
		ArrayList<Integer> arl = job.Bursts.get(0);
		int size = arl.get(1);
		if(Hasmemory(size)) {
			allocatememory(size);
			Work process = new Work();
			process.status = STATUS.READY;
			process.Bursts = job.Bursts;
			process.ProcessID = processID ++;
			process.Programname = job.Processname;
			process.loadedtime = t;
			Readyqueue.add(process);
			return true;
		}
		return false;
	}
	public static void allocatememory(int s) {
		memoryusuage += s;
	}

	public static int Getmemoryusage() {
		return memoryusuage;
	}
	public static void Scheduling(int t) {
		if(Start) {
			if(t % LONGTERM == 0) {
				while(!Jobqueue.isEmpty()) {
			    	Task job = Jobqueue.element();
				    if(!CreateProcess(job,t)) {
				    	break;
				    }
				    Jobqueue.remove();
			    }
			}
		
			for(Work pro : Readyqueue){
				if(pro.status == STATUS.WAITING) {
					
					if(pro.wait == WAITING.IO) {
						ArrayList<Integer> arl = pro.Bursts.get(0);
						pro.runningtime += delay;
						if(pro.runningtime >= arl.get(0)) {
							pro.status = STATUS.READY;
							pro.runningtime = 0;
							pro.Bursts.remove(0);
						}
						else {
							pro.TotaltimeIO += delay;
						}
					}
					else {
						ArrayList<Integer> arl = pro.Bursts.get(0);
						int msize = arl.get(1);
						if(Hasmemory(msize)) {
							pro.status = STATUS.READY;
							pro.runningtime = 0;
							return;
						}
						pro.nTimeswaiting += delay;
					}
				}
			}
			Work current = Readyqueue.element();
			System.out.println(current.runningtime);
			if(current.status != STATUS.WAITING) {
				current.TotaltimeCPU += delay;
				if(current.status == STATUS.READY) {
					current.runningtime = 0;
					if(current.Bursts.size() == 0) {
						current.endedtime = t;
						current.status = STATUS.TERMINATED;
						current.endedtime = t;
						display(current);
						Readyqueue.remove();
						return;
					}
					ArrayList<Integer> arl = current.Bursts.get(0);
					int size = arl.size();
					if(size == 1) {
						current.status = STATUS.WAITING;
						current.nTimesinIO ++;
						current.wait = WAITING.IO;
						current.runningtime = 0;
						Readyqueue.add(current);
						Readyqueue.remove();
						return;
					}
					current.nTimesinCPU ++;
					int msize = arl.get(1);
					if(Hasmemory(msize)) {
						allocatememory(msize);
						current.status = STATUS.RUNNING;
						current.runningtime += delay;
					}
					else {
						current.runningtime = 0;
						current.status = STATUS.WAITING;
						current.wait = WAITING.MEMORY;
						Readyqueue.add(current);
						Readyqueue.remove();
						
					}
					return;
				}
				current.runningtime += delay;
				ArrayList<Integer> arl = current.Bursts.get(0);
				if(arl.get(0) <= current.runningtime) {
					current.status = STATUS.READY;
					current.runningtime = 0;
					current.Bursts.remove(0);
					Readyqueue.add(current);				
					Readyqueue.remove();
					return;
				}
				
			}
		}
	}
	public static void display(Work process) {
		Writer writer = null;

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Results.txt")));
		    writer.write("ID of the Process :   ");
		    writer.write(Integer.toString(process.ProcessID));
		    ((BufferedWriter) writer).newLine();
		    writer.write("Name of the Process :  ");
		    writer.write(process.Programname);
		    ((BufferedWriter) writer).newLine();
		    writer.write("Time to Load : ");
		    writer.write(Integer.toString(process.loadedtime));
		    ((BufferedWriter) writer).newLine();
		    writer.write("Inside CPU Time : ");
		    writer.write(Integer.toString(process.nTimesinCPU));
		    ((BufferedWriter) writer).newLine();
		    writer.write("CPU total time");
		    writer.write(Integer.toString(process.TotaltimeCPU));
		    ((BufferedWriter) writer).newLine();
		    writer.write("Input/Output Frequency : ");
		    writer.write(Integer.toString(process.nTimesinIO));	
		    ((BufferedWriter) writer).newLine();
		    writer.write("Time for IO : ");
		    writer.write(Integer.toString(process.TotaltimeIO));
		    ((BufferedWriter) writer).newLine();
		    writer.write("Waiting in memory Frequency : ");
		    writer.write(Integer.toString(process.nTimeswaiting));
		    ((BufferedWriter) writer).newLine();
		    writer.write("Termination Time : ");
		    writer.write(Integer.toString(process.endedtime));
		    ((BufferedWriter) writer).newLine();
		    writer.write("Status  [Killed/Terminated]  :::   ");
		    if(process.status == STATUS.TERMINATED) {
		    	writer.write("Terminated");
		    }
		    else {
		    	writer.write("Killed");
		    }
		    writer.write("\n");
		    
		    ((BufferedWriter) writer).newLine();

		} catch (IOException ex) {
		  // report
		} finally {
		   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
	}
	public static boolean Hasmemory(int size) {
		return (Getmemoryusage() + size) < RAMSIZE_USER * 0.9;
	}
}
