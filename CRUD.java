
package CRUD;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;



public class CRUD {
    
    private Connection conn = null;
    private Statement st = null;

    
    private DefaultTableModel model = null;
    private ResultSet rs = null;
    
    private String queryBuilder = "";
    
    public CRUD(String jdbc_driver, String db_url, String username, String password)
    {
        try
        {
            Class.forName(jdbc_driver);
            
            //Connect to database
            conn = DriverManager.getConnection(db_url, username, password);
            
        }
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.OK_OPTION);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.OK_OPTION);
        }
    }
    
    public void insert(String tableName, String[] columns, String[] values)
    {
        try
        {
            queryBuilder += "INSERT INTO " + tableName + "(";
        
            for(int i = 0; i < columns.length; i++)
            {
                queryBuilder +=  ( i == columns.length - 1 ) ? columns[i] + ") VALUES (" : columns[i] + ",";
            }

            for(int i = 0; i < values.length; i++)
            {
                queryBuilder +=  ( i == values.length - 1 ) ? "'" + values[i] + "')"  : "'" + values[i] + "',";
            }
            
            st = conn.createStatement();
            st.execute(this.queryBuilder);
            
            //Clean up
            this.commitAndClose();
        }
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.OK_OPTION);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.OK_OPTION);
        }
    }
    
    /*
    NOTE: First value of second parameter is a PK column
          First value of third parameter is a PK value
    */
    public void update(String tableName, String[] columns, String[] values)
    {
        try
        {
            queryBuilder += "UPDATE " + tableName + " set ";
        
            for(int i = 0; i < columns.length; i++)
            {
               queryBuilder += ( i == columns.length - 1 ) ?  columns[i] + " = '" + values[i] + "' WHERE " + columns[0] + " = " + values[0]   : columns[i] + " = '" + values[i] + "', ";
            }
            
            st = conn.createStatement();
            st.execute(this.queryBuilder);
            
            //Clean up
            this.commitAndClose();
            
        }
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.OK_OPTION);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.OK_OPTION);
        }
    }
    
    public void delete(String tableName, String pkColumn ,int id)
    {
        try
        {
            queryBuilder += "DELETE FROM " + tableName + " WHERE " + pkColumn + " = " + id;
            
            st = conn.createStatement();
            st.execute(this.queryBuilder);
            
            //Clean up
            this.commitAndClose();
        }
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.OK_OPTION);
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.OK_OPTION);
        }
    }
    
    public ResultSet retrieveAll(String tableName) throws SQLException
    {
        queryBuilder += "SELECT * FROM " + tableName;
        st = conn.createStatement();
        
        //Execute query
        rs = st.executeQuery(this.queryBuilder);
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
            queryBuilder += ( i == columnNames.length - 1 ) ? columnNames[i] + " FROM " + tableName + " WHERE " + columnNames[0] + " = " + id : columnNames[i] + ", ";
        }
        
        st = conn.createStatement();
        //Execute query
        rs = st.executeQuery(this.queryBuilder);

        return rs;
    }
    
    //For one specific column & row
    public ResultSet retrieve(String tableName, String pkColumn, String columnName , int id) throws SQLException
    {
        queryBuilder += "SELECT " + columnName + " FROM " + tableName + " WHERE " + pkColumn + " = " + id;
        st = conn.createStatement();
        
        //Execute query
        rs = st.executeQuery(this.queryBuilder);
        
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
        
        System.out.println(queryBuilder);
        st = conn.createStatement();
        rs = st.executeQuery(this.queryBuilder);
        
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
    
    public void setColumn(JTable table, String[] columnNames)
    {
        model = (DefaultTableModel)table.getModel();
        
        //clear model
        model.setRowCount(0);
        model.setColumnCount(0);
        model.setColumnIdentifiers(columnNames);
    }
    
    
    /*
        DO NOT FORGET TO COMMIT AND CLOSE AFTER RETRIEVING DATA!
        (insert, update, and delete functions automatically commits and closes)
    */

    public void commitAndClose() 
    {
        if (conn != null) {
            try 
            {
                queryBuilder = "";
                conn.setAutoCommit(false);
                conn.commit();
            } 
            catch (SQLException ex) 
            {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } 
            finally 
            {
                try 
                {
                    rs.close();  
                    st.close();
                    conn.close();
                    model = null;
                } 
                catch (SQLException ex) 
                {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        }
    }
}
