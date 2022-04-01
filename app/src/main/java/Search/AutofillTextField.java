package Search;

import javafx.geometry.Side;
import javafx.scene.control.TextField;
import java.util.List;

public class AutofillTextField extends TextField {
    AutofillContextMenu popupEntries;
    AddressDatabase addressDatabase;

    public AutofillTextField(){ //TODO: rename to SearchTextField
    }

    public void init(AddressDatabase addressDatabase){
        this.addressDatabase = addressDatabase;
        popupEntries = new AutofillContextMenu(this, addressDatabase);
    }


    public void handleSearch() {
        var parsedAddress = parseAddress();
        if(parsedAddress == null){
            // throw new Exception("No match");
            //TODO display error "message" to user
            return;
        }
        var result = addressDatabase.search(parsedAddress);
        if(result == null){
            //TODO display error "message" to user
            return;
        }
        var node = result.node();

        System.out.println("Found node with lat: " + node.lat() + " and lon: " + node.lon());
    }

    public void handleSearchChange(){
        if(getText().length() == 0){
            popupEntries.hide();
            addressDatabase.getHistory().forEach(e -> popupEntries.getItems().add(new AddressMenuItem(e)));
        } else{
            List<Address> results;

            var searchedAddress = parseAddress();
            if(searchedAddress == null) return;

            results = addressDatabase.possibleAddresses(searchedAddress,5);


            boolean showStreet = (searchedAddress.street() != null);
            boolean showHouse = (searchedAddress.houseNumber() != null);
            boolean showCity = (searchedAddress.city() != null);
            boolean showPostcode = (searchedAddress.postcode() != null);
            if(searchedAddress.street() != null) showCity = true;

            popupEntries.getItems().clear();

            for(Address a : results){
                if(a == null) continue;
                var item = new AddressMenuItem(a, showStreet, showHouse, showCity, showPostcode);
                item.setOnAction(popupEntries::onMenuClick);
                popupEntries.getItems().add(item);
            }
        }
        if (!popupEntries.isShowing()) {
            popupEntries.show(this, Side.BOTTOM, 0, 0);
        }
    }
    private String capitalize(String string){
        if(string == null) return null;

        string = string.toLowerCase();

        var stringBuilder = new StringBuilder();
        var split = string.split(" ");
        for(String s : split){
            char[] charArray = s.toCharArray();
            charArray[0] = Character.toUpperCase(charArray[0]);
            var result = new String(charArray);
            stringBuilder.append(result);
        }

        return stringBuilder.toString();
    }

    private Address parseAddress(){
        var searchedAddressBuilder = AddressDatabase.parse(getText());

        if(searchedAddressBuilder == null){
            return null;
        };


        searchedAddressBuilder.street(capitalize(searchedAddressBuilder.getStreet()));
        searchedAddressBuilder.city(capitalize(searchedAddressBuilder.getCity()));

        return searchedAddressBuilder.build();
    }

}