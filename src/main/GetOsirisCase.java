package main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

/***
 * 
 * @author mlaubender
 *
 * fills a HashMap with ticket:case data using the osiris API.  Data is used to create osiris URL links. 
 */

public class GetOsirisCase implements Runnable{
	public HashMap<String, String> map = new HashMap<String, String>();

	public GetOsirisCase(){
		
	}
	
	/**
	 * loads osiris cases into a Ticket:Case hashmap.
	 */
	public void getOsirisCases(){
		String line = null;
		
		try{
			Process process = Runtime.getRuntime().exec("python osiris_external_ticket.py");
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
			
			while ((line = stdIn.readLine()) != null){
				String split[] = line.split(":");
				
				if (split.length > 1){ //greater than 1 means there is an external ticket
					String split1[] = split[1].split(",");
					for (String string : split1)
						map.put(string.trim(), split[0]);
				}
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public void run() {
		getOsirisCases();
	}
}
