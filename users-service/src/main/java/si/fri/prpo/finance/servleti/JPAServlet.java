package si.fri.prpo.finance.servleti;

import si.fri.prpo.finance.entitete.User;
import si.fri.prpo.finance.zrna.UserService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/servlet")
public class JPAServlet extends HttpServlet {

    @Inject
    private UserService userService;

    private static final Logger logger = Logger.getLogger(JPAServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        try {
            List<User> users = userService.getUsers();
            
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<title>Users Database Content</title>");
            writer.println("<style>");
            writer.println("table { border-collapse: collapse; width: 100%; }");
            writer.println("th, td { border: 1px solid black; padding: 8px; text-align: left; }");
            writer.println("th { background-color: #f2f2f2; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            
            writer.println("<h1>Users in Database</h1>");
            
            if (users.isEmpty()) {
                writer.println("<p>No users found in database.</p>");
            } else {
                writer.println("<table>");
                writer.println("<tr>");
                writer.println("<th>ID</th>");
                writer.println("<th>Username</th>");
                writer.println("<th>Name</th>");
                writer.println("<th>Email</th>");

                writer.println("</tr>");
                
                for (User user : users) {
                    writer.printf("<tr>");
                    writer.printf("<td>%d</td>", user.getId());
                    writer.printf("<td>%s</td>", user.getUsername());
                    writer.printf("<td>%s %s</td>", user.getFirstName(), user.getLastName());
                    writer.printf("<td>%s</td>", user.getEmail());
                    // writer.printf("<td>$%.2f</td>", user.getBalance());
                    // writer.printf("<td>%s</td>", user.isActive() ? "Active" : "Inactive");
                    writer.println("</tr>");
                }
                writer.println("</table>");
            }
            
            writer.println("</body></html>");

        } catch (Exception e) {
            logger.severe("Error retrieving users: " + e.getMessage());
            writer.println("<html><body>");
            writer.println("<h1>Error</h1>");
            writer.println("<p>Could not retrieve user data: " + e.getMessage() + "</p>");
            writer.println("</body></html>");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
