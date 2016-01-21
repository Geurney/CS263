package cs263w16;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;

@SuppressWarnings("serial")
public class DatastoreServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
      syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));   

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

      String keyname = null;
      String value = null;
      Date date = null;

      resp.setContentType("text/html");
      resp.getWriter().println("<html><body>");

      Map<String, String[]> map = req.getParameterMap();
      if (map.size() == 0) {
          Query q = new Query("TaskData");
	  PreparedQuery pq = datastore.prepare(q);
	  ArrayList<String> keynames = new ArrayList<String>();

          resp.getWriter().println("<ul>In DataStore");
	  for (Entity result : pq.asIterable()) {
	      Key key  = result.getKey();
	      keynames.add(key.getName());
	      value = (String) result.getProperty("value");
	      date = (Date) result.getProperty("date");
              resp.getWriter().println("<li>" + key + "  " + value + " " + date + " </li>");
          }
          resp.getWriter().println("</ul>");

          resp.getWriter().println("<ul>In MemCache");
	  for (String key_name : keynames) {
              value = (String)syncCache.get(key_name);
              if (value != null) {
                  resp.getWriter().println("<li>" + key_name + " " + value + " </li>");
	      }
	  }
          resp.getWriter().println("</ul>");
      } else {
          if (map.containsKey("keyname")) {
              keyname = map.get("keyname")[0];
	      if (map.containsKey("value")) {
                  value = map.get("value")[0];
                  Entity taskData = new Entity("TaskData", keyname);
		  taskData.setProperty("value", value);
		  taskData.setProperty("date", new Date());
		  datastore.put(taskData);
	          syncCache.put(keyname, value);
                  resp.getWriter().println("<h2>Stored " + keyname + " and " + value + " in Datastore</h2>");
	      } else {
                  value = (String) syncCache.get(keyname);
                  if (value == null) {
	              Key key = KeyFactory.createKey("TaskData", keyname);
                      try { 
		          Entity taskData = datastore.get(key);
	                  value = (String)taskData.getProperty("value");
	                  syncCache.put(keyname, value);
			  resp.getWriter().println("<h2>" + keyname + ": " + value + " (Datastore)</h2>");
   	              } catch (EntityNotFoundException e) {
                          resp.getWriter().println("<h2>Neither found</h2>");
	              }
		  } else {
                      resp.getWriter().println("<h2>" + keyname + ": " + value + " (Both)</h2>");
		  }
	      }
	  } else {
              resp.getWriter().println("<h2>Wrong parameters</h2>");
	  }
      }
      resp.getWriter().println("</body></html>");
  }

}
