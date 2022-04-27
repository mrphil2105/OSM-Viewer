package Search;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AddressTest {

    static AddressDatabase addressDatabase;

   @BeforeAll
   static void initiateAddresses(){
      addressDatabase=new AddressDatabase();
      addressDatabase.addAddress(new AddressBuilder());

      addressDatabase.buildTries();

   }


   @Test
   static void testOnlyStreets(){
       var address = AddressDatabase.parse("");
       address.street(SearchTextField.reformat(address.getStreet()));
       address.city(SearchTextField.reformat(address.getCity()));
        var searchList = addressDatabase.possibleAddresses(address.build(),5);
   }

    @Test
    static void testStreetAndHousenumber(){
        var address = AddressDatabase.parse("");
        address.street(SearchTextField.reformat(address.getStreet()));
        address.city(SearchTextField.reformat(address.getCity()));
        var searchList = addressDatabase.possibleAddresses(address.build(),5);

    }

    @Test
    static void testStreetAndHousenumberAndCity(){
        var address = AddressDatabase.parse("");
        address.street(SearchTextField.reformat(address.getStreet()));
        address.city(SearchTextField.reformat(address.getCity()));
        var searchList = addressDatabase.possibleAddresses(address.build(),5);

    }

    @Test
    static void testFullAddress(){
        var address = AddressDatabase.parse("");
        address.street(SearchTextField.reformat(address.getStreet()));
        address.city(SearchTextField.reformat(address.getCity()));
        var searchList = addressDatabase.possibleAddresses(address.build(),5);

    }

    @Test
    static void testFalseInput(){
        var address = AddressDatabase.parse("");
        address.street(SearchTextField.reformat(address.getStreet()));
        address.city(SearchTextField.reformat(address.getCity()));
        var searchList = addressDatabase.possibleAddresses(address.build(),5);

        var address2 = AddressDatabase.parse("efAQEgsgrf");
        address2.street(SearchTextField.reformat(address2.getStreet()));
        address2.city(SearchTextField.reformat(address2.getCity()));
        var searchList2 = addressDatabase.possibleAddresses(address2.build(),5);

        var address3 = AddressDatabase.parse("efAQEgsgrf");
        address3.street(SearchTextField.reformat(address3.getStreet()));
        address3.city(SearchTextField.reformat(address3.getCity()));
        var searchList3 = addressDatabase.possibleAddresses(address3.build(),5);

    }



}
