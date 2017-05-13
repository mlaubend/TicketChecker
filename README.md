#Jira Ticket Checker

* Java v.1.8.0_65

##Synopsis

Program that checks all shared team tickets and all individual employee Jira tickets and sorts them into "recently updated", "recently closed", and "no activity in 7 days" categories. 

The IT Ticket portal had its API turned off, so a Selenium WebDriver (PhantomJS) is used to visit the site and scrape all the relevant data. Jira tickets are checked through regular API calls. 

Once the tickets are sorted into categories, an "old ticket" list is saved to file.  This old ticket list is then compared against the newly created ticket list the next time the program is run in order to sort.  This new ticket list is then saved to file as the next "old ticket" list:

- !Old list & new list -> new ticket
- old list & !new list -> closed ticket
- old list & (new list with updated timestamp)  -> recently updated
- old list & (new list with same timestamp > 7\*24\*60\*60) -> not updated

A separate list saved to file keeps track of the "no activity in 7 days" tickets and sends an email to leadership every monday containing these tickets. 