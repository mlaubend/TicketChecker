package main;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter; //if I'm using FileWriter do I even need this?
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/***
 * 
 * @author Mark Laubender
 *
 * Contains the main method.  Responsible for all reading/writing from/to the wiki file.  Displays all Jira tickets and Osiris cases as a link on
 * the wiki page
 */

public class Main {
	/*
	 * to extend with a new ticket queue, instantiate the queue in ticketCheckerList below
	 * order must match the order in which it is printed to the wikiFile.  
	 * please don't touch anything else...
	 */

	private Multithreaded[] ticketCheckerList = {	new ITTicketChecker(),
													new JiraTicketChecker()
													//extend here
												};

	public Main(){

	}
	
	private void createThreads(ArrayList<Thread> threads){
		for (int i = 0; i < ticketCheckerList.length; i++) {
			Thread thread = new Thread(ticketCheckerList[i], "thread " + i);
			threads.add(thread);
		}
	}
	
	private static void destroyThreads(ArrayList<Thread> threads){
		threads.removeAll(threads);
	}
		
	private static void readInWikiFile(ArrayList<String> wikiFile){
		try {
			//TODO: change filepath once on the wiki server
			FileInputStream in = new FileInputStream(path);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			
			String line = null;
			try {
				while ((line = br.readLine()) != null){
					wikiFile.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}		
	}
	
	//TODO: refactor (state pattern?)
	/**
	 * add relevant information to wikifile (in memory only).  takes the starting TicketQueue ('queueStart') and iterates through its TicketList LL, 
	 * inserting relevant data at specified places until TicketLists are exhausted.  Then iterates to the next TicketQueue and repeats, until TicketQueue's are exhausted.
	 */
	private void writeToWikiFile(){
		ArrayList<String> wikiFile = new ArrayList<String>();
		boolean jiraSection = false;
		int lineCount = 0;
		
		readInWikiFile(wikiFile);
		TicketQueue queueStart = ticketCheckerList[0].getTicketQueue();
		
		for (int i = 1; i < ticketCheckerList.length; i++)
			queueStart.add(ticketCheckerList[i].getTicketQueue());

		
		while (queueStart != null){			
			TicketList list = queueStart.updatedTickets;
			
			for (int i = lineCount; i < wikiFile.size(); i++){
				if (list == null)
					break;

				if (wikiFile.get(i).contains("<h2")){
					int index = 5; //jumping over header info
					
					
					for (Ticket ticket : list.list){
						String osirisCase = " ";
						String insert = null;
						
						if (ticket.osirisCase == null)
							ticket.osirisCase = "No case"; //TODO: don't need this anymores
						
						if (!ticket.osirisCase.equals("No case"))
							osirisCase = String.format("<a target='_blank' href=https://url/#incidents/%s>%s</a>", //[[https://osiris.snei.sony.com/#incidents/%s][%s]]", 
									ticket.osirisCase, ticket.osirisCase);
				
						if (jiraSection){
							insert = String.format("| <a target='_blank' href=https://url/browse/%s>%s</a> | %s |",//"| [[https://jira.smss.sony.com/browse/%s][%s]] | %s |", 
									ticket.ticket, ticket.ticket, ticket.latestActivity);
						}
						else{
							insert = String.format("| <a target='_blank' href=https://url/%s>%s</a> | %s | %s |",//"| [[https://jira.snei.sony.com/servicedesk/customer/portal/2/%s][%s]] | %s | %s |",
									ticket.ticket, ticket.ticket, osirisCase, ticket.latestActivity);								
						}
						wikiFile.add(i+index, insert);
						index++;
					}
				}
				else continue;
				list = list.next;
				lineCount = i + 1;	//+1 because we want to start inserting AFTER the current line
			}//end for
			jiraSection = true; //TODO: Don't like this... break into two methods?
			queueStart = queueStart.next;
		}//end while
		
		//do the actual writing to the wiki text file
		PrintWriter writer = null;
		try {
			//TODO: change filepath once on wiki server - /var/www/wiki/data/SOC_Main/TicketChecker.txt
			writer = new PrintWriter(path, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e){
			e.printStackTrace();
		}

		for (String string : wikiFile)
			writer.println(string);
		writer.close();
	}
				
	public static void main(String[] args){
			Main m = new Main();
			ArrayList<Thread> threads = new ArrayList<Thread>(); //I think I save about 2 seconds doing it this way, maybe not worth it...
			
			m.createThreads(threads);
						
			try{
				for (Thread thread : threads){
					thread.start();
					thread.join();
				}
			} catch (InterruptedException ie){
				ie.printStackTrace();
			}
			Main.destroyThreads(threads);
			
			m.writeToWikiFile();			
	}
}
