package cs263w16;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.logging.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;

//Map this class to /ds route
@Path("/ds")
public class DatastoreResource {
  // Allows to insert contextual objects into the class,
  // e.g. ServletContext, Request, Response, UriInfo
  @Context
  UriInfo uriInfo;
  @Context
  Request request;

  // Return the list of entities to the user in the browser
  @GET
  @Produces(MediaType.TEXT_XML)
  public List<TaskData> getEntitiesBrowser() {
    //datastore dump -- only do this if there are a small # of entities
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	
	ArrayList<String> keynames = new ArrayList<String>();
	
    List<TaskData> list = new ArrayList<TaskData>();
	Query q = new Query("TaskData");
	PreparedQuery pq = datastore.prepare(q);
	for (Entity result : pq.asIterable()) {
		keynames.add(result.getKey().getName());
	    TaskData data = new TaskData(result.getKey().getName(), (String)result.getProperty("value"), (Date)result.getProperty("date"));
		list.add(data);
	}
	
	for (String key_name : keynames) {
        Object[] values = (Object[])syncCache.get(key_name);
        if (values != null) {
          System.out.println(key_name + " is in memcache");
	    }
	}
    return list;
  }

  // Return the list of entities to applications
  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public List<TaskData> getEntities() {
    //datastore dump -- only do this if there are a small # of entities
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	
	ArrayList<String> keynames = new ArrayList<String>();
	
    List<TaskData> list = new ArrayList<TaskData>();
	Query q = new Query("TaskData");
	PreparedQuery pq = datastore.prepare(q);
	for (Entity result : pq.asIterable()) {
		keynames.add(result.getKey().getName());
		TaskData data = new TaskData(result.getKey().getName(), (String)result.getProperty("value"), (Date)result.getProperty("date"));
		list.add(data);
	}
    for (String key_name : keynames) {
        Object[] values = (Object[])syncCache.get(key_name);
        if (values != null) {
          System.out.println(key_name + " is in memcache");
	    }
	}
    return list;
  }

  //Add a new entity to the datastore
  @POST
  @Produces(MediaType.TEXT_HTML)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public void newTaskData(@FormParam("keyname") String keyname,
      @FormParam("value") String value,
      @Context HttpServletResponse servletResponse) throws IOException {
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	
    Entity taskData = new Entity("TaskData", keyname);
	Date date = new Date();
	taskData.setProperty("value", value);
	taskData.setProperty("date", date);
    datastore.put(taskData);
	syncCache.put(keyname, new Object[]{(Object)value,(Object)date});
	System.out.println("Posting new TaskData: " +keyname+" val: "+value+" ts: "+date);
    servletResponse.sendRedirect("../done.html");
  }

  //The @PathParam annotation says that keyname can be inserted as parameter after this class's route /ds
  @Path("{keyname}")
  public TaskDataResource getEntity(@PathParam("keyname") String keyname) {
    System.out.println("GETting TaskData for " +keyname);
    return new TaskDataResource(uriInfo, request, keyname);
  }
}
