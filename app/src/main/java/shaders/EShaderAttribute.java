// Credit: https://www.coding-daddy.xyz/node/16

package shaders;

/**
 * Represents shader attributes.
 * 
 * @author serhiy
 */
public enum EShaderAttribute {
	POSITION("inPosition"), COLOR("inColor"), ORTHO("inOrtho"), TRANS("inTrans");

	private final String attributeName;

	EShaderAttribute(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 * @return shader attribute name as it is appearing in the shader source
	 *         code.
	 */
	public String getAttributeName() {
		return attributeName;
	}
}
