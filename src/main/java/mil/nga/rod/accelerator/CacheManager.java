package mil.nga.rod.accelerator;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.PropertyLoader;
import mil.nga.exceptions.PropertiesNotLoadedException;
import mil.nga.exceptions.PropertyNotFoundException;
import mil.nga.rod.JSONSerializer;
import mil.nga.rod.jdbc.RoDRecordFactory;
import mil.nga.rod.model.Product;
import mil.nga.rod.model.QueryRequestAccelerator;
import redis.clients.jedis.exceptions.JedisConnectionException;


/**
 * Class containing the logic required to load a local Redis cache with data
 * used to increase the performance of product queries.  
 * 
 * @author L. Craig Carpenter
 */
public class CacheManager {

    /**
     * Set up the Log4j system for use throughout the class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            CacheManager.class);
    
    /** 
     * Expected format associated with dates coming in from callers.
     */
    private static final String INPUT_DATE_FORMAT_STRING = 
            "yyyy-MM-dd hh:mm:ss";
    
    /**
     * Date formatter objecf for printing output information.
     */
    private static final DateFormat dateFormatter = 
            new SimpleDateFormat(INPUT_DATE_FORMAT_STRING);

    /**
     * Default constructor.
     */
    public CacheManager() { }
    
    /**
     * Main method containing the logic required to update the accelerator cache.
     */
    public void updateAcceleratorCache() {
    
        long start          = System.currentTimeMillis();
        int  successCounter = 0;
        int  failedCounter  = 0;
        int  totalCounter   = 0;
        
        LOGGER.info("Cache update started at [ "
                + dateFormatter.format(new Date(System.currentTimeMillis()))
                + " ].");
        
        try {
            
            List<Product> records = RoDRecordFactory.getInstance().getAllProducts();
            
            if ((records != null) && (records.size() > 0)) {
                AcceleratorRecordFactory factory = new AcceleratorRecordFactory();
                for (Product record : records) {
                    totalCounter++;
                    String key = factory.getKey(record);
                    if (RedisCacheManager.getInstance().get(key) == null) {
                        
                        String value = factory.getValue(
                                factory.buildRecord(record));
                        
                        if (value != null) {
                            RedisCacheManager.getInstance().put(key, value);
                            successCounter++;
                        }
                        else {
                            failedCounter++;
                            LOGGER.warn("Unable to create accelerator record "
                                    + "for key [ "
                                    + key
                                    + " ].");
                        }
                    }
                }
            }
            else {
                LOGGER.error("There are no records in the target data store.");
            }
        }
        catch (IOException ioe) {
            LOGGER.error("Unexpected IOException raised while attempting "
                    + "to access on-disk files.  Error message [ "
                    + ioe.getMessage()
                    + " ]. ");
        }
        catch (PropertyNotFoundException pnfe) {
            LOGGER.error("PropertyNotFoundException raised "
                    + "while attempting to establish a connection to the "
                    + "back end data store.  Please ensure the required "
                    + "properties are available.  Property-specific error "
                    + "message [ "
                    + pnfe.getMessage()
                    + " ].");
        }
        catch (PropertiesNotLoadedException pnle) {
            LOGGER.error("Unexpected PropertiesNotLoadedException raised "
                    + "while attempting to establish a connection to the "
                    + "back end data store.  Error message [ "
                    + pnle.getMessage()
                    + " ].  Please ensure the system properties file "
                    + "is available.");
        }
        catch (ClassNotFoundException cnfe) {
            LOGGER.error("Unexpected ClassNotFoundException raised while "
                    + "attempting to establish a connection to the back "
                    + "end data store.  Error message [ "
                    + cnfe.getMessage()
                    + " ].  Please ensure the data store JDBC driver "
                    + "library is on the class path.");
        }
        catch (JedisConnectionException jce) {
            LOGGER.error("Unexpected JedisConnectionException raised while "
                    + "attempting to establish a connection to the Redis "
                    + "cache.  Error message [ "
                    + jce.getMessage()
                    + " ].  Please ensure that the cache is available "
                    + "or disable the caching feature.");
        }
        finally {
            try {
                RedisCacheManager.getInstance().close(); 
                RoDRecordFactory.getInstance().close();
            } 
            catch (Exception e) {}
        }

        LOGGER.info("Cache update completed at [ "
                + dateFormatter.format(new Date(System.currentTimeMillis()))
                + " ] in [ "
                + (System.currentTimeMillis() - start)
                + " ] ms.");
        LOGGER.info("[ "
                + totalCounter
                + " ] records were processed.  [ "
                + successCounter 
                + " ] were successfully updated, [ "
                + failedCounter
                + " ] records failed to update.");
    }
    
    
    /**
     * Main method invoked to start the Replication-on-Demand cache management
     * application.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        try {
        System.out.println(PropertyLoader.getInstance().loadProperties().toString());

            List<String> list = RoDRecordFactory.getInstance().getAORCodes();
            String test = JSONSerializer.getInstance().marshall(list);
            
            List<String> list2 = JSONSerializer.getInstance().deserializeToStringList(test);
            for (String item : list2) {
                System.out.println(item);
            }
            list = RoDRecordFactory.getInstance().getCountries();
            test = JSONSerializer.getInstance().marshall(list);
            list2 = JSONSerializer.getInstance().deserializeToStringList(test);
            for (String item : list2) {
                System.out.println(item);
            }
            
            list = RoDRecordFactory.getInstance().getProductTypes();
            test = JSONSerializer.getInstance().marshall(list);
            list2 = JSONSerializer.getInstance().deserializeToStringList(test);
            for (String item : list) {
                System.out.println(item);
            }
            
            QueryRequestAccelerator record = new QueryRequestAccelerator.QueryRequestAcceleratorBuilder()
                                                .fileDate(new java.sql.Date(System.currentTimeMillis()))
                                                .path("/path/to/file")
                                                .hash("12345-MD5-12345")
                                                .size(1500L)
                                                .build();
            
            String marshalled = JSONSerializer.getInstance().marshall(record);
            System.out.println(marshalled);
            QueryRequestAccelerator record2 = JSONSerializer.getInstance().deserializeToQueryRequestAccelerator(marshalled);
            System.out.println(record2.toString());
            
            List<Product> prods = RoDRecordFactory.getInstance().getAllProducts();
            
            //RoDRecordFactory.getInstance().close();
            
            
            (new CacheManager()).updateAcceleratorCache();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
