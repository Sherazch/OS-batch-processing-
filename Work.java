import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

/*
This work is a soul property and work done by fawaz
@copyrights by Fawaz
*/

enum STATUS{
	READY, WAITING, RUNNING, TERMINATED, KILLED;
}

enum WAITING{
	IO, MEMORY;
}
public class Work {
	int ProcessID;
	String Programname;
	int loadedtime = 0;
	int nTimesinCPU= 0;
	int TotaltimeCPU= 0;
	int nTimesinIO= 0;
	int TotaltimeIO= 0;
	int nTimeswaiting= 0;
	int endedtime= 0;
	Vector<ArrayList>	Bursts;
	STATUS status;
	int runningtime = 0;
	WAITING wait;
}
