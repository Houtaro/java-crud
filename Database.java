
package Libs;
import java.sql.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class Database {
    
    private static Database instance = null;
    
    private Connection conn = null;
    private PreparedStatement ps = null;
    private DefaultTableModel model = null;
    private ResultSet rs = null;
    private String queryBuilder = "";
    
    //private constructor
    private Database()
    {
        
    }
    
    public static Database getInstance()
    {
        if(instance == null)
        {
            instance = new Database();
        }
        
        return instance;
    }
    
    public void createDatabase(String databaseName) throws SQLException
    {
        queryBuilder = "CREATE Database " + databaseName;
        ps = conn.prepareStatement(this.queryBuilder);
        ps.executeQuery();
    }
    
    public void connect(String jdbc_driver, String db_url, String username, String password) throws SQLException, ClassNotFoundException
    {
        Class.forName(jdbc_driver);
        conn = DriverManager.getConnection(db_url, username, password);
    }
    
    public Statement getStatement()
    {
        return this.ps;
    }
    
    public Connection getConnection()
    {
        return this.conn;
    }
     
    public void insert(String tableName, String[] columns, Object[] values) throws SQLException
    {
        queryBuilder += "INSERT INTO " + tableName + "(";

        for(int i = 0; i < columns.length; i++)
        {
            queryBuilder +=  ( i == columns.length - 1 ) ? columns[i] + ") VALUES (" : columns[i] + ",";
        }

        for(int i = 0; i < values.length; i++)
        {
            queryBuilder +=  ( i == values.length - 1 ) ? "?)"  : "?,";
        }

        ps = conn.prepareStatement(this.queryBuilder);
        for(int i = 0; i < values.length; i++)
        {
            ps.setObject( (i + 1), values[i]);
        }

        ps.executeUpdate();

        this.commitAndClose();
    }
    
    
    /*
    NOTE: First value of second parameter is a PK column
          First value of third parameter is a PK value
    
    */
    
    public void update(String tableName, String[] columns, Object[] values) throws SQLException, Exception
    {
        queryBuilder += "UPDATE " + tableName + " set ";

        for(int i = 1; i < columns.length; i++)
        {
           queryBuilder += ( i == columns.length - 1 ) ?  columns[i] + " = ? WHERE " + columns[0] + " = ?" : columns[i] + " = ?, ";
        }

        ps = conn.prepareStatement(this.queryBuilder);
        ps.setObject(values.length, values[0]);

        for(int i = 0; i < values.length - 1; i++)
        {
            ps.setObject((i + 1), values[i + 1]);
        }

        ps.executeUpdate();

        this.commitAndClose();
    }
    
    public void delete(String tableName, String pkColumn , int id) throws SQLException
    {
            queryBuilder += "DELETE FROM " + tableName + " WHERE " + pkColumn + " = ?";
            ps = conn.prepareStatement(this.queryBuilder);
            
            ps.setInt(1, id);
            ps.executeUpdate();
            
            this.commitAndClose();
    }
    
    
    public ResultSet retrieveAll(String tableName) throws SQLException
    {
        queryBuilder += "SELECT * FROM " + tableName;
        ps = conn.prepareStatement(this.queryBuilder);
        rs = ps.executeQuery();
        return rs;
    }
    
    
    /*
    NOTE: First value of second parameter is a PK column
    */
    public ResultSet retrieve(String tableName, String[] columnNames, int id) throws SQLException
    {
        queryBuilder += "SELECT ";

        for(int i = 1; i < columnNames.length; i++)
        {
            queryBuilder += ( i == columnNames.length - 1 ) ? columnNames[i] + " FROM " + tableName + " WHERE " + columnNames[0] + " = ?" :  columnNames[i] + ", ";
        }
        
        ps = conn.prepareStatement(this.queryBuilder);
        
        ps.setInt(1, id);
        rs = ps.executeQuery();
        return rs;
    }
    
    
    //For one specific column & row
    public ResultSet retrieve(String tableName, String pkColumn, String columnName , int id) throws SQLException
    {
        queryBuilder += "SELECT " + columnName + " FROM " + tableName + " WHERE " + pkColumn + " = ?";
        ps = conn.prepareStatement(this.queryBuilder);
        ps.setInt(1, id);
         
        rs = ps.executeQuery();
        
        return rs;
    }
    
    //for multiple columns
    public ResultSet retrieve(String tableName, String[] columnNames) throws SQLException
    {
        
        queryBuilder += "SELECT ";
        
        for(int i = 0; i < columnNames.length; i++)
        {
            queryBuilder += ( i == columnNames.length - 1 ) ? columnNames[i] + " FROM " + tableName : columnNames[i] + ", ";
        }
        
        ps = conn.prepareStatement(this.queryBuilder);
        rs = ps.executeQuery();
        return rs;
    }
    
    //binds the result set into the jtable
    public void bind(JTable tableName, ResultSet rs) throws SQLException
    {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        while(rs.next())
        {
            Object[] objects = new Object[columnCount];
            
            for(int i = 0; i < columnCount; i++)
            {
                objects[i] = rs.getObject(i + 1);
            }
            
            model.addRow(objects);
        }
        
        tableName.setModel(model);
    }
    
    //binds resultset into an html table - NOT FINISHED YET
    /*public void bind(ResultSet rs) throws SQLException
    {
        String output = "";
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        while(rs.next())
        {
            output += "<tr>";
            for(int i = 0; i < columnCount; i++)
            {
                output += "<td" + rs.getObject(i + 1) + "</td>";
            }
            output += "</tr>";
        }
     
    }*/
    
    public void setColumn(JTable table, String[] columnNames)
    {
        model = (DefaultTableModel)table.getModel();
        
        model.setRowCount(0);
        model.setColumnCount(0);
        model.setColumnIdentifiers(columnNames);
    }
    
    
    //COMMIT and CLOSE after retrieving data
    public void commitAndClose() throws SQLException
    {
        if (conn != null)
        {
            queryBuilder = "";
            conn.setAutoCommit(false);
            conn.commit();
            rs.close();  
            ps.close();
            conn.close();
            model = null;
        }
    }
}