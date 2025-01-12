package si.fri.prpo.finance.servleti;

import si.fri.prpo.finance.zrna.YahooFinanceService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet("/servlet/test-stock")
public class TestStockServlet extends HttpServlet {

    @Inject
    private YahooFinanceService yahooFinanceService;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String symbol = req.getParameter("symbol");
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter writer = resp.getWriter();

        if (symbol == null || symbol.isEmpty()) {
            writer.append("<html><body><h1>Error: No stock symbol provided</h1></body></html>");
            return;
        }

        try {
            Map<String, Object> stockData = yahooFinanceService.getStockPriceAndStats(symbol);
            writer.append("<html><body>");
            writer.append("<h1>Stock Data for: ").append(symbol).append("</h1>");
            writer.append("<p>Price: ").append(stockData.get("price").toString()).append("</p>");
            writer.append("<p>Currency: ").append(stockData.get("currency").toString()).append("</p>");
            writer.append("<p>Exchange: ").append(stockData.get("exchange").toString()).append("</p>");
            writer.append("<p>Last Updated: ").append(stockData.get("lastUpdated").toString()).append("</p>");
            writer.append("</body></html>");
        } catch (Exception e) {
            writer.append("<html><body><h1>Error fetching stock data: ").append(e.getMessage()).append("</h1></body></html>");
        }
    }
}