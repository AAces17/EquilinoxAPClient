package aaces.aploader.client;

import aaces.aploader.APAgent;
import aaces.aploader.APBridge;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ItemManager {
    private int index;
    private int receivedDpPerMinBonuses;

//region Item ID Maps

    public static final Map<Integer, String> ItemIDtoSpeciesName = ImmutableMap.<Integer, String>builder()
            .put(10001, "Grass Tuft")
            .put(10002, "Daisy")
            .put(10003, "Stones")
            .put(10004, "Brown Stones")
            .put(10007, "Seaweed")
            .put(10010, "Button Mushroom")
            .put(10011, "Oak Tree")
            .put(10013, "Rosemary")
            .put(10015, "Shell")
            .put(10016, "Rock")
            .put(10017, "Brown Rock")
            .put(10019, "Birch Tree")
            .put(10023, "Tall Tree")
            .put(10029, "Jungle Rocks")
            .put(10032, "Yucca")
            .put(10033, "Snow Rocks")
            .put(10042, "Tomato Plant")
            .put(10046, "Swamp Grass")
            .put(10049, "Jungle Grass")
            .put(10056, "Desert Rock")
            .put(10060, "Lush Grass")
            .put(10064, "Fir Tree")
            .put(10068, "Willow Tree")
            .put(10072, "Flowery Grass")
            .put(10073, "Joshua Tree")
            .put(10076, "Vine Tree")
            .put(10078, "Palm Tree")
            .put(10088, "Pink Tree")
            .put(11001, "Chicken")
            .put(11002, "Sheep")
            .put(11003, "Trout")
            .put(11005, "Guinea Pig")
            .put(11019, "Lizard")
            .put(11020, "Fox")
            .put(11024, "Jellyfish")
            .put(11028, "Butterfly")
            .put(11038, "Bear")
            .build();

    public static final Map<String, Integer> SpeciesNameToShopItemID = ImmutableMap.<String, Integer>builder()
            .put("Oak Tree", 2)
            .put("Apple Tree", 32)
            .put("Sycamore Tree", 69)
            .put("Nut Tree", 137)
            .put("Elm Tree", 112)
            .put("Ash Tree", 150)
            .put("Tomato Plant", 62)
            .put("Berry Bush", 3)
            .put("Blueberry Bush", 71)
            .put("Holly Bush", 133)
            .put("Tall Tree", 28)
            .put("Juniper Tree", 4)
            .put("Cedar Tree", 81)
            .put("Acer Tree", 5)
            .put("Large Tree", 54)
            .put("Rosemary", 108)
            .put("Heather", 7)
            .put("Barley", 148)
            .put("Desert Grass", 180)
            .put("Fern", 10)
            .put("Sage", 106)
            .put("Potato Plant", 51)
            .put("Grass Tuft", 20)
            .put("Wheat", 11)
            .put("Wild Mint", 105)
            .put("Carrot", 45)
            .put("Oregano", 107)
            .put("Seaweed", 17)
            .put("Kelp", 13)
            .put("Tropical Seaweed", 136)
            .put("Coral", 165)
            .put("Water Lily", 16)
            .put("Yucca", 21)
            .put("Small Cactus", 120)
            .put("Medium Cactus", 18)
            .put("Giant Cactus", 121)
            .put("Prickly Pear", 19)
            .put("Birch Tree", 25)
            .put("Red Maple Tree", 135)
            .put("Eucalyptus Tree", 66)
            .put("Wobbly Tree", 113)
            .put("Autumnal Tree", 156)
            .put("Pagoda Tree", 155)
            .put("Pink Tree", 26)
            .put("Red Tree", 42)
            .put("Cherry Tree", 30)
            .put("Spiral Tree", 67)
            .put("Palm Tree", 27)
            .put("Banana Tree", 43)
            .put("Flower Tree", 109)
            .put("Orange Tree", 47)
            .put("Mango Tree", 94)
            .put("Button Mushroom", 119)
            .put("Red Mushroom", 59)
            .put("Tall Mushroom", 31)
            .put("Jungle Mushroom", 39)
            .put("Tropical Mushroom", 126)
            .put("Jungle Grass", 123)
            .put("Jungle Plant", 36)
            .put("Jungle Flower", 111)
            .put("Vine Tree", 37)
            .put("Ficus Tree", 124)
            .put("Canopy Tree", 125)
            .put("Witchwood Tree", 159)
            .put("Starbloom Bush", 173)
            .put("Joshua Tree", 122)
            .put("Umbrella Tree", 46)
            .put("Willow Tree", 110)
            .put("Slimy Tree", 58)
            .put("Dead Tree", 146)
            .put("Swamp Grass", 60)
            .put("Turnip", 142)
            .put("Bulrush", 143)
            .put("Swamp Flower", 144)
            .put("Fly Trapper", 176)
            .put("Lush Grass", 149)
            .put("Primrose", 127)
            .put("Bamboo", 70)
            .put("Daisy", 114)
            .put("Tulip", 80)
            .put("Bluebell", 118)
            .put("Rose", 151)
            .put("Buttercup", 115)
            .put("Snap Dragon", 103)
            .put("Poppy", 116)
            .put("Sunflower", 153)
            .put("Pansies", 154)
            .put("Lily", 152)
            .put("Marigolds", 182)
            .put("Fir Tree", 132)
            .put("Spruce Tree", 83)
            .put("Pine Tree", 131)
            .put("Flowery Grass", 128)
            .put("Tropical Flower", 117)
            .put("Bromeliad", 129)
            .put("Healbloom", 157)
            .put("Leafy Plant", 130)
            .put("Brown Rock", 6)
            .put("Brown Stones", 9)
            .put("Jungle Rocks", 35)
            .put("Snow Rocks", 134)
            .put("Stones", 162)
            .put("Rock", 163)
            .put("Shell", 166)
            .put("Desert Rock", 181)
            .put("Sheep", 1)
            .put("Wild Boar", 50)
            .put("Warthog", 79)
            .put("Deer", 99)
            .put("Camel", 85)
            .put("Goat", 92)
            .put("Chicken", 8)
            .put("Sparrow", 64)
            .put("Toucan", 63)
            .put("Dove", 161)
            .put("Eagle", 171)
            .put("Duck", 65)
            .put("Peacock", 145)
            .put("Lizard", 75)
            .put("Turtle", 12)
            .put("Frog", 38)
            .put("Toad", 74)
            .put("Trout", 14)
            .put("Redfish", 15)
            .put("Clown Fish", 76)
            .put("Angel Fish", 168)
            .put("Neon Fish", 169)
            .put("Salmon", 167)
            .put("Pike", 23)
            .put("Dolphin", 183)
            .put("Royal Gramma", 170)
            .put("Guinea Pig", 52)
            .put("Rabbit", 41)
            .put("Desert Hare", 73)
            .put("Meerkat", 178)
            .put("Beaver", 89)
            .put("Squirrel", 49)
            .put("Butterfly", 55)
            .put("Bee", 56)
            .put("Fly", 175)
            .put("Fox", 84)
            .put("Wolf", 72)
            .put("Bear", 78)
            .put("Jellyfish", 172)
            .build();

//endregion

    public ItemManager(int index, int receivedDpPerMinBonuses){
        this.index = index;
        this.receivedDpPerMinBonuses = receivedDpPerMinBonuses;
    }

    public int getIndex(){
        return this.index;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public int getReceivedDpPerMinBonuses(){
        return this.receivedDpPerMinBonuses;
    }

    public boolean processReceivedItem(long itemId, int index, String sendingPlayer){
        if(index > this.index + 1){
            APAgent.log("Somehow got a skipped item? Current index: " + this.index + ", received index: " + index);
            //skipped item?
        }
        //Handle the received item:

        // I'm using the first digit of the item id to classify items. Not sure if this is the most elegant
        // way to do it, but it works. I can use the second digit to further differentiate them, if needed
        int firstDigit = Character.getNumericValue(Long.toString(itemId).charAt(0));

        switch(firstDigit){
            case 1: // Species Unlocks

                // I don't think the game saves which shop items are unlocked properly, so we allow these items
                // to go through on connection even if they have been received before (no harm in doing so)
                String species = ItemIDtoSpeciesName.get((int) itemId);
                int shopIdOfSpecies = SpeciesNameToShopItemID.get(species);
                APBridge.unlockShopItem(shopIdOfSpecies);

                // Only send the in game notification if this is actually a new item, though
                if(index > this.index) APBridge.sendNotification(
                        species + " Unlocked!",
                        "Received " + species + " Permit from " + sendingPlayer);
                break;
            case 7: // DP/Min Bonus Items
                if (index <= this.index) return false; // Don't give these again on startup
                this.receivedDpPerMinBonuses++;
                APBridge.sendNotification("Received Bonus DP/Min!", "Received Progressive DP/Min bonus from " + sendingPlayer + ". Total bonus: +"+this.receivedDpPerMinBonuses*20+" dp/min");
                break;
            case 8: // Money (dp) items
                if (index <= this.index) return false; // Don't give money again on startup
                int amount = (int) (itemId - 80000) * 100; // eg, id 80750 <=> 75,000 dp
                APBridge.giveMoney(amount);
                APBridge.sendNotification("Received " + amount + " dp!", "Received " + amount + " dp from " + sendingPlayer);
                break;
            case 9: // Filler
                APAgent.log("Received blank filler item; this shouldn't happen");
                break;
            default: // This should never happen
                APAgent.log("Unknown item id: " + itemId);
                break;
        }

        if(index <= this.index) return false;
        this.index++;
        return true;
    }
}
