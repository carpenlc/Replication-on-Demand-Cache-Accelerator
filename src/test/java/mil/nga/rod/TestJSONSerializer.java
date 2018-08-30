package mil.nga.rod;


import static org.junit.Assert.*;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import mil.nga.rod.JSONSerializer;
import mil.nga.rod.model.Product;
import mil.nga.rod.model.QueryRequestAccelerator;
import mil.nga.rod.model.TestProduct;
import mil.nga.rod.model.TestQueryRequestAccelerator;

public class TestJSONSerializer {

    private static final DateFormat dateFormatter = 
            new SimpleDateFormat("yyyy-MM-dd");
    
    
    @Test
    public void testStringListSerialization() {
        
        List<String> list = new ArrayList<String>();
        list.add("Element 1");
        list.add("Element 2");
        list.add("Element 3");
        list.add("Element 4");
        list.add("Element 5");
        list.add("Element 6");
        list.add("Element 7");
        
        String serialized = JSONSerializer.getInstance().serialize(list);
        List<String> list2 = JSONSerializer
                    .getInstance()
                    .deserializeToStringList(serialized);
        
        assertTrue(list.equals(list2));
        
    }
    
    @Test
    public void testQueryRequestAcceleratorSerialization() {

        Product product = new Product.ProductBuilder()
                .aorCode(TestProduct.AOR_CODE)
                .classification(TestProduct.CLASSIFICATION)
                .classificationDescription(TestProduct.CLASSIFICATION_DESCRIPTION)
                .countryName(TestProduct.COUNTRY_NAME)
                .edition(TestProduct.EDITION)
                .fileDate(TestProduct.FILE_DATE)
                .iso3Char(TestProduct.ISO3CHR)
                .loadDate(TestProduct.LOAD_DATE)
                .mediaName(TestProduct.MEDIA_NAME)
                .notes(TestProduct.NOTES)
                .nsn(TestProduct.NSN)
                .nrn(TestProduct.NRN)
                .path(TestProduct.PATH)
                .productType(TestProduct.PRODUCT_TYPE)
                .releasability(TestProduct.RELEASABILITY)
                .releasabilityDescription(TestProduct.RELEASABILITY_DESCRIPTION)
                .size(TestProduct.SIZE)
                .url(TestProduct.URL)
                .build();
        
        QueryRequestAccelerator record = new QueryRequestAccelerator.QueryRequestAcceleratorBuilder()
        		.product(product)
        		.size(TestQueryRequestAccelerator.SIZE)
        		.fileDate(TestQueryRequestAccelerator.CURRENT_DATE)
        		.hash(TestQueryRequestAccelerator.HASH)
        		.build();
        
        String serialized = JSONSerializer.getInstance().serialize(record);
        QueryRequestAccelerator record2 = JSONSerializer
                    .getInstance()
                    .deserializeToQueryRequestAccelerator(serialized);
        
        System.out.println(serialized);
        // System.out.println(dateFormatter.format(record.getFileDate()));
        // System.out.println(dateFormatter.format(record2.getFileDate()));
        
        // Compare the strings.
        assertEquals(dateFormatter.format(record.getFileDate()), 
                dateFormatter.format(record2.getFileDate()));
        assertEquals(record.getHash(), record2.getHash());
        assertEquals(record.getPath(), record2.getPath());
        assertEquals(record.getSize(), record2.getSize());
        
    }
    
 
    
}
