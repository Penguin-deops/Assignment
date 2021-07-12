<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
        <title>Cake Manager Page</title>
    </head>
    <body>        
        <h1>Available List of Cakes:</h1>
        <hr>
            <!-- Display info or error messages -->
            <% 
                String info = (String) request.getAttribute("info");
                if ( info == null ) {
                    info = "";
                }
                String error = (String) request.getAttribute("error");
                if ( error == null ) {
                    error = "";
                }
              %>
        <h3>
            <font color="red"><%=error%></font>
        </h3>
        <h3>
            <font color="blue"><%=info%></font>
        </h3>
        <table border="2">
            <tr>
            <th>ID</th>
            <th>Title</th>
            <th>Description</th>
            <th>Image</th>
            </tr>   
        <%
            java.util.ArrayList<com.waracle.cakemgr.CakeEntity> list = (java.util.ArrayList<com.waracle.cakemgr.CakeEntity>) request.getAttribute("list");
            for (com.waracle.cakemgr.CakeEntity object : list) {
        %>
            <tr>
                <td><%=object.getId()%></td>
                <td><%=object.getTitle()%></td>
                <td><%=object.getDescription() %></td>
                <td> <img src="<%=object.getImage() %>" width="100"></td>                
            </tr>            
            <%}%>
        </table>
            
        <h1>Add new Cake</h1>
            <hr>
            <form action = "cakes" method = "post"> 
            
            <table>
                <tr>
                    <td>Title:</td><td><input type = "text" name = "title"></td>
                </tr>
            <tr>
                <td>Description:</td><td><input type = "text" name = "description" ></td>
            </tr>
            <tr>
                <td>Image:</td><td><input type = "text" name = "image"></td>
            </tr>
            <tr>
                <td></td>
                <td colspan="2">
                        <input type = "submit" value = "Submit" />
                </td>
            </tr>
            </table>
      </form>                    

        </body>
</html>
