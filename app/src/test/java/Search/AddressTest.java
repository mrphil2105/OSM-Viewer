package Search;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddressTest {

    static AddressDatabase addressDatabase;
    static Address[] addresses;


   @BeforeAll
   static void initiateAddresses(){
      addressDatabase=new AddressDatabase();
      addresses=new Address[10];

      addresses[0]=(new Address("Testvej","24",null,null,"4050","Testby",null));
      addresses[1]=(new Address("Testvej","20",null,null,"4050","Testby",null));
      addresses[2]=(new Address("Testvej","21",null,null,"4050","Testby",null));
      addresses[3]=(new Address("Testvej","5",null,null,"4080","Anden Testby",null));
      addresses[4]=(new Address("Testvej","6",null,null,"4090","Tredje Testby",null));
      addresses[5]=(new Address("Anden Testvej","245",null,null,"4050","Testby",null));
      addresses[6]=(new Address("Anden Testvej","3",null,null,"4050","Testby",null));
      addresses[7]=(new Address("Anvej","45",null,null,"4080","Anden Testby",null));
      addresses[8]=(new Address("Endu En Vej","21",null,null,"5080","Fjerde Testby",null));
      addresses[9]=(new Address("Også En Vej","26",null,null,"5080","Fjerde Testby",null));

      for (Address a:addresses){
          addressDatabase.addAddress(a);
      }
      addressDatabase.buildTries();

   }



    @Test
    static void OnlyLetters(){
        var list = search("t");

        var shouldContain=new ArrayList<Address>();
        shouldContain.add(addresses[0]);
        shouldContain.add(addresses[1]);
        shouldContain.add(addresses[2]);
        shouldContain.add(addresses[3]);
        shouldContain.add(addresses[5]);

        assertTrue(containsExactly(list,shouldContain));

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

    static private List<Address> search(String input){
        var address = AddressDatabase.parse(input);
        address.street(SearchTextField.reformat(address.getStreet()));
        address.city(SearchTextField.reformat(address.getCity()));
        return addressDatabase.possibleAddresses(address.build(),5);
    }

    static private boolean containsExactly(List<Address> checkList,List<Address> input){
        for (Address a:input){
            if (!checkList.contains(a)) return false;
        }

        return checkList.size() == input.size();


    }


}
