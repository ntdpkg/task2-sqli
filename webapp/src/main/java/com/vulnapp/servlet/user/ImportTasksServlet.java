package com.vulnapp.servlet.user;

import com.vulnapp.model.User;
import com.vulnapp.utils.JwtUtil;
import com.vulnapp.db.Database;

import java.io.InputStream;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class ImportTasksServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // User user = new User(1, "test", "", "admin");
        User user = JwtUtil.verifyUser(req, resp);

        Part filePart = req.getPart("xmlfile");
        if (filePart == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("No file uploaded");
            return;
        }
        InputStream fileContent = filePart.getInputStream();
        StringBuilder result = new StringBuilder("Import Results:\n");

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(fileContent);
            
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("task");

            String sql = "INSERT INTO tasks (user_id, title, description) VALUES (?, ?, ?)";
            
            for (int i = 0; i < nodeList.getLength(); i++) {
                try (var conn = Database.getConnection(); var pstmt = conn.prepareStatement(sql)) {
                    Element element = (Element) nodeList.item(i);
                    String title = element.getElementsByTagName("title").item(0).getTextContent();
                    String description = element.getElementsByTagName("description").item(0).getTextContent();

                    pstmt.setInt(1, user.getId());
                    pstmt.setString(2, title);
                    pstmt.setString(3, description);
                    pstmt.executeUpdate();

                    result.append("  - Imported task: ").append(title).append("\n");
                } catch (Exception e) {
                    result.append("Error importing task: ").append(e.getMessage()).append("</p>");
                }               
            }
        } catch (Exception e) {
            result.append("Error parsing XML: \n").append(e.getMessage()).append("\n");
        }
        
        resp.setContentType("text/html");
        resp.getWriter().write(result.toString());
    } 
}
