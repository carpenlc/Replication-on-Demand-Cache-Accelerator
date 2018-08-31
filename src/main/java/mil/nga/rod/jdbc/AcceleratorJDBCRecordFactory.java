package mil.nga.rod.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.PropertyLoader;
import mil.nga.exceptions.PropertiesNotLoadedException;
import mil.nga.exceptions.PropertyNotFoundException;
import mil.nga.rod.model.Product;
import mil.nga.rod.model.QueryRequestAccelerator;

/**
 * This is kind of messy because the accelerator record data is stored in a 
 * different schema than the actual product data.  
 * 
 * Non-EJB version of the code used to interface the back-end Oracle database 
 * that stores the information on the ISO files created for "Replication on 
 * Demand".
 * 
 * @author L. Craig Carpenter
 */
public class AcceleratorJDBCRecordFactory 
        implements RoDRecordFactoryConstants, AutoCloseable {

    /**
     * Set up the Log4j system for use throughout the class
     */     
    static final Logger LOGGER = LoggerFactory.getLogger(
            AcceleratorJDBCRecordFactory.class);
    
    // JDBC Connection properties
    private String jdbcDriver       = null;
    private String connectionString = null;
    private String dbUser           = null;
    private String password         = null;
    
    /**
     * Connection to the target database.
     */
    private Connection rodConnection = null;

    /**
     * Default constructor loading the required system properties.
     * 
     * @throws PropertyNotFoundException
     * @throws PropertiesNotLoadedException
     * @throws ClassNotFoundException
     */
    private AcceleratorJDBCRecordFactory () 
            throws PropertyNotFoundException, 
                PropertiesNotLoadedException, 
                ClassNotFoundException {
        
        PropertyLoader props = PropertyLoader.getInstance();
        
        setJdbcDriver(props.getProperty(ACCELERATOR_JDBC_DRIVER_PROPERTY));
        setConnectionString(props.getProperty(ACCELERATOR_JDBC_CONNECTION_STRING));
        setUser(props.getProperty(ACCELERATOR_DB_USERNAME));
        setPassword(props.getProperty(ACCELERATOR_DB_PASSWORD));
        
        Class.forName(getJdbcDriver());
    }
    
 
    /**
     * Retrieve the query request accelerator data from the backing data
     * source.   
     * 
     * @param prod The product that has been selected.
     * @return The associated QueryRequestAccelerator or null if errors 
     * were encountered retrieving the data.
     */
    public QueryRequestAccelerator getRecord(Product prod) {
    	
    	QueryRequestAccelerator record = null;
        PreparedStatement       stmt   = null;
        ResultSet               rs     = null;
        long                    start  = System.currentTimeMillis();
        String                  sql    = 
        		"select NRN, NSN, FILE_DATE, HASH from "
                + ACCELERATOR_TARGET_TABLE_NAME
                + " where NRN=? and NSN=?";
        
    	if (prod != null) {
    		if ((prod.getNRN() != null) && (!prod.getNRN().isEmpty())) {
    			if ((prod.getNSN() != null) && (!prod.getNSN().isEmpty())) {
    				
    				try {
	    				if (getConnection() != null) {
	    					
		                    stmt = getConnection().prepareStatement(sql);
		                    stmt.setString(1, prod.getNRN());
		                    stmt.setString(2, prod.getNSN());
		                    rs   = stmt.executeQuery();
		                    
		                    while (rs.next()) {
		                    	record = new QueryRequestAccelerator
		                    			.QueryRequestAcceleratorBuilder()
		                    			.product(prod)
		                    			.fileDate(rs.getDate("FILE_DATE"))
		                    			.size(rs.getLong("FILE_SIZE"))
		                    			.hash(rs.getString("HASH"))
		                    			.build();
		                    }
		                 
		    				if (LOGGER.isDebugEnabled()) {
		    	                LOGGER.debug("Accelerator record for file [ " 
		    	                        + prod.getPath()
		    	                        + " ] retrieved in [ "
		    	                        + (System.currentTimeMillis() - start) 
		    	                        + " ] ms.");
		    	            }
	    				}
    				}
    				catch (IllegalStateException ise) {
                        LOGGER.warn("Unexpected IllegalStateException raised "
                                + "while loading [ "
                                + ACCELERATOR_TARGET_TABLE_NAME
                                + " ] records from "
                                + "data store.  Error encountered [ "
                                + ise.getMessage()
                                + " ].");
    				}
    				catch (SQLException se) {
    		            LOGGER.error("An unexpected SQLException was raised "
    		            		+ "while attempting to retrieve accelerator "
    		            		+ "record for NRN => [ "
    		                    + prod.getNRN()
    		                    + " ] and NSN => [ "
    		                    + prod.getNSN()
    		                    + " ].  Error message [ "
    		                    + se.getMessage() 
    		                    + " ].");
    		        }
    		        finally {
    		            try { 
    		                if (rs != null) { rs.close(); }
    		            } catch (Exception e) {}
    		            try { 
    		                if (stmt != null) { stmt.close(); } 
    		            } catch (Exception e) {}
    		        }
    			}
    			else {
        			LOGGER.warn("Product NSN is null or empty.  Unable to "
        					+ "generate the cache accelerator record.  Return "
        					+ "value will be null.");
    			}
    		}
    		else {
    			LOGGER.warn("Product NRN is null or empty.  Unable to "
    					+ "generate the cache accelerator record.  Return "
    					+ "value will be null.");
    		}
    	}
    	else {
    		LOGGER.warn("Input product is null.  Unable to generate the "
    				+ "cache accelerator record.  Return value will be "
    				+ "null.");
    	}
    	return record;
    }
    
    /**
     * Insert the data associated with the query request accelerator record 
     * into the backing data store.   
     * @param record The record to insert.
     */
    public void insert (QueryRequestAccelerator record) {
    	
    	String sql = "INSERT INTO " + ACCELERATOR_TARGET_TABLE_NAME 
    			+ " (NRN, NSN, FILE_DATE, FILE_SIZE, HASH) VALUES ?, ?, ?, ?, ?;";
    	PreparedStatement stmt     = null;
    	
    	try {
	    	if (getConnection() != null) {
	    		stmt = getConnection().prepareStatement(sql);
	    		stmt.setString(1, record.getProduct().getNRN());
	    		stmt.setString(2, record.getProduct().getNSN());
	    		stmt.setDate(  3, new java.sql.Date(record.getFileDate().getTime()));
	    		stmt.setLong(  4, record.getSize());
	    		stmt.setString(5, record.getHash());
	    		
	    		stmt.executeUpdate();
	    	}
    	}
    	catch (SQLException se) {
	        LOGGER.error("An unexpected SQLException was raised while "
	                + "attempting to insert a single [ "
	                + ACCELERATOR_TARGET_TABLE_NAME
	                + " ] record in the target data source.  Error "
	                + "message [ "
	                + se.getMessage() 
	                + " ].");
	    }
	    finally {
	        try { 
	            if (stmt != null) { stmt.close(); } 
	        } catch (Exception e) {}
	    }
    }
    
    /**
     * IUpdate an existing query request accelerator record in the backing 
     * data store.   
     * @param record The record to insert.
     */
    public void update (QueryRequestAccelerator record) {
    	
    	String sql = "UPDATE " 
    			+ ACCELERATOR_TARGET_TABLE_NAME 
    			+ " SET FILE_DATE=?, FILE_SIZE=?, HASH=? WHERE NRN=? AND NSN=?";
    	PreparedStatement stmt     = null;
    	
    	try {
	    	if (getConnection() != null) {
	    		stmt = getConnection().prepareStatement(sql);
	    		stmt.setDate(  1, new java.sql.Date(record.getFileDate().getTime()));
	    		stmt.setLong(  2, record.getSize());
	    		stmt.setString(3, record.getHash());
	    		stmt.setString(4, record.getProduct().getNRN());
	    		stmt.setString(5, record.getProduct().getNSN());
	    		stmt.executeUpdate();
	    	}
    	}
    	catch (SQLException se) {
	        LOGGER.error("An unexpected SQLException was raised while "
	                + "attempting to update a single [ "
	                + ACCELERATOR_TARGET_TABLE_NAME
	                + " ] record in the target data source.  Error "
	                + "message [ "
	                + se.getMessage() 
	                + " ].");
	    }
	    finally {
	        try { 
	            if (stmt != null) { stmt.close(); } 
	        } catch (Exception e) {}
	    }
    }
    
    /**
     * Construct a <code>java.sql.Connection</code> from the input database
     * connection properties.
     * 
     * @return A populated <code>java.sql.Connection</code> object.
     * @throws SQLException Thrown if problems were encountered establishing 
     * the database connection. 
     */
    private Connection getConnection() throws SQLException {
        
        if (rodConnection == null) {
            rodConnection = DriverManager.getConnection(
                    getConnectionString(),
                    getUser(),
                    getPassword());
        }
        return rodConnection;
    }
    
    /**
     * Accessor method for the singleton instance of the 
     * ProductQueryResponseMarshaller class.
     * 
     * @return The singleton instance of the ProductQueryResponseMarshaller .
     * class.
     */
    public static AcceleratorJDBCRecordFactory getInstance() 
            throws PropertyNotFoundException, 
                PropertiesNotLoadedException, 
                ClassNotFoundException {
        return RoDRecordFactoryHolder.getSingleton();
    } 
    
    /**
     * Close the database connection if open.
     */
    @Override
    public void close() {
        if (rodConnection != null) {
            LOGGER.info("Closing JDBC connection.");
            try { rodConnection.close(); } catch (Exception e) {}
        }
    }
    
    /**
     * Getter method for the JDBC database connection string.
     * 
     * @return The JDBC database connection string.
     */
    private String getConnectionString() {
        return connectionString;
    }
    
    /**
     * Getter method for the JDBC driver class name.

     * @return The JDBC driver class name.
     */
    private String getJdbcDriver() {
        return jdbcDriver;
    }
    
    /**
     * Getter method for the password associated with the database user.
     * 
     * @return The password associated with the database user.
     */
    private String getPassword() {
        return password;
    }
    
    /**
     * Getter method for the database user.
     * 
     * @return The database user.
     */
    private String getUser() {
        return dbUser;
    }
    
    /**
     * Setter method for the JDBC database connection string.
     * 
     * @param value The value for the JDBC database connection string.
     * @throws PropertyNotFoundException Thrown if the input value is null or
     * empty.
     */
    private void setConnectionString(String value) 
            throws PropertyNotFoundException {
        
        if ((value == null) || (value.isEmpty())) {
            throw new PropertyNotFoundException("Required property [ "
                    + RoDRecordFactoryConstants.ACCELERATOR_JDBC_CONNECTION_STRING
                    + " ] was not supplied.");
        }
        else {
            connectionString = value;
        }    
        
    }
    
    /**
     * Setter method for the JDBC driver class name.
     * 
     * @param value The value for the JDBC driver class name.
     * @throws PropertyNotFoundException Thrown if the input value is null or
     * empty.
     */
    private void setJdbcDriver(String value) 
            throws PropertyNotFoundException {
        
        if ((value == null) || (value.isEmpty())) {
            throw new PropertyNotFoundException("Required property [ "
                    + RoDRecordFactoryConstants.ACCELERATOR_JDBC_DRIVER_PROPERTY
                    + " ] was not supplied.");
        }
        else {
            jdbcDriver = value;
        }    
        
    }
    
    /**
     * Setter method for the password associated with the database user.
     * 
     * @param value The value for the password associated with the database 
     * user.
     * @throws PropertyNotFoundException Thrown if the input value is null or
     * empty.
     */
    private void setPassword(String value) 
            throws PropertyNotFoundException {
        
        if ((value == null) || (value.isEmpty())) {
            throw new PropertyNotFoundException("Required property [ "
                    + RoDRecordFactoryConstants.ACCELERATOR_DB_PASSWORD
                    + " ] was not supplied.");
        }
        else {
            password = value;
        }    
        
    }
    
    /**
     * Setter method for the database user.
     * 
     * @param value The value for the database user.
     * @throws PropertyNotFoundException Thrown if the input value is null or
     * empty.
     */
    private void setUser(String value) 
            throws PropertyNotFoundException {
        
        if ((value == null) || (value.isEmpty())) {
            throw new PropertyNotFoundException("Required property [ "
                    + RoDRecordFactoryConstants.ACCELERATOR_DB_USERNAME
                    + " ] was not supplied.");
        }
        else {
            dbUser = value;
        }    
        
    }
    
    /**
     * Debugging method to print out database connection parameters.
     */
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("RoD Product Datasource: ");
    	sb.append("JDBC Connection String => [ ");
    	sb.append(getConnectionString());
    	sb.append(" ], Database User => [ ");
    	sb.append(getUser());
    	sb.append(" ], Password => [ <hidden> ], JDBC Driver => [ ");
    	sb.append(getJdbcDriver());
    	sb.append(" ], Target Table Name => [ ");
    	sb.append(ACCELERATOR_TARGET_TABLE_NAME);
    	sb.append(" ].");
    	return sb.toString();
    }
    /**
     * Static inner class used to construct the Singleton object.  This class
     * exploits the fact that classes are not loaded until they are referenced
     * therefore enforcing thread safety without the performance hit imposed
     * by the <code>synchronized</code> keyword.
     * 
     * @author L. Craig Carpenter
     */
    public static class RoDRecordFactoryHolder {
        
        /**
         * Reference to the Singleton instance of the RoDRecordFactory.
         */
        private static AcceleratorJDBCRecordFactory _instance = null;
    
        /**
         * Accessor method for the singleton instance of the 
         * RoDRecordFactory.
         * 
         * @return The Singleton instance of the RoDRecordFactory.
         * @throws PropertyNotFoundException Thrown if any of the required 
         * properties are not supplied.
         * @throws PropertiesNotLoadedException Thrown if all required 
         * properties are not supplied. 
         * @throws ClassNotFoundException Thrown if the defined JDBC driver 
         * could not be found. 
         */
        public static AcceleratorJDBCRecordFactory getSingleton() 
                throws PropertyNotFoundException, 
                	PropertiesNotLoadedException, 
                	ClassNotFoundException {
            if (_instance == null) {
                _instance = new AcceleratorJDBCRecordFactory();
            }
            return _instance;
        }
        
    }
}
