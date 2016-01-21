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

    resp.setContentType("text/html");
    resp.getWriter().println("<html><body>");

    Map<String, String[]> map = req.getParameterMap();
	// No parameters
    if (map.size() == 0) {
      Query q = new Query("TaskData");
	  PreparedQuery pq = datastore.prepare(q);
	  ArrayList<String> keynames = new ArrayList<String>();

      resp.getWriter().println("<ul>In DataStore");
	  for (Entity result : pq.asIterable()) {
	    Key key  = result.getKey();
	    keynames.add(key.getName());
	    String value = (String) result.getProperty("value");
	    Date date = (Date) result.getProperty("date");
        resp.getWriter().println("<li>" + key + "  " + value + " " + date + " </li>");
      }
      resp.getWriter().println("</ul>");

      resp.getWriter().println("<ul>In MemCache");
	  for (String key_name : keynames) {
        Object[] values = (Object[])syncCache.get(key_name);
        if (values != null) {
          resp.getWriter().println("<li>" + key_name + " " + (String)values[0] + " " + (Date)values[1] + " </li>");
	    }
	  }
      resp.getWriter().println("</ul>");
    } else {
      if (map.containsKey("keyname")) {
        String keyname = map.get("keyname")[0];
		// keyname & value
	    if (map.containsKey("value")) {
          String value = map.get("value")[0];
		  Date date = new Date();
          Entity entity = new Entity("TaskData", keyname);
		  entity.setProperty("value", value);
		  entity.setProperty("date", date);
		  datastore.put(entity);
	      syncCache.put(keyname, new Object[]{(Object)value,(Object)date});
          resp.getWriter().println("<h2>Stored " + keyname + " and " + value + " in Datastore & memcache</h2>");
	    } else {
		  // keyname
          Object[] values = (Object[])syncCache.get(keyname);
          if (values == null) {
	        Key key = KeyFactory.createKey("TaskData", keyname);
            try { 
			  Entity entity = datastore.get(key);
	          String value = (String)entity.getProperty("value");
			  Date date = (Date)entity.getProperty("date");
	          syncCache.put(keyname, new Object[]{(Object)value, (Object)date});
			  resp.getWriter().println("<h2>" + keyname + ": " + value + " " + date + " (Datastore)</h2>");
			} catch (EntityNotFoundException e) {
              resp.getWriter().println("<h2>Neither found</h2>");
	        }
		  } else {
            resp.getWriter().println("<h2>" + keyname + ": " + (String)values[0] + " " + (Date)values[1] + " (Both)</h2>");
		  }
	    }
	  } else {
        resp.getWriter().println("<h2>Wrong parameters</h2>");
	  }
    }
    resp.getWriter().println("</body></html>");
  }

}
