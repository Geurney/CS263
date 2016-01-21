package cs263w16;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;


// The Worker servlet should be mapped to the "/worker" URL.
public class Worker extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
	    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
        syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));

        String keyname = request.getParameter("keyname");
        String value = request.getParameter("value");
		Date date = new Date();
        Entity entity = new Entity("TaskData", keyname);
        entity.setProperty("value", value);
        entity.setProperty("date", date);
	    datastore.put(entity);
        syncCache.put(keyname, new Object[]{(Object)value, (Object)date});
    }
}	
