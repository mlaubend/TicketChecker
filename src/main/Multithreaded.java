package main;

/***
 * 
 * @author Mark Laubender
 *
 * created to ease extensibility when adding another Ticket Queue to Main
 */

public interface Multithreaded extends Runnable{

	public TicketQueue getTicketQueue(); //needed to instantiate the LL's in main
}
