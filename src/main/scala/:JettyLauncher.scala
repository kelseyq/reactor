/*
 * Starts jetty for scalatra programatically
 *
 * Replace YourApplicationEndpointFilter with the filter in your application
 */
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import com.example.app.MyScalatraServlet

object JettyLauncher {
  def main(args: Array[String]) {
    val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 8080

    val server = new Server(port)
    val context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS)

    context.addServlet(classOf[MyScalatraServlet], "/*")
    context.addServlet(classOf[DefaultServlet], "/");
    context.setResourceBase("src/main/webapp")

    server.start
    server.join
  }

}