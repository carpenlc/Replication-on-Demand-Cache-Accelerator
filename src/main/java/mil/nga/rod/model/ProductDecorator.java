package mil.nga.rod.model;

/**
 * Adaptor class allowing us to decorate the <code>Product</code> object with
 * the on-disk file information.
 * 
 * @author L. Craig Carpenter
 */
public class ProductDecorator {

	/**
	 * The Product that will have the decorations applied.
	 */
	protected Product product;
	
	public ProductDecorator() {}
	
	/**
	 * Constructor for the Product data.
	 * @param prod The Product data.
	 */
	protected ProductDecorator(Product prod) {
		product = prod;
	}
	
	/**
	 * Accessor method for the Product data.
	 * @return The RoD Product data.
	 */
	public Product getProduct() {
		return product;
	}
	
}
