package com.waracle.cakemgr;



//todo: should be refactored to only import used classes
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.waracle.cakemgr.exception.cakemgrException;

@WebServlet("/cakes")
public class CakeServlet extends HttpServlet {

    //todo: move to Constant/interfasce class
    private final String JSON_CONTENT_TYPE = "application/json";//JSON Content Type
    private final String MULTIPART_CONTENT_TYPE = "multipart/form-data";//form Content Type

    @Override
    public void init() throws ServletException {
        super.init();

        System.out.println("init started");

        System.out.println("downloading cake json");
        //todo: refactor to use json to object mapping api to avoid longer lines of code.
        try (InputStream inputStream = new URL("https://gist.githubusercontent.com/hart88/198f29ec5114a3ec3460/raw/8dd19a88f9b8d24c23d9960f3300d0c917a4f07c/cake.json").openStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer buffer = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line);
                line = reader.readLine();
            }

            System.out.println("parsing cake json");
            JsonParser parser = new JsonFactory().createParser(buffer.toString());
            if (JsonToken.START_ARRAY != parser.nextToken()) {
                throw new Exception("bad token");
            }

            JsonToken nextToken = parser.nextToken();
            while (nextToken == JsonToken.START_OBJECT) {
                System.out.println("creating cake entity");

                CakeEntity cakeEntity = new CakeEntity();
                System.out.println(parser.nextFieldName());
                cakeEntity.setTitle(parser.nextTextValue());

                System.out.println(parser.nextFieldName());
                cakeEntity.setDescription(parser.nextTextValue());

                System.out.println(parser.nextFieldName());
                cakeEntity.setImage(parser.nextTextValue());
                add(cakeEntity);//call the add method

                nextToken = parser.nextToken();
                System.out.println(nextToken);

                nextToken = parser.nextToken();
                System.out.println(nextToken);
            }

        } catch (Exception ex) {
            throw new ServletException(ex);
        }

        System.out.println("init finished");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);//calls the getPost to service both methods
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("*** Content Type: " + req.getContentType());
        
        try {
            //detect if this is a browser request
            if (isBrowser(req)) { //browser
                processBrowserRequest(req, resp);

            } else if (isJson(req)) {//json request
                processJsonRequest(req, resp);

            } else if (isMultipartContent(req)) {//form
                processFormRequest(req, resp);

            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported request with content-type set to: " + req.getContentType());
            }
        } catch (cakemgrException ex) {
            req.setAttribute("error", ex.getMessage());
            req.getRequestDispatcher("display.jsp").forward(req, resp);
        }
    }

    /** 
     * Processes Json request for the given req and resp objects
     * @param req
     * @param response 
     */
    private void processJsonRequest(HttpServletRequest req, HttpServletResponse response) {
        System.out.println("processJsonRequest ...");
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter out = null;
        try {
            response.setContentType(JSON_CONTENT_TYPE);//sets the content type
            response.setCharacterEncoding("utf-8");//set the encoding
            out = response.getWriter();
            mapper.writeValue(out, getList());//using mapper to convert the object to json representation.

        } catch (IOException ex) { // catch the io ex
            try {
                mapper.writeValue(out, new cakemgrException(ex.getMessage()));
            } catch (IOException ex1) {

            }
            ex.printStackTrace();
        }
    }

    /**
     * Returns true if the given request is a Multipart content, and false otherwise
     * content.
     * @param request is the servlet request to be evaluated. Must be non-null.
     * @return <code>true</code> if the request is multipart; <code>false</code>
     * otherwise.
     */
    private final boolean isMultipartContent(HttpServletRequest request) {
        if (!"post".equals(request.getMethod().toLowerCase())) {
            return false;
        }

        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        //default form
        if (contentType.toLowerCase().startsWith("application/x-www-form-urlencoded")) {
            return true;
        }
        if (contentType.toLowerCase().startsWith(MULTIPART_CONTENT_TYPE)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given request content type is null and false otherwise
     * todo: further validation are required to assert the correct browser content type.
     * @param request The servlet request to be evaluated. Must be null.
     * @return <code>true</code> if the request is null; <code>false</code>
     * otherwise.
     */
    private boolean isBrowser(HttpServletRequest req) {
        return (req.getContentType() == null);//todo: check for possible content types
    }

    /**
     * Returns true if the given request content type is JSON and false otherwise,
     * todo: further validation are required to verify the correct client type.
     * @param req
     * @return boolean
     */
    private boolean isJson(HttpServletRequest req) {
        return JSON_CONTENT_TYPE.equalsIgnoreCase(req.getContentType());
    }

    /** Returns the list of object from the DB */
    private List getList() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        List<CakeEntity> list = session.createCriteria(CakeEntity.class).list();
        HibernateUtil.close(session);
        return list;
    }

    /** Processes the form request */
    private void processFormRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.print("\nprocessFormRequest ...");
        try {
            String title = req.getParameter("title");
            String desc = req.getParameter("description");
            String image = req.getParameter("image");
            System.out.print("\n****Title=" + title + ", desc=" + desc + ", image=" + image);
            //basic validate
            if (title.isEmpty() || desc.isEmpty()) {
                req.setAttribute("error", "Form Validation Error, invalid values)");
            } else {
                CakeEntity object = new CakeEntity();
                object.setTitle(title);
                object.setDescription(desc);
                object.setImage(image);
                //add object
                add(object);
            }
            
            processBrowserRequest(req, resp);
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new cakemgrException("Form Request Error: " + ex.getMessage());
        }
    }

    /**
     * adds the given object to the db
     */
    private void add(CakeEntity object) {
        System.out.println("*** add ... " + object.toString());
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            session.persist(object);
            System.out.println("adding cake entity: " + object);
            session.getTransaction().commit();

        } catch (ConstraintViolationException ex) {
            System.err.println("\n***ConstraintViolationException: " + ex.getMessage() + ",\n Possible duplicated Object: " + object);
            //ex.printStackTrace();
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new cakemgrException("Add Error: " + ex.getMessage());
        } finally {
            HibernateUtil.close(session);
        }
    }

    /** Processes the Browser request by returning the list page */
    private void processBrowserRequest(HttpServletRequest req, HttpServletResponse resp) {
        System.out.println("processBrowserRequest ...");
        try {
            List list = getList();
            req.setAttribute("list", list);
            req.setAttribute("info", list.size() + " item(s)");
            req.getRequestDispatcher("display.jsp").forward(req, resp);//todo:this should be refactored to seperate 
            
        } catch (Throwable ex) {
            ex.printStackTrace();
            throw new cakemgrException("Browser Request Error: " + ex.getMessage());
        }
    }

    @Override
    public void destroy() {
        super.destroy(); //To change body of generated methods, choose Tools | Templates.
        try {
            HibernateUtil.shutdown();//to shutdown the database to avoid memory leaks
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
