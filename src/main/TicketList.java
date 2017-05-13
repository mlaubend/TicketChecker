package main;
import java.util.ArrayList;

/***
 * 
 * @author Mark Laubender
 *
 * Linked List node containing an ArrayList. 
 */

public class TicketList{
	public ArrayList<Ticket> list = new ArrayList<Ticket>();
	public TicketList next;
	
	public TicketList(ArrayList<Ticket> list){
		this.list = new ArrayList<Ticket>();
		for (Ticket t : list){
			this.list.add(t.clone());
		}
		next = null;
	}
	
	public TicketList(){
		next = this;
	}
	
	//selection sort
	public static void sortTicketList(ArrayList<Ticket> list){
		for (int i = 0; i < list.size(); i++){
			Ticket first = list.get(i);
			int place = i;
			for (int j = i; j < list.size(); j++){
				if (Ticket.compare(list.get(j), first)){
					first = list.get(j);
					place = j;
				}
			}
			
			Ticket temp = list.get(i).clone();
			list.remove(i);
			list.add(i, first);
			list.remove(place);
			list.add(place, temp);	
		}

	}

}
