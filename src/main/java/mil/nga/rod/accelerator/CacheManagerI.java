package mil.nga.rod.accelerator;

import java.util.Set;

/**
 * Interface defining the methods that the Cache manager code must implement.
 * 
 * @author L. Craig Carpenter
 */
public interface CacheManagerI {

    /**
     * Retrieve a request accelerator record from the cache.
     * 
     * @param key Key to query for. 
     * @return The value associated with the input key.  This method
     * returns null of unable to retrieve the key from the cache.
     */
    public String get(String key);
    
    /**
     * Get a Set containing all of the keys that are currently stored in the 
     * target cache.
     *   
     * @return A Set containing all of the keys stored in the Redis cache.
     * @throws JedisConnectionException Runtime exception thrown if a 
     * connection cannot be made to the local Redis cache. 
     */
    public Set<String> getKeys();
    
    /**
     * Store a key/value pair in the target cache.
     * 
     * @param key Key to query for. 
     * @param value The value associated with the input key.  This method
     * returns null of unable to retrieve the key from the cache.
     */
    public void put(String key, String value);
    
    /**
     * Remove a key/value pair from the target cache.
     * 
     * @param key Key to remove. 
     */
    public void remove(String key);
}
