import co3
import requests

requests.packages.urllib3.disable_warnings()

def show_incident_list(client):
    incidents = None
    incidents = client.get('/incidents/open')

       # Print the incident names
    for inc in incidents:
        print("{0}:{1}".format(inc['id'], inc['properties']['external_ticket']))
 
if __name__ == "__main__":
    client = co3.SimpleClient(org_name=name, proxies=None, base_url=url, verify=False)
    client.connect(username, password)
    show_incident_list(client)
