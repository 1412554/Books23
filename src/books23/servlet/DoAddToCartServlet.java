package books23.servlet;
 
import java.io.IOException;
 
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.omg.PortableInterceptor.USER_EXCEPTION;

import books23.beans.Cart;
import books23.beans.Product;
import books23.beans.UserAccount;
import books23.utils.DBUtils;
import books23.utils.MyUtils;
 
@WebServlet(urlPatterns = { "/addToCart"})
public class DoAddToCartServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
 
  public DoAddToCartServlet() {
      super();
  }
 
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
 
	  Connection conn = MyUtils.getStoredConnection(request);
	  
	  HttpSession 	session 		= 	request.getSession();
      UserAccount 	loginedUser 	= 	MyUtils.getLoginedUser(session);
      if (loginedUser != null) {
	      String 		userName 		= 	loginedUser.getUserName();
	      String 		code 			= 	(String) request.getParameter("code");
	      String 		errorString 	= 	null;
	      List<Cart> 	list 			= 	null;
	      int			total			= 	0;
	      UserAccount	user			=	new UserAccount();
	      try {
	    	  Product 	product 		= 	DBUtils.findProduct(conn, code);
	    	  			user 			=	DBUtils.findUser(conn, userName);
	    	  
	    	  String 	bookName 		= 	product.getName();
	    	  String 	bookImageUrl 	= 	product.getImageUrl();
	    	  String	bookQuantityStr	=	(String) request.getParameter("quantity");
	    	  int 		bookPrice		=	product.getPrice();
	    	  
	    	  int		bookQuantity		= 	1;
	    	  
	    	  try {
	    		  bookQuantity 	= 	Integer.parseInt(bookQuantityStr);
	          } catch (Exception e) {
	          }
	    	  
	    	  if (DBUtils.existCart(conn, userName, code)) {
	    		  DBUtils.updateQuantityCart(conn, userName, code, bookQuantity);
	    	  } else {
	    		  Cart 	cart 	= 	new Cart(userName, code, bookName, bookImageUrl, bookQuantity, bookPrice);
	    	  
	    		  DBUtils.addToCart(conn, cart, userName);
	    	  }
	    	  
	          list 	= 	DBUtils.getCart(conn, userName);
	          total = 	DBUtils.getTotal(conn, userName);
	      } catch (SQLException e) {
	          e.printStackTrace();
	          errorString = e.getMessage();
	      }
	      int pointCapable = 0;
	      if (user.getPoint() < 20000) {
	    	  pointCapable = user.getPoint();
	      } else {
	    	  pointCapable = 20000;
	      }
	      // Lưu thông tin vào request attribute trước khi forward sang views.
	      request.setAttribute("errorString", errorString);
	      request.setAttribute("cartList", list);
	      request.setAttribute("total", total);
	      request.setAttribute("pointCapable", pointCapable);
	      
	      // Forward sang /WEB-INF/views/productListView.jsp
	      RequestDispatcher dispatcher = request.getServletContext()
	              .getRequestDispatcher("/WEB-INF/views/cartView.jsp");
	      dispatcher.forward(request, response);
      } else {
    	  RequestDispatcher dispatcher = request.getServletContext()
	              .getRequestDispatcher("/WEB-INF/views/loginView.jsp");
	      dispatcher.forward(request, response);
      }
      
  }
 
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
      doGet(request, response);
  }
 
}