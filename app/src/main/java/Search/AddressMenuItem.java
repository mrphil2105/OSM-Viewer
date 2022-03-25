package Search;

import javafx.scene.control.CustomMenuItem;
import javafx.scene.text.Text;

public class AddressMenuItem extends CustomMenuItem{
    Address address;

    public AddressMenuItem(Address address){
        super(new Text(address.toString()));
        this.address = address;
        setHideOnClick(true);
    }

    public Address getAddress() {
        return address;
    }
}
