package main;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/***
 * 
 * @author Mark Laubender
 * 
 *  gathers open tickets from the Jira IT Portal through a selenium webdriver, compares it to the last open ticket dataset and sorts IT Tickets that are recently updated, 
 *  closed, or in need of a status update.  Displays the IT Ticket and Osiris case as a link.  Multithreaded.
 *
 ***/

public class ITTicketChecker implements Multithreaded{
	WebDriver driver;
	public TicketQueue IT = new TicketQueue(); 
	private ArrayList<Ticket> oldTicketList = new ArrayList<Ticket>();
	private ArrayList<Ticket> notUpdatedMetricsList = new ArrayList<Ticket>();
		
	public ITTicketChecker() {
		driver = getWebDriver();
	}
	
	private static WebDriver getWebDriver() {
		DesiredCapabilities cap = new DesiredCapabilities();
		String[] noLogsArgs = new String[] { "--webdriver-loglevel=NONE" };
		cap.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, noLogsArgs);
		cap.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, "/usr/local/bin/phantomjs");
		return new PhantomJSDriver(cap);
		
		//TODO: delete
		//debugging
		//chromedriver is not headless - I can see what selenium is actually doing in the web browser
//		System.setProperty("webdriver.chrome.driver", "/Library/Java/Libraries/selenium-2.49.0/chromedriver"); //for chromedriver
//		return new ChromeDriver();	
	}
	
	/**
	 * 
	 * @param name the name of a web element this method will wait on to render 
	 * @param by the type of web element; either class or name
	 * @return true when the chosen web element has finished rendering
	 */
	private Boolean waitToRenderCurrentPage(String name, String by){
		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d){
				if (by.equals("className")){
					if (d.findElement(By.className(name)) != null)
						return true;
					return false;
				}
				else if (by.equals("name")){
					if (d.findElement(By.name(name)) != null)
						return true;
					return false;	
				}
				return false;
			}
		});
		return false;
	}
	
	//TODO: roll these up into one, something wacky about the generics in readInListFromFile()
	@SuppressWarnings("unchecked")
	private  void readOldTicketListFromBackupFile(){
		try{
			FileInputStream fileIn = new FileInputStream("backupOldTicketList.ser");
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			try {
				oldTicketList = (ArrayList<Ticket>) objectIn.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			objectIn.close();
			fileIn.close();
		} catch (IOException ioe){
			driver.quit();
			ioe.printStackTrace();
		}	
	}

	@SuppressWarnings("unchecked")
	private void readOldTicketListFromFile(){
		try{
			FileInputStream fileIn = new FileInputStream("oldTicketList.ser");
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			try {
				oldTicketList = (ArrayList<Ticket>) objectIn.readObject();
			} catch (ClassNotFoundException e) {
				driver.quit();
				e.printStackTrace();
			}
			objectIn.close();
			fileIn.close();
		} catch (IOException ioe){
			readOldTicketListFromBackupFile();
			ioe.printStackTrace();
		}	
	}
	
	@SuppressWarnings("unchecked")
	private void readNotUpdatedMetricsListFromFile(){
		try{
			FileInputStream fileIn = new FileInputStream("notUpdatedMetricsList.ser");
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			try {
				notUpdatedMetricsList = (ArrayList<Ticket>) objectIn.readObject(); 
			} catch (ClassNotFoundException e) {
				driver.quit();
				e.printStackTrace();
			}
			objectIn.close();
			fileIn.close();
		} catch (IOException ioe){
			ioe.printStackTrace();
		}	
	}
	
	private void login(){
		driver.navigate().to(url);
		
		WebElement username = driver.findElement(By.name("os_username"));
		WebElement password = driver.findElement(By.name("os_password"));
		WebElement login = driver.findElement(By.id("js-login-submit"));
		
		username.sendKeys(username);
		char[] array = {'p','a','s','s','w','o','r','d'};
		String d1a = new String(array);
		password.sendKeys(d1a);
		d1a = null; 
		Arrays.fill(array, '0');
		login.click();
		
		waitToRenderCurrentPage("sd-header-my-requests", "className");
	}
		
	private Boolean navigateToNextPage(String page, int pageCount){
		String path = null;
		switch (page) {
			case "open": path = url + pageCount;
				break;
			case "closed": path = url + pageCount;
				break;
		}
		
		driver.navigate().to(path);
		waitToRenderCurrentPage("requests-list", "className");
		
		if (driver.findElement(By.className("requests-list")).getText().equals("No open requests found"))
			return false;

		//check if page contains no tickets
		if ((driver.findElements(By.className("request-list-item")).size() != 0)) {
			return true;
		} else return false;
	}
	
	/**
	 * fills a Ticket structure with relevant data and adds it to the correct list
	 * @param status Whether the ticket is open or closed	//hmm, I actually don't need this anymore...
	 * @param list list to add the newly created ticket to
	 */
	private void getData(String status, ArrayList<Ticket> list){	
		ArrayList<WebElement> elements = new ArrayList<WebElement>(); 
		elements.addAll(driver.findElements(By.className("request-list-item")));
			
		for (WebElement element : elements){
			//dealing with AJAX bullshit, the webpage gets 3 attempts to load AJAX.
			int attempts = 0; 
			while (attempts < 3){
				try{
					Ticket thisTicket = new Ticket(	element.findElement(By.className("cv-request-item")).getAttribute("data-request-key"), 
													element.findElement(By.className("latest-activity-summary")).getText(), 
													element.findElement(By.className("cv-request-name")).getText());
					list.add(thisTicket);
					break;
				} catch (StaleElementReferenceException sere){
					sere.printStackTrace();
				}
				attempts++;
			}
		}
	}
	
	//I need to update the old ticket list with the new tickets because I need to keep the correct timestamps - can't just rewrite old ticket list with new ticket list
	//WARNING: This is the heart of this program, if you mess with this you WILL break something
	/**
	 * takes the list of tickets and categorizes them into: recently updated, recently closed, and tickets in need of an update  						
	 * @param queue the queue that will be sorted into updated, closed, and not updated
	 */
	
	/*
	 * 		pseudocode:
	 * 			if ticket from old queue is in new queue:
	 * 				if latest activity are the same:
	 * 					if delta timestamp (from now) is more than a week:
	 * 						there's been no movement in a week -> move ticket to not updated list
	 * 				else it's been updated -> move ticket to updated list
	 * 			else it's been closed -> move ticket to closed list
	 * 
	 *  		what's left in new queue are newly created tickets -> move to old queue for the next round

	 */
	private void categorize(TicketQueue queue){
		int pageCount = 1;
		ArrayList<Ticket> openTickets = new ArrayList<Ticket>();
		
		//collect data from the IT web portal
		while (navigateToNextPage("open", pageCount)){
			getData("open", openTickets);	
			pageCount++;
		}	
				
		//do the actual sorting
		for (int i = 0; i < oldTicketList.size(); i++){
			Ticket oldTicket = oldTicketList.get(i);	
			boolean isClosed  = true;
					
			for (int j = 0; j < openTickets.size(); j++) {
				Ticket currentTicket = openTickets.get(j);
				
				//if old ticket is in new ticket list, check if recently updated -> else it's closed
				if (oldTicket.ticket.equals(currentTicket.ticket)){ 
					isClosed = false;
	
					//check if updated within the past 7 days
					//if old list latest activity equals new list latest activity AND the timestamp is more than 7 days, it's not updated -> add to notUpdated list
					if (oldTicket.latestActivity.equals(currentTicket.latestActivity)){ 
						if (System.currentTimeMillis() - oldTicket.timestamp > 7*24*60*60*1000L){
							
							//add ticket to not updated metrics
							Ticket t = containsTicket(oldTicket); //do NOT try and combine with the next line
							if (t == null)
								notUpdatedMetricsList.add(oldTicket);
							
							oldTicket.timestamp = System.currentTimeMillis();
							queue.notUpdatedTickets.list.add(oldTicket);							
						}
					}
					//add to recently updated ticket list
					//we need to update the ticket in oldTicketList to the recently updated info
					else { 
							if (!currentTicket.latestActivity.contains(team name)){
								if (!currentTicket.latestActivity.contains("Request")){
									queue.updatedTickets.list.add(currentTicket);
									//update oldTicketList with the new activity and timestamp
									//TODO: I may need to move the next 2 lines out of the if statement...
									oldTicketList.get(i).latestActivity = currentTicket.latestActivity;
									oldTicketList.get(i).timestamp = currentTicket.timestamp;									
								}
							}
						
						//metrics
						Ticket t = containsTicket(currentTicket);
						if (t != null && !t.latestActivity.contains(team name))
							notUpdatedMetricsList.remove(t);
					}
					openTickets.remove(j); //removing non-updated tickets from openTickets list
					j--;
					break;
				}
			}
			
			//add to recently closed ticket list if no match above
			if (isClosed){
				//we need to remove from oldTicketList once a ticket has been closed
				queue.closedTickets.list.add(oldTicket);
				oldTicketList.remove(i); //removing closed tickets from openTickets list
				i--;
				
				//metrics
				Ticket t = containsTicket(oldTicket);
				if (t != null)
					notUpdatedMetricsList.remove(t);
			}
		}
		
		//at this point we should have an openTickets.list solely of newly created tickets - add them to the old ticket list
		for (Ticket ticket : openTickets){
			if (oldTicketList.contains(ticket)) //double check
				continue;
			oldTicketList.add(ticket);
			queue.updatedTickets.list.add(ticket); 
		}
	}
	
	private Ticket containsTicket(Ticket t){
		for (Ticket ticket : notUpdatedMetricsList){
			if(Ticket.compare(t, ticket))
				return ticket;
		}
		return null;
	}
		
	private void getOsirisCase(){
		GetOsirisCase getCases = new GetOsirisCase();
		TicketList list = IT.updatedTickets;
		
		getCases.getOsirisCases();
		
		while (list != null){
			for (int i = 0; i < list.list.size(); i++){
				Ticket ticket = list.list.get(i);
				
				if (getCases.map.containsKey(ticket.ticket) && (getCases.map.get(ticket.ticket) != null))
					ticket.osirisCase = getCases.map.get(ticket.ticket);
				else ticket.osirisCase = " ";			
			}	
			list = list.next;
		}	
	}
	
	private void writeOldTicketListToFile(){
		try{
			FileOutputStream fileOut = new FileOutputStream("oldTicketList.ser");
			FileOutputStream backupFileOut = new FileOutputStream("backupOldTicketList.ser");
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			ObjectOutputStream backupObjectOut = new ObjectOutputStream(backupFileOut);
			objectOut.writeObject(oldTicketList);
			backupObjectOut.writeObject(oldTicketList);
			objectOut.close();
			backupObjectOut.close();
			fileOut.close();
			backupFileOut.close();
		} catch (IOException ioe){
			driver.quit();
			ioe.printStackTrace();
		}
	}
	
	private void writeNotUpdatedMetricsListToFile(){
		try{
			FileOutputStream fileOut = new FileOutputStream("notUpdatedMetricsList.ser");
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(notUpdatedMetricsList);
			objectOut.close();
			fileOut.close();			
		} catch (IOException ioe){
			driver.quit();
			ioe.printStackTrace();
		}
	}
		
	@Override
	public TicketQueue getTicketQueue(){
		return IT;
	}
		
	@Override
	public void run() {
		readOldTicketListFromFile();
		readNotUpdatedMetricsListFromFile();
					
		try{
			login();
//			test();
			categorize(IT);
			getOsirisCase();
		}
		catch(Exception e){
			driver.quit(); //driver needs to quit on fail, else it will stay open forever
			e.printStackTrace();
		}	
		driver.quit();
		writeOldTicketListToFile();
		writeNotUpdatedMetricsListToFile();	
	}
}

//my graveyard

/*		
private void writeListToFile(String fileName, ArrayList<?> list){
	try{
		FileOutputStream fileOut = new FileOutputStream(fileName);
		ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
		objectOut.writeObject(list);
		objectOut.close();
		fileOut.close();				
	} catch (IOException ioe){
		driver.quit();
		ioe.printStackTrace();
	}
}

public void getClosedTicketsLatestActivity(){
	//since all tickets in the closedTickets list come from oldTicketList, the latestActivity will not be current
	//TODO:do I even need this?
}
*/	

/*	
@SuppressWarnings("unchecked")
private void readInListFromFile(String fileName, ArrayList<?> list){
	try{
		FileInputStream fileIn = new FileInputStream(fileName);
		ObjectInputStream objectIn = new ObjectInputStream(fileIn);
		try {
			list = (ArrayList<?>) objectIn.readObject();
		} catch (ClassNotFoundException e) {
			driver.quit();
			e.printStackTrace();
		}
		objectIn.close();
		fileIn.close();
	} catch (IOException ioe){
		ioe.printStackTrace();
	}	
}
*/	

/*	
public void test(){
	for (int i = 0; i < oldTicketList.size(); i++){
		Ticket ticket = oldTicketList.get(i);
		//make this in need of an update
		if (ticket.ticket.equals("IT-15846")){
			ticket.timestamp = ticket.timestamp - 8*24*60*60*1000L;
			System.out.println("found IT-15846");
		}

		if (ticket.ticket.equals("IT-15126"))
			ticket.timestamp = ticket.timestamp - 8*24*60*60*1000L;
		if (ticket.ticket.equals("IT-5300"))
			ticket.timestamp = ticket.timestamp - 8*24*60*60*1000L;

		//make this recently updated
		else if (ticket.ticket.equals("IT-15126"))
			ticket.latestActivity = "this just got changed";

		//make this recently closed
		else if (ticket.ticket.equals("IT-4348")){
			for (int j = 0; j < openTickets.list.size(); j++){
				if (openTickets.list.get(j).ticket.equals("IT-4348")){
					openTickets.list.remove(j);
					break;
				}
			}
		}
		//make this newly created ticket
		else if (ticket.ticket.equals("IT-6100"))
			oldTicketList.remove(oldTicketList.indexOf(ticket));
		
	}
}
*/	
/*
System.out.println("Recently updated");
for (int i = 0; i < IT.updatedTickets.list.size(); i++){
	System.out.println(IT.updatedTickets.list.get(i).ticket + ", " + IT.updatedTickets.list.get(i).title);
}

System.out.println("\n\nNot Updated");
for (int i = 0; i < IT.notUpdatedTickets.list.size(); i++){
	System.out.println(IT.notUpdatedTickets.list.get(i).ticket + ", " + IT.notUpdatedTickets.list.get(i).title);
}

System.out.println("\n\nNot Udated Metrics");
for (int i = 0; i < notUpdatedMetricsList.size(); i++){
	System.out.println(notUpdatedMetricsList.get(i).ticket + ", " + notUpdatedMetricsList.get(i).title);
}
*/


