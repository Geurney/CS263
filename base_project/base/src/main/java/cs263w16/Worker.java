package cs263w16;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

// The Worker servlet should be mapped to the "/worker" URL.
public class Worker extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
        String key = request.getParameter("keyname");
		        // Do something with key.
    }
}	