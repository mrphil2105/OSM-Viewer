package Search;

import javafx.geometry.Side;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Locale;

public class AutofillTextField extends TextField {
    AutofillContextMenu popupEntries;
    AddressDatabase addressDatabase;

    public AutofillTextField(){
    }

    public void init(AddressDatabase addressDatabase){
        this.addressDatabase = addressDatabase;
        popupEntries = new AutofillContextMenu(this, addressDatabase);
    }

    public void handleSearchChange(String text){
        int inputLength = getText().length();
        if(inputLength == 0){
            popupEntries.hide();
            showHistory(addressDatabase.getHistory());
        } else{
            List<Address> results;


            var searchedAddressBuilder = AddressDatabase.parse(getText());

            if(searchedAddressBuilder == null){
                return;
            };

            searchedAddressBuilder.street(capitalize(searchedAddressBuilder.getStreet()));
            searchedAddressBuilder.city(capitalize(searchedAddressBuilder.getCity()));

            var searchedAddress = searchedAddressBuilder.build();

            boolean showStreet = (searchedAddress.street() != null);
            boolean showHouse = (searchedAddress.houseNumber() != null);
            boolean showCity = (searchedAddress.city() != null);
            boolean showPostcode = (searchedAddress.postcode() != null);
            if(searchedAddress.street() != null) showCity = true;

            //TODO evt. lav alle bogstaver store i starten af ord


            results = addressDatabase.autofillSearch(searchedAddressBuilder,5);
            popupEntries.getItems().clear();

            for(Address a : results){
                if(a == null) continue;
                var item = new AddressMenuItem(a, showStreet, showHouse, showCity, showPostcode);
                item.setOnAction(popupEntries::onMenuClick);
                popupEntries.getItems().add(item);
            }
            if (!popupEntries.isShowing()) {
                popupEntries.show(this, Side.BOTTOM, 0, 0);
            }
        }
    }
    private String capitalize(String string){
        if(string == null) return null;

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

    public void showHistory(List<Address> history){
        //TODO
    }

}