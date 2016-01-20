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
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      resp.setContentType("text/html");
      resp.getWriter().println("<html><body>");

      Map<String, String[]> map = req.getParameterMap();
      if (map.size() == 0) {
          Query q = new Query("TaskData");
	  PreparedQuery pq = datastore.prepare(q);
          resp.getWriter().println("<ul>");
	  for (Entity result : pq.asIterable()) {
	      String value = (String) result.getProperty("value");
	      Date date = (Date) result.getProperty("date");
              resp.getWriter().println("<li> " + value + " " + date + " </li>");
          }
          resp.getWriter().println("</ul>");
      } else {
          if (map.containsKey("keyname")) {
              String keyname = map.get("keyname")[0];
	      if (map.containsKey("value")) {
                  String value = map.get("value")[0];
                  Entity taskData = new Entity("TaskData", keyname);
		  taskData.setProperty("value", value);
		  taskData.setProperty("date", new Date());
		  datastore.put(taskData);
                  resp.getWriter().println("<h2>Stored " + keyname + " and " + value + " in Datastore</h2>");
	      } else {
	          Key key = KeyFactory.createKey("TaskData", keyname);
                  try { 
		      Entity taskData = datastore.get(key);
	              String value = (String)taskData.getProperty("value");
		      Date date = (Date)taskData.getProperty("date");
                      resp.getWriter().println("<h2>" + value + " " + date + " </h2>");
	          } catch (EntityNotFoundException e) {
                      resp.getWriter().println("<h2>Entity not found</h2>");
	          }
	      }
	  } else {
              resp.getWriter().println("<h2>Wrong parameters</h2>");
	  }
      }
      resp.getWriter().println("</body></html>");
  }

}
