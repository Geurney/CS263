package cs263w16;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;
import java.util.logging.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;
import javax.xml.bind.JAXBElement;

public class TaskDataResource {
  @Context
  UriInfo uriInfo;
  @Context
  Request request;
  String keyname;

  public TaskDataResource(UriInfo uriInfo, Request request, String kname) {
    this.uriInfo = uriInfo;
    this.request = request;
    this.keyname = kname;
  }
  
  // for the browser
  @GET
  @Produces(MediaType.TEXT_XML)
  public TaskData getTaskDataHTML() {
	TaskData taskData = null;
	MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	Object[] values = (Object[])syncCache.get(keyname);
	
	if (values == null) {
	  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	  Key key = KeyFactory.createKey("TaskData", keyname);
      try { 
        Entity entity = datastore.get(key);
	    String value = (String)entity.getProperty("value");
		Date date = (Date)entity.getProperty("date");
	    syncCache.put(keyname, new Object[]{(Object)value, (Object)date});
		taskData = new TaskData(entity.getKey().getName(), (String)entity.getProperty("value"), (Date)entity.getProperty("date"));
	  } catch (EntityNotFoundException e) {
        throw new RuntimeException("Get: TaskData with " + keyname +  " not found");
	  }
	} else {
		taskData = new TaskData(keyname, (String)values[0], (Date)values[1]);
	}
    return taskData;
  }
  
  // for the application
  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public TaskData getTaskData() {
	TaskData taskData = null;
	MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	Object[] values = (Object[])syncCache.get(keyname);
	
	if (values == null) {
	  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	  Key key = KeyFactory.createKey("TaskData", keyname);
      try { 
        Entity entity = datastore.get(key);
	    String value = (String)entity.getProperty("value");
		Date date = (Date)entity.getProperty("date");
	    syncCache.put(keyname, new Object[]{(Object)value, (Object)date});
		taskData = new TaskData(entity.getKey().getName(), (String)entity.getProperty("value"), (Date)entity.getProperty("date"));
	  } catch (EntityNotFoundException e) {
        throw new RuntimeException("Get: TaskData with " + keyname +  " not found");
	  }
	} else {
		taskData = new TaskData(keyname, (String)values[0], (Date)values[1]);
	}
    return taskData;
  }

  @PUT
  @Consumes(MediaType.APPLICATION_XML)
  public Response putTaskData(String val) {
    Response res = null;
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	
    try { 
	    Key key = KeyFactory.createKey("TaskData", this.keyname);
	    Entity entity = datastore.get(key);
		Object[] values = (Object[])syncCache.get(keyname);
		entity.setProperty("value", val);
		values[0] = (Object)val;
		datastore.put(entity);
		syncCache.put(keyname, values);
		res = Response.noContent().build();
   	} catch (EntityNotFoundException e) {
		Entity entity = new Entity("TaskData", this.keyname);
		Date date = new Date();
		entity.setProperty("value", val);
		entity.setProperty("date", date);
		datastore.put(entity);
		syncCache.put(keyname, new Object[]{(Object)val, (Object)date});
		res = Response.created(uriInfo.getAbsolutePath()).build();
	}
    return res;
  }

  @DELETE
  public void deleteIt() {
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	try {
		Key key = KeyFactory.createKey("TaskData", this.keyname);
	    datastore.delete(key);
		syncCache.delete(keyname);
	} catch (Exception e) {
		System.out.println("Delete Failure!");
	}
  }

} 
