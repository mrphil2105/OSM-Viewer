// Credit: https://www.coding-daddy.xyz/node/16

package shaders;

/**
 * Represents shader attributes.
 *
 * @author serhiy
 */
public enum Location {
    POSITION("inPosition", LocationType.Attrib),
    COLOR("inColor", LocationType.Attrib),
    ORTHOGRAPHIC("inOrthographic", LocationType.Uniform),
    TRANS("inTrans", LocationType.Uniform);

    private final String name;
    private final LocationType type;
    Location(String name, LocationType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * @return shader attribute name as it is appearing in the shader source
     * code.
     */
    public String getName() {
        return name;
    }

    public LocationType getType() {
        return type;
    }

    enum LocationType {
        Attrib, Uniform
	}
}
