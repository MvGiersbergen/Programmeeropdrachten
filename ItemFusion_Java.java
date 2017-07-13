import java.util.*;
import java.io.*;
public class ItemFusion {
    static ArrayList<Item> items = new ArrayList<>();
    static ArrayList<Fusion> fusions = new ArrayList<>();
    static boolean cheapest=true;
    static ArrayList<String> shoppingListItem = new ArrayList<>();
    static ArrayList<Integer> shoppingListPrice = new ArrayList<>();
    static ArrayList<Integer> shoppingListStore = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        importData();                               // importeer de data
        Item desiredItem = askItem();               // vraag naar een item
        cheapest=askCheapest();                     // vraag naar cheapest of fastest
        int levelsDeep = 4;
        Node finalRecipe = findCheapestRecipe (desiredItem, levelsDeep);
        System.out.println(finalRecipe.price);
        printRecipe(finalRecipe, 0);
        makeShoppingList(finalRecipe);
        printShoppingList();
    }

    public static Node findCheapestRecipe (Item desiredItem, int levelsDeep){
        int cheapestPrice=9999;
        int fastestWay=6;
        for (Item loopItem:items){                                              // als het item te koop is, dan is dat nu even de goedkoopste prijs
            if (loopItem.name.equals(desiredItem.name)){
                cheapestPrice=loopItem.price;
                fastestWay=loopItem.store;
            }
        }
        Node nodeThisLoop = new Node(desiredItem, cheapestPrice, fastestWay);               // maak een nieuwe node met de koop-prijs als goedkoopste prijs

        if (levelsDeep>0){
            ArrayList<Fusion> possibleFusionsList=possibleFusions(desiredItem.name);        // maak een lijst met de mogelijke fusions om dit item te maken
            for (Fusion possibleFusionThisLoop:possibleFusionsList){                        // bekijk per mogelijke fusion:
                int sumPrices;
                int maxValue;
                Node item1=findCheapestRecipe(nameToItem(possibleFusionThisLoop.item1),levelsDeep-1);     // vind de goedkoopste manier om het 1e item van de fusion te maken (dit wordt eerst 5 keer herhaald, daarna gaat het programma pas verder met de volgende regel)
                Node item2=findCheapestRecipe(nameToItem(possibleFusionThisLoop.item2),levelsDeep-1);     // vind de goedkoopste manier om de tweede fusion te maken (meteen van de 5e fusion)
                sumPrices=item1.price+item2.price;
                maxValue=Math.max(item1.store, item2.store);
                if ((cheapest && sumPrices<cheapestPrice) || (!cheapest && maxValue<fastestWay && sumPrices<cheapestPrice)){
                    cheapestPrice=sumPrices;
                    fastestWay=maxValue;
                    nodeThisLoop.addParents(item1,item2);
                }
            }
        }
        return nodeThisLoop;
    }

    public static void printRecipe (Node node, int counter){
        System.out.print(node.item.name+calculateSpaces(node.item.name));
        if (node.parents.size()>0) {
            counter++;
            System.out.print(" <-+-- ");
            printRecipe(node.parents.get(0), counter);
            System.out.println();
            for (int i = 0; i < counter; i++) {
                System.out.print("             |   ");
            }

            System.out.println();
            for (int j = 0; j < counter; j++) {
                System.out.print("          ");
                if (j < counter - 1) {
                    System.out.print("   |   ");
                }
            }

            System.out.print("   +-- ");
            printRecipe(node.parents.get(1), counter);
        }
    }

    public static String calculateSpaces(String input){
        String printSpaces="";
        int lengthDifference=10-input.length();

        for (int i=0; i<lengthDifference; i++){
            printSpaces=printSpaces+" ";
        }
        return printSpaces;
    }

    public static void makeShoppingList(Node node){

        if (node.parents.size()>0) {
            makeShoppingList(node.parents.get(0));
            makeShoppingList(node.parents.get(1));
        } else {
            shoppingListItem.add(node.item.name);
            shoppingListPrice.add(node.item.price);
            shoppingListStore.add(node.item.store);
        }
    }

    public static void printShoppingList(){
        ArrayList<String> finalListItem = new ArrayList<>();
        ArrayList<Integer> finalListPrice = new ArrayList<>(), finalListStore = new ArrayList<>(), finalListAmount = new ArrayList<>(), multipliedItemsPrice = new ArrayList<>();

        for (int i=0;i<shoppingListItem.size();i++){
            if (!finalListItem.contains(shoppingListItem.get(i))){
                finalListItem.add((String)shoppingListItem.get(i));
                finalListPrice.add((int)shoppingListPrice.get(i));
                finalListStore.add((int)shoppingListStore.get(i));
                finalListAmount.add(1);
            } else {
                int index = finalListItem.indexOf(shoppingListItem.get(i));
                finalListAmount.set(index,finalListAmount.get(index)+1);
            }
        }
        System.out.println();
        System.out.println();
        for (int i=0;i<finalListItem.size();i++){
            int sum=finalListAmount.get(i)*finalListPrice.get(i);
            multipliedItemsPrice.add(sum);

            System.out.println(finalListAmount.get(i)+"x "+finalListItem.get(i)+calculateSpaces(finalListItem.get(i))+finalListAmount.get(i)+"x "+finalListPrice.get(i)+" ("+finalListStore.get(i)+") = "+sum);
        }
        System.out.println("------------------------------");

        int totalCosts=0;
        for (int i=0;i<finalListPrice.size();i++){
            totalCosts+=multipliedItemsPrice.get(i);
        }

        System.out.println("Total price             = "+totalCosts);

    }

    public static Item nameToItem (String thisName){
        Item returnItem=items.get(0);
        for (Item thisItem:items){
            if (thisItem.name.equals(thisName)){
                returnItem=thisItem;
            }
        }
        return returnItem;
    }
    public static ArrayList possibleFusions (String item){
        ArrayList<Fusion> possibleFusions = new ArrayList<>();
        for (Fusion oneFusion:fusions){
            if (oneFusion.name.equals(item)){
                possibleFusions.add(oneFusion);
            }
        }
        return possibleFusions;
    }
    public static Item askItem () {
        System.out.println("Which item do you want?");
        Scanner in = new Scanner(System.in);
        String item = in.nextLine();
        Item itemItem=items.get(0);
        Boolean itemPresent=false;
        for (Item oneItem:items){
            if (oneItem.name.equals(item)){
                itemItem=oneItem;
                itemPresent=true;
            }
        }
        if (!itemPresent){                                          // als er nog geen item van is, maak een nieuw item dat altijd duurder is dan de andere
            items.add(new Item(item,9999,6));
        }
        for (Item oneItem:items){                                   // en zoek dan nog een keer naar het item in de lijst
            if (oneItem.name.equals(item)){
                itemItem=oneItem;
            }
        }
        return itemItem;
    }
    public static boolean askCheapest(){
        System.out.println("Cheapest (yes=cheapest, no=fastest)?");
        Scanner inn = new Scanner(System.in);
        String cheapestIn = inn.nextLine();
        Boolean cheapest= true;
        if (cheapestIn.equals("yes")){
            cheapest=true;
        } else if (cheapestIn.equals("no")){
            cheapest=false;
        }
        return cheapest;
    }
    private static void importData() throws IOException {
        Scanner scanner = new Scanner(new File("item-fusion-data.txt"));
        int store=0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains("Store Items")){
                store=findStore(line);
            }

            String[] splitString;
            if (line.contains("~")&&!line.contains("Cost")&&(line.contains("*")||store==1)){
                addItem(line, store);
            }
            if (line.contains("+")){
                addFusion(line);

            }
        }
    }

    public static int findStore (String line){
        int store=0;
        switch (line) {
            case "Beginning Store Items:":
                store = 1; break;
            case "Second Store Items: (* for new)":
                store = 2; break;
            case "Third Store Items: (* for new)":
                store = 3; break;
            case "4th Store Items: (* for new) (Quest The Legendary Mermaid adds Mermaid Pearls)":
                store = 4; break;
            case "5th Store Items: (* for new)":
                store = 5; break;
        }
        return store;
    }

    public static void addItem(String line, int store){
        String[] splitString = line.split(" ~ ");
        if (splitString[0].contains("*")){
            splitString[0]=splitString[0].replace("*", "");
        }
        items.add(new Item(splitString[0], Integer.parseInt(splitString[1]),store));
    }

    public static void addFusion (String line){
        String[] splitString = line.split(" \\+ | = | =");
        fusions.add(new Fusion(splitString[0],splitString[1],splitString[2]));
    }
}
class Item {
    String name;
    int price;
    int store;
    public Item (String nameIN, int priceIN, int storeIN) {
        name=nameIN;
        price=priceIN;
        store=storeIN;
    }
}
class Fusion {
    String name;
    String item1;
    String item2;
    public Fusion (String item1IN, String item2IN, String nameIN) {
        name=nameIN;
        item1=item1IN;
        item2=item2IN;
    }
}
class Node {
    Item item;
    int price;
    int store;
    ArrayList <Node> parents;
    public Node (Item itemIN, int newPriceIN, int storeIN){
        item=itemIN;
        price=newPriceIN;
        store=storeIN;
        parents=new ArrayList<>();
    }
    public void addParents (Node parent1, Node parent2){
        parents.clear();
        parents.add(parent1);
        parents.add(parent2);
        price=parent1.price+parent2.price;
        store=Math.max(parent1.store, parent2.store);
    }
}
