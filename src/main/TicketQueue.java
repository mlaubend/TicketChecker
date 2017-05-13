package main;

/***
 * 
 * @author Mark Laubender
 *
 * data structure for each ticket Queue (IT, Jira, etc...).  Contains a LL for updated, closed, and not updated tickets 
 */

public class TicketQueue {
	public TicketList updatedTickets;
	public TicketList closedTickets;
	public TicketList notUpdatedTickets;

	public TicketQueue next;
	
	public TicketQueue(){
		updatedTickets = new TicketList();
		closedTickets = new TicketList();
		notUpdatedTickets = new TicketList();
		
		updatedTickets.next = closedTickets;
		closedTickets.next = notUpdatedTickets;
		notUpdatedTickets.next = null;
		
		next = null;
	}
	
	public void add(TicketQueue queue){
		while (next != null)
			next = next.next;
		next = queue;
	}

}
