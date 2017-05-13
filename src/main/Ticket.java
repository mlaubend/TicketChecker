package main;
import java.io.Serializable;

/***
 * 
 * @author Mark Laubender
 *
 * base data structure for tickets.  Contains variables for ticket number, ticket latest activity, osiris case number, and a timestamp for when the ticket structure was 
 * instantiated
 */

public class Ticket implements Serializable{
		private static final long serialVersionUID = 1L;
		public String ticket;
		public String latestActivity;
		public String osirisCase;
		public String title;
		public long timestamp = System.currentTimeMillis();
		
		public Ticket(String ticket, String latestActivity, String title){
			this.ticket = ticket;
			this.latestActivity = latestActivity;
			this.title = title;
			osirisCase = null;
		}
		
		public Ticket(){
			ticket = null;
			latestActivity = null;
			osirisCase = null;
			title = null;
		}
		
		public Ticket clone(){
			Ticket ticket = new Ticket();
			
			ticket.ticket = this.ticket;
			ticket.latestActivity = this.latestActivity;
			ticket.osirisCase = this.osirisCase;
			ticket.timestamp = this.timestamp;
			
			return ticket;
		}
		
		/**
		 * comparator for Ticket class
		 * @param o1 first ticket to compare
		 * @param o2 second ticket to compare
		 * @return true if o1 < o2
		 */
		public static boolean compare(Ticket o1, Ticket o2) {
			try{
				String[] split1 = o1.ticket.split("-");
				String[] split2 = o2.ticket.split("-");	
				
				if (Integer.parseInt(split1[1]) == Integer.parseInt(split2[1])){
					return true;
				}
				else return false;
			} catch(Exception e){
				return false;
			}
		}
}

