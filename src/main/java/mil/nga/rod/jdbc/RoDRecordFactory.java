package mil.nga.rod.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.PropertyLoader;
import mil.nga.exceptions.PropertiesNotLoadedException;
import mil.nga.exceptions.PropertyNotFoundException;
import mil.nga.rod.model.Product;

/**
 * Non-EJB version of the code used to interface the back-end Oracle database 
 * that stores the information on the ISO files created for "Replication on 
 * Demand".
 * 
 * @author L. Craig Carpenter
 */
public class RoDRecordFactory 
        implements RoDRecordFactoryConstants, AutoCloseable {

    /**
     * Set up the Log4j system for use throughout the class
     */     
    static final Logger LOGGER = LoggerFactory.getLogger(
            RoDRecordFactory.class);
    
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
     * 
     * @throws PropertyNotFoundException
     * @throws PropertiesNotLoadedException
     * @throws ClassNotFoundException
     */
    private RoDRecordFactory () 
            throws PropertyNotFoundException, 
                PropertiesNotLoadedException, 
                ClassNotFoundException {
        
        PropertyLoader props = PropertyLoader.getInstance();
        
        setJdbcDriver(props.getProperty(JDBC_DRIVER_PROPERTY));
        setConnectionString(props.getProperty(JDBC_CONNECTION_STRING));
        setUser(props.getProperty(DB_USERNAME));
        setPassword(props.getProperty(DB_PASSWORD));
        
        Class.forName(getJdbcDriver());
    }
    
    /**
     * Get a list of all of the product records in the back-end data store.
     * 
     * @return A list of all Products in the back-end data store.
     */
    public List<Product> getAllProducts() {
        
        List<Product>     products = new ArrayList<Product>();
        PreparedStatement stmt     = null;
        ResultSet         rs       = null;
        long              start    = System.currentTimeMillis();
        int               counter  = 0;
        String            sql      = "select PROD_TYPE, MEDIA_NAME, NRN, "
                + "NSN, EDITION, LOAD_DATE, FILE_DATE, SEC_CLASS, CLASS_DESC, "
                + "SEC_REL, REL_DESC, UNIX_PATH, HYPERLINK_URL, ALL_NOTES, "
                + "ISO3CHR, AOR_CODE, COUNTRY_NAME, PRODUCT_SIZE_BYTES from "
                + TARGET_TABLE_NAME
                + " order by FILE_DATE desc";
        
            
        try { 
            if (getConnection() != null) {

                stmt = getConnection().prepareStatement(sql);
                rs   = stmt.executeQuery();
                
                while (rs.next()) {
                    try {
                        Product product = new Product.ProductBuilder()
                                .aorCode(rs.getString("AOR_CODE"))
                                .classification(rs.getString("SEC_CLASS"))
                                .classificationDescription(
                                        rs.getString("CLASS_DESC"))
                                .countryName(rs.getString("COUNTRY_NAME"))
                                .edition(rs.getLong("EDITION"))
                                .fileDate(rs.getDate("FILE_DATE"))
                                .iso3char(rs.getString("ISO3CHR"))
                                .loadDate(rs.getDate("LOAD_DATE"))
                                .mediaName(rs.getString("MEDIA_NAME"))
                                .notes(rs.getString("ALL_NOTES"))
                                .nsn(rs.getString("NSN"))
                                .nrn(rs.getString("NRN"))
                                .path(rs.getString("UNIX_PATH"))
                                .productType(rs.getString("PROD_TYPE"))
                                .releasability(rs.getString("SEC_REL"))
                                .releasabilityDescription(
                                        rs.getString("REL_DESC"))
                                .size(rs.getLong("PRODUCT_SIZE_BYTES"))
                                .url(rs.getString("HYPERLINK_URL"))
                                .build();
                        products.add(product);
                    }
                    catch (IllegalStateException ise) {
                        LOGGER.warn("Unexpected IllegalStateException raised "
                                + "while loading [ "
                                + TARGET_TABLE_NAME
                                + " ] records from "
                                + "data store.  Error encountered [ "
                                + ise.getMessage()
                                + " ].");
                        counter++;
                    }
                }
            }
            else {
                LOGGER.warn("Unable to obtain a connection to the target "
                        + "database.  An empty List will be returned to "
                        + "the caller.");
            }
        }
        catch (SQLException se) {
            LOGGER.error("An unexpected SQLException was raised while "
                    + "attempting to retrieve all [ "
                    + TARGET_TABLE_NAME
                    + " ] records from the target data source.  Error "
                    + "message [ "
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
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[ " 
                    + products.size()
                    + " ] records selected in [ "
                    + (System.currentTimeMillis() - start) 
                    + " ] ms.  Of the records selected [ "
                    + counter
                    + " ] contained data errors.");
        }
        return products;
    }
    
    /**
     * Get a list of products that match the input NRN/NSN.  
     * 
     * @param nrn The NRN to select.
     * @param nsn The NSN to select.
     * @return A list of products matching the input NRN/NSN.
     */
    public List<Product> getProducts(String nrn, String nsn) {
        
        List<Product>     products = new ArrayList<Product>();
        PreparedStatement stmt     = null;
        ResultSet         rs       = null;
        long              start    = System.currentTimeMillis();
        int               counter  = 0;
        String            sql      = "select PROD_TYPE, MEDIA_NAME, NRN, "
                + "NSN, EDITION, LOAD_DATE, FILE_DATE, SEC_CLASS, CLASS_DESC, "
                + "SEC_REL, REL_DESC, UNIX_PATH, HYPERLINK_URL, ALL_NOTES, "
                + "ISO3CHR, AOR_CODE, COUNTRY_NAME, PRODUCT_SIZE_BYTES from "
                + TARGET_TABLE_NAME
                + " where NRN=? and NSN=? order by FILE_DATE desc";
        
            
        try { 
            if ((nrn != null) && (!nrn.isEmpty())) {
                if ((nsn != null) && (!nsn.isEmpty())) {
                    if (getConnection() != null) {
        
                        stmt = getConnection().prepareStatement(sql);
                        stmt.setString(1, nrn);
                        stmt.setString(2, nsn);
                        rs   = stmt.executeQuery();
                        
                        while (rs.next()) {
                            try {
                                Product product = new Product.ProductBuilder()
                                        .aorCode(rs.getString("AOR_CODE"))
                                        .classification(rs.getString("SEC_CLASS"))
                                        .classificationDescription(
                                                rs.getString("CLASS_DESC"))
                                        .countryName(rs.getString("COUNTRY_NAME"))
                                        .edition(rs.getLong("EDITION"))
                                        .fileDate(rs.getDate("FILE_DATE"))
                                        .iso3char(rs.getString("ISO3CHR"))
                                        .loadDate(rs.getDate("LOAD_DATE"))
                                        .mediaName(rs.getString("MEDIA_NAME"))
                                        .notes(rs.getString("ALL_NOTES"))
                                        .nsn(rs.getString("NSN"))
                                        .nrn(rs.getString("NRN"))
                                        .path(rs.getString("UNIX_PATH"))
                                        .productType(rs.getString("PROD_TYPE"))
                                        .releasability(rs.getString("SEC_REL"))
                                        .releasabilityDescription(
                                                rs.getString("REL_DESC"))
                                        .size(rs.getLong("PRODUCT_SIZE_BYTES"))
                                        .url(rs.getString("HYPERLINK_URL"))
                                        .build();
                                products.add(product);
                            }
                            catch (IllegalStateException ise) {
                                LOGGER.warn("Unexpected IllegalStateException raised "
                                        + "while loading [ "
                                        + TARGET_TABLE_NAME
                                        + " ] records from "
                                        + "data store.  Error encountered [ "
                                        + ise.getMessage()
                                        + " ].");
                                counter++;
                            }
                        }
                    }
                    else {
                        LOGGER.warn("Unable to obtain a connection to the target "
                                + "database.  An empty List will be returned to "
                                + "the caller.");
                    }
                }
                else {
                    LOGGER.warn("Input NSN is null.  Query wasn't executed.  "
                            + "Return array is empty.");
                }
            }
            else {
                LOGGER.warn("Input NRN is null.  Query wasn't executed.  "
                        + "Return array is empty.");     
            }
        }
        catch (SQLException se) {
            LOGGER.error("An unexpected SQLException was raised while "
                    + "attempting to retrieve all [ "
                    + TARGET_TABLE_NAME
                    + " ] records from the target data source.  Error "
                    + "message [ "
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
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[ " 
                    + products.size()
                    + " ] records selected in [ "
                    + (System.currentTimeMillis() - start) 
                    + " ] ms.  Of the records selected [ "
                    + counter
                    + " ] contained data errors.");
        }
        return products;
    }
    
    /**
     * Get a list of AOR codes from the back end data source.
     * 
     * @return The list of AOR codes.
     */
    public List<String> getAORCodes() {
        
        List<String>      aors   = new ArrayList<String>();
        PreparedStatement stmt   = null;
        ResultSet         rs     = null;
        long              start  = System.currentTimeMillis();
        String            sql    = "select distinct(AOR_CODE) from "
                + TARGET_TABLE_NAME;
        
        try {
            if (getConnection() != null) {
                stmt = getConnection().prepareStatement(sql);
                rs   = stmt.executeQuery();
                while (rs.next()) {
                    aors.add(rs.getString("AOR_CODE"));
                }
            }
            else {
                LOGGER.warn("Unable to obtain a connection to the target "
                        + "database.  An empty List will be returned to "
                        + "the caller.");
            }
        }
        catch (SQLException se) {
            LOGGER.error("An unexpected SQLException was raised while "
                    + "attempting to retrieve a list of AOR Codes from "
                    + "the target data source.  Error message [ "
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
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[ "
                    + aors.size() 
                    + " ] AOR_CODES selected in [ "
                    + (System.currentTimeMillis() - start) 
                    + " ] ms.");
        }
        return aors;
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
     * Get a list of countries from the back end data source.
     * 
     * @return The list of countries codes.
     */
    public List<String> getCountries() {

        List<String>      countries = new ArrayList<String>();
        PreparedStatement stmt      = null;
        ResultSet         rs        = null;
        long              start     = System.currentTimeMillis();
        String            sql       = "select distinct(COUNTRY_NAME) from "
                + TARGET_TABLE_NAME
                + " order by COUNTRY_NAME";
            
        try {
            if (getConnection() != null) {
                stmt = getConnection().prepareStatement(sql);
                rs   = stmt.executeQuery();
                while (rs.next()) {
                    countries.add(rs.getString("COUNTRY_NAME"));
                }
            }
            else {
                LOGGER.warn("Unable to obtain a connection to the target "
                        + "database.  An empty List will be returned to "
                        + "the caller.");
            }
        }
        catch (SQLException se) {
            LOGGER.error("An unexpected SQLException was raised while "
                    + "attempting to retrieve a list of COUNTRY_NAME "
                    + "from the target data source.  Error message [ "
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
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[ "
                    + countries.size() 
                    + " ] COUNTRY_NAME selected in [ "
                    + (System.currentTimeMillis() - start) 
                    + " ] ms.");
        }
        return countries;
    }
    
    /**
     * Accessor method for the singleton instance of the 
     * ProductQueryResponseMarshaller class.
     * 
     * @return The singleton instance of the ProductQueryResponseMarshaller .
     * class.
     */
    public static RoDRecordFactory getInstance() 
            throws PropertyNotFoundException, 
                PropertiesNotLoadedException, 
                ClassNotFoundException {
        return RoDRecordFactoryHolder.getSingleton();
    } 
    
    /**
     * Get a list of product types from the back end data source.
     * 
     * @return The list of unique product types in the back-end data store.
     */
    public List<String> getProductTypes() {
        
        List<String>      products = new ArrayList<String>();
        PreparedStatement stmt     = null;
        ResultSet         rs       = null;
        long              start    = System.currentTimeMillis();
        String            sql      = "select distinct(PROD_TYPE) from "
                + TARGET_TABLE_NAME;
        
        try {
            if (getConnection() != null) {
                stmt = getConnection().prepareStatement(sql);
                rs   = stmt.executeQuery();
                while (rs.next()) {
                    products.add(rs.getString("PROD_TYPE"));
                }
            }
            else {
                LOGGER.warn("Unable to obtain a connection to the target "
                        + "database.  An empty List will be returned to "
                        + "the caller.");
            }
        }
        catch (SQLException se) {
            LOGGER.error("An unexpected SQLException was raised while "
                    + "attempting to retrieve a list of PROD_TYPE records "
                    + "from the target data source.  Error message [ "
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
        
        
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("[ "
                    + products.size() 
                    + " ] PROD_TYPE records selected in [ "
                    + (System.currentTimeMillis() - start) 
                    + " ] ms.");
        }
        return products;
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
                    + RoDRecordFactoryConstants.JDBC_CONNECTION_STRING
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
                    + RoDRecordFactoryConstants.JDBC_DRIVER_PROPERTY
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
                    + RoDRecordFactoryConstants.DB_PASSWORD
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
                    + RoDRecordFactoryConstants.DB_USERNAME
                    + " ] was not supplied.");
        }
        else {
            dbUser = value;
        }    
        
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
        private static RoDRecordFactory _instance = null;
    
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
        public static RoDRecordFactory getSingleton() 
                throws PropertyNotFoundException, PropertiesNotLoadedException, ClassNotFoundException {
            if (_instance == null) {
                _instance = new RoDRecordFactory();
            }
            return _instance;
        }
        
    }
}
