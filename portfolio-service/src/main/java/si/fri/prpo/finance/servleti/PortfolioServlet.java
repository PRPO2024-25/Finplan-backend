package si.fri.prpo.finance.servleti;

import si.fri.prpo.finance.entitete.Portfolio;

import si.fri.prpo.finance.zrna.PortfolioBean;

import si.fri.prpo.finance.clients.UserClient;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/servlet/portfolios")
public class PortfolioServlet extends HttpServlet {

    @Inject
    private PortfolioBean portfolioBean;


    @Inject
    private UserClient userClient;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        //get all portfolios
        List<Portfolio> portfolios = portfolioBean.getAllPortfolios();

        writer.append("<html>");
        writer.append("<head>");
        writer.append("<title>Portfolios</title>");
        writer.append("<style>");
        writer.append("table { border-collapse: collapse; width: 100%; }");
        writer.append("th, td { border: 1px solid black; padding: 8px; text-align: left; }");
        writer.append("th { background-color: #f2f2f2; }");
        writer.append(".error { color: red; }");
        writer.append("</style>");
        writer.append("</head>");
        writer.append("<body>");
        writer.append("<h1>Portfolios</h1>");
        writer.append("<table>");
        writer.append("<tr>");
        writer.append("<th>ID</th>");
        writer.append("<th>Name</th>");
        writer.append("<th>Cash Balance</th>");
        writer.append("<th>Total Value</th>");
        writer.append("<th>User ID</th>");
        writer.append("<th>User Status</th>");
        writer.append("</tr>");

        for (Portfolio p : portfolios) {
            writer.append("<tr>");
            writer.append("<td>").append(p.getId().toString()).append("</td>");
            // writer.append("<td>").append(p.getPortfolioName()).append("</td>");
            writer.append("<td>").append(p.getCashBalance().toString()).append("</td>");
            // writer.append("<td>").append(p.getTotalPortfolioValue().toString()).append("</td?>");
            writer.append("<td>").append(p.getUserId().toString()).append("</td>");
            
            // Check user existence
            Response userResponse = userClient.getUserById(p.getUserId());
            writer.append("<td>");
            if (userResponse.getStatus() == 200) {
                writer.append("<span style='color: green;'>Active</span>");
            } else {
                writer.append("<span style='color: red;'>Not Found</span>");
            }
            writer.append("</td>");
            
            writer.append("</tr>");
        }

        writer.append("</table>");
        
        // Add form for creating new portfolio
        writer.append("<h2>Create New Portfolio</h2>");
        writer.append("<form method='post'>");
        writer.append("<p>Name: <input type='text' name='name' required></p>");
        writer.append("<p>Cash Balance: <input type='number' name='cashBalance' required></p>");
        writer.append("<p>User ID: <input type='number' name='userId' required></p>");
        writer.append("<input type='submit' value='Create Portfolio'>");
        writer.append("</form>");
        
        writer.append("</body>");
        writer.append("</html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String name = req.getParameter("name");
        String cashBalanceStr = req.getParameter("cashBalance");
        String userIdStr = req.getParameter("userId");

        if (name != null && userIdStr != null && cashBalanceStr != null) {
            // Check if user exists before creating portfolio
            Response userResponse = userClient.getUserById(Long.parseLong(userIdStr));
            
            if (userResponse.getStatus() != 200) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().println("User not found or service unavailable");
                return;
            }

            Portfolio portfolio = new Portfolio();
            // portfolio.setName(name);
            portfolio.setCashBalance(new BigDecimal(cashBalanceStr));
            portfolio.setUserId(Long.parseLong(userIdStr));

            portfolioBean.createPortfolio(portfolio);
            resp.sendRedirect(req.getContextPath() + "/servlet/portfolios");
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().println("Name, cash balance, and userId are required");
        }
    }
}