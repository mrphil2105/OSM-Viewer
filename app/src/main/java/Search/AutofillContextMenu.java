package Search;

import collections.trie.FinalTrie;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class AutofillContextMenu<Value> extends ContextMenu {
    TextField text;
    AddressDatabase addresses;
    int maxEntries;

    public AutofillContextMenu(TextField text, AddressDatabase addresses, int maxEntries){
        this.text = text;
        this.addresses = addresses;
        this.maxEntries = maxEntries;
        text.textProperty().addListener(this::showPossibleWords);
    }

    public void showPossibleWords(ObservableValue<? extends String> observable, String oldValue, String newValue){
        int inputLength = text.getText().length();
        if(inputLength == 0){
            hide();
            showHistory(addresses.getHistory());
        } else{
            List<Address> results;

            results = addresses.searchAddress(text.getText());

            getItems().clear();

            for(Address a : results){
                var item = new AddressMenuItem(a);
                item.setOnAction(this::autofill);
                getItems().add(item);
            }
            if (!isShowing()) {
                show(text, Side.BOTTOM, 0, 0);
            }
        }
    }

    public void autofill(ActionEvent actionEvent){
        hide(); //TODO gøres måske automatisk
        String inputText = text.getText();


        //TODO: check om nedenunder viker i stedet for udkommenteret
        AddressMenuItem menuItem = (AddressMenuItem) actionEvent.getSource();
        var address = menuItem.getAddress();
        //Address address = AddressDatabase.parse(inputText);

        String replaced;
        if (address.city() == null && address.street() != null) {
            replaced = address.toString();
        } else {
            replaced = inputText.replaceAll(address.city() + "$", address.toString());
        }
        text.setText(replaced);
    }

    public void showHistory(List<Address> history){
        //TODO
    }
}
