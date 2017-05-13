package main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/***
 * 
 * @author Mark Laubender
 *
 * executes a python script to return ticket data from Jira using the Jira API.  Reads data into a TicketQueue data structure. Multithreaded.
 */

public class BBoardTicketChecker implements Multithreaded{
	public TicketQueue jira = new TicketQueue();
	
	public BBoardTicketChecker(){
		
	}
	
	/**
	 * load JIRA tickets to jira TicketQueue from python script, script does all the sorting beforehand 
	 */
	private void loadJiraTicketsToLocalList(){
		String line = null;
		
		try{
			Process process = Runtime.getRuntime().exec("python jiraapi.py");
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
			TicketList list = jira.updatedTickets;
			
			while ((line = stdIn.readLine()) != null){
				if (line.equals("<--->")){
					list = list.next;
					continue;
				}
				String split[] = line.split(" | "); //for some reason I need the whitespace before and after the token...
				try{
					split[1] = "Update by " + split[2] + " " + split[3];
				} catch(Exception e){
					split[1] = "";					
				}

				Ticket ticket = new Ticket(split[0], split[1], null);
				ticket.osirisCase = "No case";
				list.list.add(ticket);
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	@Override
	public TicketQueue getTicketQueue(){
		return jira;
	}

	@Override
	public void run() {
		loadJiraTicketsToLocalList();
	}
}
