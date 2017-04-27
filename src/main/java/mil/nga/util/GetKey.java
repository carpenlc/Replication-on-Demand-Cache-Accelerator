package mil.nga.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.rod.accelerator.RedisCacheManager;
import mil.nga.rod.jdbc.RoDRecordFactory;
import mil.nga.rod.model.Product;
import mil.nga.util.Options.Multiplicity;
import mil.nga.util.Options.Separator;

public class GetKey {
    
    /**
     * Set up the Log4j system for use throughout the class
     */     
    static final Logger LOGGER = LoggerFactory.getLogger(
            GetKey.class);
    
    /**
     * Usage String presented when the command line arguments do not make 
     * sense.
     */
    public static final String USAGE_STRING = 
            "Usage : java mil.nga.util.GetKey -key=<key name> "
            + "[ -getISORecord ] [ -h ] [ -help ]";
    
    /**
     * Usage String presented when the command line arguments do not make 
     * sense.
     */
    public static final String HELP_STRING = 
            "Usage : java mil.nga.util.GetKey -key=<key name> "
            + "[ -getISORecord ] [ -h ] [ -help ]";
    
    /**
     * Default constructor requiring clients to supply the key to retrieve 
     * from the cache and whether or not to print the corresponding backing
     * record from the data store.
     * 
     * @param key
     * @param getISORecords
     */
    public GetKey(String key, boolean getISORecords) {
        printKeyValue(key);
        if (getISORecords) {
            printISORecords(key);
        }
    }
    
    public String getNRNFromKey(String key) {
        String NRN = null;
        String[] array = key.split("\\+");
        if (array.length == 2) {
            NRN = array[1];
        }
        return NRN;
    }
    
    public String getNSNFromKey(String key) { 
        String NSN = null;
        String[] array = key.split("\\+");
        if (array.length == 2) {
            NSN = array[0];
        }
        return NSN;
    }
    
    public void printISORecords(String key) {
        
        try (RoDRecordFactory factory = RoDRecordFactory.getInstance()) {
            List<Product> products = factory.getProducts(getNRNFromKey(key), getNSNFromKey(key));
            if (products.size() > 0) {
                for (Product prod : products) {
                    System.out.println(prod.toString());
                }
            }
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Print out the key/value pair.
     * 
     * @param key The key to query for.
     */
    public void printKeyValue(String key) {
        try (RedisCacheManager manager = RedisCacheManager.getInstance()) { 
            String value = manager.get(key);
            if ((value == null) || (value.isEmpty())) {
                LOGGER.warn("Input key [ "
                        + key 
                        + " ] does not exist in the cache.");
            }
            else {
                System.out.println("Key => [ "
                        + key 
                        + " ], value => [ "
                        + value
                        + " ].");
            }
        }
    }
    public void execute() {
        
        
        
    }
    
    /**
     * Main method used to process the command line arguments.
     * 
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        
        String  key             = null;
        boolean printISORecords = false;
        
        // set up the command line options
        Options opt = new Options(args, 0);
        opt.getSet().addOption("key", Separator.EQUALS, Multiplicity.ONCE);
        opt.getSet().addOption("getISORecord", Multiplicity.ZERO_OR_ONE);
        opt.getSet().addOption("h", Multiplicity.ZERO_OR_MORE);
        opt.getSet().addOption("help", Multiplicity.ZERO_OR_MORE);
        
        // Ensure the command line options presented make sense.
        if (!opt.check(true, false)) {
            System.err.println(USAGE_STRING);
            System.exit(1);
        }
        
        if ((opt.getSet().isSet("h")) || (opt.getSet().isSet("help"))) {
            System.out.println(HELP_STRING);
            System.exit(0);
        }
        
        if (opt.getSet().isSet("key")) {
            key = opt.getSet().getOption("key").getResultValue(0);
            if ((key == null) || (key.isEmpty())) {
                LOGGER.error("Required input parameter [ key ] is null or not "
                        + "defined.");
            }
            else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("User requested key [ "
                            + key
                            + " ].");
                }
            }
        }
        else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Required input parameter [ key ] is not defined.");
            }
            System.err.println(USAGE_STRING);
            System.exit(1);
        }
        
        if (opt.getSet().isSet("getISORecord")) {
            printISORecords = true;
        }
        
        new GetKey(key, printISORecords).getNSNFromKey(key);
     
    }
}
