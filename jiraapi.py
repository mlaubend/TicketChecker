#!/usr/bin/env python

'''
To extend and add another employee's ticket queue, add USERNAME to SOC_employees list
'''

from jira import JIRA
from datetime import datetime
import pickle
import requests

requests.packages.urllib3.disable_warnings()

class JiraTicket:
	def __init__(self):
		#this list is where you extend, don't touch anything else
		self.employees = [
				employee1,
				employee2
				#extend here
				]
		self.new_tickets = {}
		self.old_tickets = {}
		self.closed_tickets = {}
		self.updated_tickets = {}
		self.needs_update = {}
		jira_options = { 'server': server, 'verify':False}
		self.jira = JIRA(options=jira_options, basic_auth=('username', 'password'))

	def get_unix_timestamp(self, issue):
		split = issue.fields.updated.split('T')
		split1 = split[1].split('.')

		created = split[0] + " " + split1[0]
		return (datetime.strptime(created, '%Y-%m-%d %H:%M:%S')).strftime('%s')
		#unix_t = timestamp.strftime('%s')

		return int(unix_t)

	def get_new_tickets(self, username):
		JQL = "reporter=" + username + " and status in ('In Progress', open, reopened)" + " order by created desc"
		JQL_query = self.jira.search_issues(JQL, maxResults=50)

		for item in JQL_query:
			self.new_tickets[str(item)] = {} #TODO: don't need this
			issue = self.jira.issue(str(item))
			comments = self.jira.comments(str(item))

			self.new_tickets[str(item)] = {	
									'commentCount': len(issue.fields.comment.comments), 
									'timestamp': int(datetime.now().strftime('%s')),
									'latestCommentAuthor': None
									}
			if self.new_tickets[str(item)]['commentCount'] > 0:
				self.new_tickets[str(item)]['latestCommentAuthor'] = comments[-1].raw['updateAuthor']['displayName']
			else:
				self.new_tickets[str(item)]['latestCommentAuthor'] = str(issue.fields.reporter)

	def write_dict_to_file(self, dict):
		pickle.dump(dict, open("old_jira_tickets.p", "wb"))
		pickle.dump(dict, open("backup_old_jira_tickets.p", "wb"))

	def load_dict_from_file(self):
		try:
			self.old_tickets = pickle.load(open("old_jira_tickets.p", "rb"))
		except:
			self.old_tickets = pickle.load(open("backup_old_jira_tickets.p", "rb"))

	def compare_old_to_new(self):
		to_delete_from_old = []
		to_delete_from_new = []

		for key in self.old_tickets:
			old_issue = self.old_tickets[key]
			
			#check for updated tickets
			if key in self.new_tickets.keys():
				if old_issue['commentCount'] == self.new_tickets[key]['commentCount']:
					if (int(datetime.now().strftime('%s')) - int(old_issue['timestamp'])) > 7*24*60*60:
						self.needs_update[key] = {}		
						old_issue['timestamp'] = self.new_tickets[key]['timestamp']				
						if old_issue['commentCount'] > 0:
								self.needs_update[key] = old_issue['latestCommentAuthor']

				#else it's been updated										
				else:
					self.updated_tickets[key] = {}
					if old_issue['commentCount'] > 0:
						self.updated_tickets[key] = old_issue['latestCommentAuthor']
					old_issue['commentCount'] = self.new_tickets[key]['commentCount']
					old_issue['timestamp'] = self.new_tickets[key]['timestamp']
				to_delete_from_new.append(key)

			#else it's closed
			else:
				self.closed_tickets[key] ={}
				self.closed_tickets[key] = old_issue['latestCommentAuthor']
				to_delete_from_old.append(key)

		for key in to_delete_from_old:
			del(self.old_tickets[key])
		for key in to_delete_from_new:
			del(self.new_tickets[key])

		#at this point the only tickets left in new_tickets are newly created -> add them to 
		#old_tickets and recently updated
		for key in self.new_tickets.keys():
			ticket = self.new_tickets[key]

			self.old_tickets[key] = {}
			self.old_tickets[key]['commentCount'] = ticket['commentCount']
			self.old_tickets[key]['timestamp'] = ticket['timestamp']
			self.old_tickets[key]['latestCommentAuthor'] = ticket['latestCommentAuthor']

			self.updated_tickets[key] = {}
			self.updated_tickets[key] = ticket['latestCommentAuthor']

	def test(self):
		jt.old_tickets['ticket']['commentCount'] = 5 #create updated ticket
		jt.old_tickets['ticket']['timestamp'] -= 8*24*60*60 #create needs update ticket
		del(jt.new_tickets['ticket']) #create closed ticket

	def print_all_lists(self):
		print "new tickets"
		for key in self.new_tickets.keys():
			print key
		print "\nold tickets"
		for key in self.old_tickets.keys():
			print key
		print "\nupdated tickets"
		for key in self.updated_tickets.keys():
			print key
		print "\nclosed tickets"
		for key in self.closed_tickets.keys():
			print key
#		print "\nneeds update tickets"
#		for key in self.needs_update.keys():
#			print key
		print "\n"

def main():
	jt = JiraTicket()

	jt.load_dict_from_file()

	for employee in jt.employees:
		jt.get_new_tickets(employee)

#	jt.test()
	jt.compare_old_to_new()
#	jt.write_dict_to_file(jt.old_tickets)

	for key in jt.updated_tickets:
		print str(key) + " | " + str(jt.updated_tickets[key])
	print "<--->"

	for key in jt.closed_tickets:
		print str(key) + " | " + str(jt.closed_tickets[key])
	print "<--->"

#	for key in jt.needs_update:
#		print str(key) + " | " + str(jt.needs_update[key])


if __name__=='__main__':
	main()
	