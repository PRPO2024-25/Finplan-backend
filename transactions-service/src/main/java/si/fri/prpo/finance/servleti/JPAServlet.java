package si.fri.prpo.finance.servleti;

import si.fri.prpo.finance.entitete.Transakcija;
import si.fri.prpo.finance.zrna.TransakcijaZrno;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/servlet/transactions")
public class JPAServlet extends HttpServlet {
    
    @Inject
    private TransakcijaZrno transakcijaZrno;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter writer = resp.getWriter();
        
        // Get filter parameters
        String minAmountStr = req.getParameter("minAmount");
        String maxAmountStr = req.getParameter("maxAmount");
        
        Double minAmount = minAmountStr != null ? Double.parseDouble(minAmountStr) : null;
        Double maxAmount = maxAmountStr != null ? Double.parseDouble(maxAmountStr) : null;
        
        // Get transactions
        List<Transakcija> transactions = transakcijaZrno.getTransakcije(
            minAmount, 
            maxAmount, 
            null,   // startDate 
            null,   // endDate
            null,   // status
            null,   // type
            "createdAt", // sortBy
            "DESC",     // sortOrder
            50         // limit
        );
        
        // Write HTML response
        writer.println("<!DOCTYPE html>");
        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Transactions List</title>");
        writer.println("<style>");
        writer.println("table { border-collapse: collapse; width: 100%; }");
        writer.println("th, td { border: 1px solid black; padding: 8px; text-align: left; }");
        writer.println("th { background-color: #f2f2f2; }");
        writer.println(".filter-form { margin-bottom: 20px; }");
        writer.println("</style>");
        writer.println("</head>");
        writer.println("<body>");
        
        // Add filter form
        writer.println("<div class='filter-form'>");
        writer.println("<h2>Filter Transactions</h2>");
        writer.println("<form method='get'>");
        writer.println("Min Amount: <input type='number' step='0.01' name='minAmount' value='" + 
                (minAmountStr != null ? minAmountStr : "") + "'>");
        writer.println("Max Amount: <input type='number' step='0.01' name='maxAmount' value='" + 
                (maxAmountStr != null ? maxAmountStr : "") + "'>");
        writer.println("<input type='submit' value='Filter'>");
        writer.println("</form>");
        writer.println("</div>");
        
        // Add transactions table
        writer.println("<h2>Transactions</h2>");
        writer.println("<table>");
        writer.println("<tr>");
        writer.println("<th>ID</th>");
        writer.println("<th>Sender ID</th>");
        writer.println("<th>Receiver ID</th>");
        writer.println("<th>Amount</th>");
        writer.println("<th>Status</th>");
        writer.println("<th>Created At</th>");
        writer.println("<th>Description</th>");
        writer.println("</tr>");
        
        for (Transakcija t : transactions) {
            writer.println("<tr>");
            writer.println("<td>" + t.getId() + "</td>");
            writer.println("<td>" + t.getSenderId() + "</td>");
            writer.println("<td>" + t.getReceiverId() + "</td>");
            writer.println("<td>" + String.format("%.2f", t.getAmount()) + "</td>");
            writer.println("<td>" + t.getStatus() + "</td>");
            writer.println("<td>" + t.getCreatedAt() + "</td>");
            writer.println("<td>" + (t.getDescription() != null ? t.getDescription() : "") + "</td>");
            writer.println("</tr>");
        }
        
        writer.println("</table>");
        writer.println("</body>");
        writer.println("</html>");
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
