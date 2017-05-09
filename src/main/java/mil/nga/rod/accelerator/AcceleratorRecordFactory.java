package mil.nga.rod.accelerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.nga.rod.JSONSerializer;
import mil.nga.rod.model.Product;
import mil.nga.rod.model.QueryRequestAccelerator;
import mil.nga.types.HashType;
import mil.nga.util.FileUtils;
import mil.nga.util.HashGenerator;

/**
 * Class containing the logic required to generate the key/value pair for the
 * query accelerator records.  
 * 
 * @author L. Craig Carpenter
 */
public class AcceleratorRecordFactory {

    /**
     * Set up the Log4j system for use throughout the class
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(
            AcceleratorRecordFactory.class);
    
    /**
     * The hash type to calculate.
     */
    public static final HashType HASH_TYPE = HashType.MD5;

    /**
     * Default constructor enforcing the singleton design pattern.
     */
    private AcceleratorRecordFactory () {}
    
    /**
     * Accessor method for the singleton instance of the 
     * AcceleratorRecordFactory class.
     * 
     * @return The singleton instance of the AcceleratorRecordFactory.
     * class.
     */
    public static AcceleratorRecordFactory getInstance() {
        return AcceleratorRecordFactoryHolder.getSingleton();
    } 
    
    /**
     * Calculate the key that will be used for storage/lookup of the query
     * accelerator records.  
     * 
     * @return The key used to store the accelerator record.  May be empty
     * so callers must check it before attempting to store in the cache.
     */
    public String getKey(Product prod){
        StringBuilder sb = new StringBuilder();
        if (prod != null) {
            if ((prod.getNSN() != null) && (!prod.getNSN().isEmpty())) {
                if ((prod.getNRN() != null) && (!prod.getNRN().isEmpty())) {
                    sb.append(prod.getNSN().trim());
                    sb.append("+");
                    sb.append(prod.getNRN().trim());
                }
                else {
                    LOGGER.error("The input product object contains a null "
                            + "(or empty) value for the NRN.  This is not "
                            + "supposed to happen.");
                }
            }
            else {
                LOGGER.error("The input product object contains a null "
                        + "(or empty) value for the NSN.  This is not "
                        + "supposed to happen.");
            }
        }
        else {
            LOGGER.error("The input product object is null.  Nothing to "
                    + "store.");
        }
        return sb.toString();
    }
    
    /**
     * Based on the key design in which keys are the combination of NSN and 
     * NRN of a given product, the NRN is the second half of the key.  The
     * NRN is used to extract the data from the backing data store.
     * 
     * @param key The target key.
     * @return The product NRN.
     */
    public String getNRNFromKey(String key) {
        String NRN = null;
        String[] array = key.split("\\+");
        if (array.length == 2) {
            NRN = array[1];
        }
        return NRN;
    }
    
    /**
     * Based on the key design in which keys are the combination of NSN and 
     * NRN of a given product, the NSN is the first half of the key.  The
     * NSN is used to extract the data from the backing data store.
     * 
     * @param key The target key.
     * @return The product NRN.
     */
    public String getNSNFromKey(String key) { 
        String NSN = null;
        String[] array = key.split("\\+");
        if (array.length == 2) {
            NSN = array[0];
        }
        return NSN;
    }
    
    /**
     * Serialize the input QueryRequestAccelerator record into a JSON String 
     * that will be cached.
     * 
     * @param record The QueryRequestAccelerator record to be cached.
     * @return The serialized version of the QueryRequestAccelerator record.
     */
    public String getValue(QueryRequestAccelerator record) {
        String value = null;
        if (record != null) {
            value = JSONSerializer.getInstance().serialize(record);
        }
        return value;
    }
    
    
    /**
     * Generate a <code>QueryRequestAccelerator</code> record for storage 
     * in the target cache.
     * 
     * @param prod The database record identifying an on-disk ISO file.
     * @return A QueryRequestAccelerator record to add to the cache.
     * @throws IOException Thrown if there are problems accessing the target 
     * file.
     */
    public QueryRequestAccelerator buildRecord(Product prod) 
            throws IOException {
        
        QueryRequestAccelerator record    = null;
        HashGenerator           generator = new HashGenerator();
        
        if (prod != null) {
            String path = prod.getPath();
            if ((path != null) && (!path.isEmpty())) {
                try {
                    Path p = Paths.get(path);
                    if (Files.exists(p)) {
                        String hash = generator.getHash(p, HASH_TYPE);
                        if (hash != null) {
                            record = new QueryRequestAccelerator
                                    .QueryRequestAcceleratorBuilder()
                                        .fileDate(FileUtils.getActualFileDate(p))
                                        .hash(hash)
                                        .path(path)
                                        .size(FileUtils.getActualFileSize(p))
                                        .build();
                        }
                        else {
                            LOGGER.error("Unable to generate a hash for file [ "
                                    + path
                                    + " ].  See previous error messages for more "
                                    + "information.");
                        }
                    }
                    else {
                    LOGGER.error("Target file [ "
                            + path
                            + " ] does not exist.  Unable to generate an "
                            + "accelerator record.");
                
                    }
                }
                catch (IOException ioe) {
                    LOGGER.error("An unexpected IOException was raised while "
                            + "attempting to access file [ "
                            + path
                            + " ].  Exception message [ "
                            + ioe.getMessage()
                            + " ].  Accelerator record not created.");
                }
            }
            else {
                LOGGER.error("Target file name is null or empty.  Unable to "
                        + "generate an accelerator record.");
            }
        }
        else {
            LOGGER.error("The input product object is null.  Nothing to "
                    + "store.");
        }
        return record;
    }
    
    /**
     * Static inner class used to construct the Singleton object.  This class
     * exploits the fact that classes are not loaded until they are referenced
     * therefore enforcing thread safety without the performance hit imposed
     * by the <code>synchronized</code> keyword.
     * 
     * @author L. Craig Carpenter
     */
    public static class AcceleratorRecordFactoryHolder {
        
        /**
         * Reference to the Singleton instance of the AcceleratorRecordFactory.
         */
        private static AcceleratorRecordFactory _instance = null;
    
        /**
         * Accessor method for the singleton instance of the 
         * AcceleratorRecordFactory.
         * 
         * @return The Singleton instance of the AcceleratorRecordFactory.
         */
        public static AcceleratorRecordFactory getSingleton() {
            if (_instance == null) {
                _instance = new AcceleratorRecordFactory();
            }
            return _instance;
        }
        
    }
}
