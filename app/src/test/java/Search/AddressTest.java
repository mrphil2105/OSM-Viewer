package Search;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddressTest {

    static AddressDatabase addressDatabase;
    static Address[] addresses;


   @BeforeAll
   static void initiateAddresses(){
      addressDatabase=new AddressDatabase();
      addresses=new Address[11];

      addresses[0]=(new Address("Testvej","24B",null,null,"4050","Testby",null));
      addresses[1]=(new Address("Testvej","6",null,null,"4050","Testby",null));
      addresses[2]=(new Address("Testvej","21",null,null,"4050","Testby",null));
      addresses[3]=(new Address("Testvej","5",null,null,"4080","Anden Testby",null));
      addresses[4]=(new Address("Testvej","27",null,null,"4090","Tredje Testby",null));
      addresses[5]=(new Address("Anden Testvej","245",null,null,"4050","Testby",null));
      addresses[6]=(new Address("Anden Testvej","3A",null,null,"4050","Testby",null));
      addresses[7]=(new Address("Anvej","35",null,null,"4080","Anden Testby",null));
      addresses[8]=(new Address("Endu En Vej","21",null,null,"5080","Fjerde Testby",null));
      addresses[9]=(new Address("Også En Vej","26",null,null,"5080","Fjerde Testby",null));
      addresses[10]=(new Address("Også En Vej","24",null,null,"6000","Fjerde Testby b",null));

      for (Address a:addresses){
          addressDatabase.addAddress(a);
      }
      addressDatabase.buildTries();

   }


   //tests

    @Test
    public void testOnlyLetters(){
        //should return addreses which street name starts with input letters,but only one per city

        var list = search("t");
        int[] shouldContain = {0,3,4};
        assertTrue(containsExactly(list,shouldContain));

        var list2 = search(" An");
        int[] shouldContain2 = {5,7};
        assertTrue(containsExactly(list2,shouldContain2));

    }

   @Test
   public void testOnlyStreets(){
       //should return addresses with the given street name,but only one per city

       var list = search("testvej");
       int[] shouldContain = {0,3,4};
       assertTrue(containsExactly(list,shouldContain));

       var list2 = search("Endu En Vej");
       int[] shouldContain2 = {8};
       assertTrue(containsExactly(list2,shouldContain2));
   }

    @Test
    public void testStreetAndHousenumber(){
        //should return addreses which street name starts with input letters, and which housenumber starts with the given number

        var list = search("test 2");
        int[] shouldContain = {0,2,4};
        assertTrue(containsExactly(list,shouldContain));

        var list2 = search(" Anden TestVej 3A,");
        int[] shouldContain2 = {6};
        assertTrue(containsExactly(list2,shouldContain2));

        var list3 = search("An 3.");
        int[] shouldContain3 = {6,7};
        assertTrue(containsExactly(list3,shouldContain3));

    }

    @Test
    public void testStreetAndHousenumberAndCity(){
        //should return addreses which street name starts with the first input letters, and which housenumber starts with the given number, and which city starts with the letters after the number

        var list = search("testvej, 2 test");
        int[] shouldContain = {0,2};
        assertTrue(containsExactly(list,shouldContain));

        var list2 = search("An 3, anden testby   ");
        int[] shouldContain2 = {7};
        assertTrue(containsExactly(list2,shouldContain2));

    }

    @Test
    public void testStreetAndHousenumberAndPostcode(){
        //should return addresses which street name starts with the first input letters, which housenumber starts with the given number,  and which postcode matches the second number string

        var list = search("tesT 2 . 4090");
        int[] shouldContain = {4};
        assertTrue(containsExactly(list,shouldContain));

        var list2 = search("An,, 3 4050");
        int[] shouldContain2 = {6};
        assertTrue(containsExactly(list2,shouldContain2));

        var list3 = search("tesT 2 . 4050");
        int[] shouldContain3 = {0,2};
        assertTrue(containsExactly(list3,shouldContain3));

    }

    @Test
    public void testFullAddress(){
        //should return addresses which street name starts with the first input letters, which housenumber starts with the given number, and which city starts with the letters after the number, and which postcode matches the second number string

        var list = search("test  24B   4050 testBy ");
        int[] shouldContain = {0};
        assertTrue(containsExactly(list,shouldContain));



        var list2 = search("Også En Vej, 2 6000 fjerde ");
        int[] shouldContain2 = {10};
        assertTrue(containsExactly(list2,shouldContain2));

    }

    @Test
    public void testFalseInput(){
        var list = search("");
        assertTrue(list ==null || list.isEmpty());

        var list2 = search("drgdfgdcfg");
        assertTrue(list2 ==null || list2.isEmpty());

        var list3 = search("An 3 anden testby 5060");
        assertTrue(list3 ==null || list3.isEmpty());


    }


    //Utils

    private List<Address> search(String input){
        var address = AddressDatabase.parse(input);
        if (address == null) {
            return null;
        }

        address.street(reformat(address.getStreet()));
        address.city(reformat(address.getCity()));



        return addressDatabase.possibleAddresses(address.build(),5);
    }


    private boolean containsExactly(List<Address> checkList, int[] numbers){
        var shouldContain=new ArrayList<Address>();
       for (int i : numbers){
           shouldContain.add(addresses[i]);
       }

       return containsExactly(checkList,shouldContain);

    }

    private boolean containsExactly(List<Address> checkList,List<Address> input){
        for (Address a:input){
            if (!checkList.contains(a)) return false;
        }

        return checkList.size() == input.size();


    }

    public static String reformat(String string) {
        if (string == null) return null;
        string = string.toLowerCase();

        var stringBuilder = new StringBuilder();
        var split = string.split(" ");
        for (String s : split) {
            if (s.length() == 0) continue;
            char[] charArray = s.toCharArray();
            charArray[0] = Character.toUpperCase(charArray[0]);
            var result = new String(charArray);
            stringBuilder.append(result).append(" ");
        }

        return stringBuilder.toString().trim();
    }

}
