package scala

import java.sql.{ Connection, DriverManager, Statement, ResultSet }
import scala.concurrent.{Future}
import scala.concurrent.ExecutionContext.Implicits.global

object DBConnection {
  def getConnection: Connection = {

    var connection:Connection = null
    try {
      val url = "jdbc:mysql://localhost:3306/blacklinesafetySKU"
      val driver = "com.mysql.jdbc.Driver"
      val username = "root"
      val password = "root"

      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password);
    }
    catch {
      case e: Exception => e.printStackTrace
    }
    connection
  }

  def executeQuery(connection: Connection, sql: String): Future[ResultSet] = Future {

    var dbResponse: ResultSet = null
    try {
      val statement:Statement = connection.createStatement
      dbResponse = statement.executeQuery(sql);

      println("In get query")
    }
    catch {
      case e: Exception => e.printStackTrace
    }
    dbResponse
  }

}