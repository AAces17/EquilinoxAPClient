package aaces.aploader.client;

import com.google.common.collect.ImmutableMap;

import java.util.*;

public class LocationManager {
    private final List<Integer> queuedLocs;
    private final Set<Integer> checkedLocs;

//region Location ID Maps

    public static final Map<String, Integer> TaskNameToLocID = ImmutableMap.<String, Integer>builder()
            .put("Watching Grass Grow", 10001)
            .put("Flower Power", 10002)
            .put("My First Tree", 10003)
            .put("More Grass", 10004)
            .put("Sheepish", 10005)
            .put("Wheat Breeder", 10006)
            .put("Genetic Modification", 10007)
            .put("Chicken Farm", 10008)
            .put("This is Boar-ing", 10009)
            .put("Under the Sea", 10010)
            .put("Gone Fishing", 10011)
            .put("Diversify", 10012)
            .put("An Apple a Day", 10013)
            .put("An Apple Every Day", 10014)
            .put("Completionist", 10015)
            .put("A Bird in the Hand", 10016)
            .put("Totally Quackers", 10017)
            .put("Grassy Plains", 10018)
            .put("Merry Berry", 10019)
            .put("Deserted", 10020)
            .put("Woodland Biome", 10021)
            .put("Rabbit Food", 10022)
            .put("24 Carrot Gold", 10023)
            .put("Forest Flora and Fauna", 10024)
            .put("Mountaineering", 10025)
            .put("Totally Nuts!", 10026)
            .put("Oh Deer", 10027)
            .put("The Tropics", 10028)
            .put("Totally Coconuts!", 10029)
            .put("Tropical Waters", 10030)
            .put("Control a Sheep", 10031)
            .put("Swamped", 10032)
            .put("Potato Potahto", 10033)
            .put("In the Jungle", 10034)
            .put("The Mighty Jungle", 10035)
            .put("Catching Butterflies", 10036)
            .put("A Turnip for the Books", 10037)
            .put("Beavering Away", 10038)
            .put("Flutter By", 10039)
            .put("Honey, I'm Home!", 10040)
            .put("Hungry Turtle", 10041)
            .put("Mushroom Mastery", 10042)
            .put("Lush!", 10043)
            .put("On your Pike", 10044)
            .put("Logger Rhythm", 10045)
            .put("Hungry Fish", 10046)
            .put("Butterfly Hunter", 10047)
            .put("Banana Farmer", 10048)
            .put("Honey Harvester", 10049)
            .put("Digging a Hole", 10050)
            .put("Fox Hunting", 10051)
            .put("Wolf in Sheep's Clothing", 10052)
            .put("Cashing In", 10053)
            .put("Potato Party", 10054)
            .put("Spitting Image", 10055)
            .put("Fish Mastery", 10056)
            .put("Blooming Gorgeous", 10057)
            .put("It's a Trap!", 10058)
            .put("Desert Eagle", 10059)
            .put("Hole in One", 10060)
            .build();

    public static final Map<String, Integer> SpeciesEvolutionToLocID = ImmutableMap.<String, Integer>builder()
            .put("Wheat", 20005)
            .put("Buttercup", 20006)
            .put("Wild Mint", 20008)
            .put("Tulip", 20009)
            .put("Oregano", 20012)
            .put("Kelp", 20014)
            .put("Water Lily", 20018)
            .put("Sage", 20020)
            .put("Heather", 20021)
            .put("Elm Tree", 20022)
            .put("Red Maple Tree", 20024)
            .put("Sycamore Tree", 20025)
            .put("Juniper Tree", 20026)
            .put("Apple Tree", 20027)
            .put("Fern", 20028)
            .put("Pansies", 20030)
            .put("Bluebell", 20031)
            .put("Tropical Mushroom", 20034)
            .put("Cedar Tree", 20035)
            .put("Barley", 20036)
            .put("Tropical Seaweed", 20037)
            .put("Wobbly Tree", 20038)
            .put("Poppy", 20039)
            .put("Spruce Tree", 20040)
            .put("Lily", 20041)
            .put("Snap Dragon", 20043)
            .put("Small Cactus", 20044)
            .put("Jungle Mushroom", 20045)
            .put("Desert Grass", 20047)
            .put("Carrot", 20048)
            .put("Jungle Flower", 20050)
            .put("Potato Plant", 20051)
            .put("Prickly Pear", 20052)
            .put("Red Mushroom", 20053)
            .put("Rose", 20054)
            .put("Coral", 20055)
            .put("Marigolds", 20057)
            .put("Turnip", 20058)
            .put("Berry Bush", 20059)
            .put("Tropical Flower", 20061)
            .put("Jungle Plant", 20062)
            .put("Sunflower", 20063)
            .put("Holly Bush", 20065)
            .put("Leafy Plant", 20066)
            .put("Tall Mushroom", 20067)
            .put("Medium Cactus", 20069)
            .put("Nut Tree", 20070)
            .put("Acer Tree", 20071)
            .put("Ash Tree", 20074)
            .put("Primrose", 20075)
            .put("Bromeliad", 20077)
            .put("Blueberry Bush", 20079)
            .put("Bulrush", 20080)
            .put("Slimy Tree", 20081)
            .put("Red Tree", 20082)
            .put("Swamp Flower", 20083)
            .put("Ficus Tree", 20084)
            .put("Pagoda Tree", 20085)
            .put("Bamboo", 20086)
            .put("Flower Tree", 20087)
            .put("Healbloom", 20089)
            .put("Banana Tree", 20090)
            .put("Eucalyptus Tree", 20091)
            .put("Umbrella Tree", 20092)
            .put("Canopy Tree", 20093)
            .put("Fly Trapper", 20094)
            .put("Orange Tree", 20095)
            .put("Autumnal Tree", 20096)
            .put("Large Tree", 20097)
            .put("Mango Tree", 20098)
            .put("Giant Cactus", 20099)
            .put("Pine Tree", 20100)
            .put("Witchwood Tree", 20101)
            .put("Starbloom Bush", 20102)
            .put("Dead Tree", 20103)
            .put("Spiral Tree", 20104)
            .put("Cherry Tree", 20105)
            .put("Redfish", 21004)
            .put("Wild Boar", 21006)
            .put("Duck", 21007)
            .put("Salmon", 21008)
            .put("Rabbit", 21009)
            .put("Sparrow", 21010)
            .put("Squirrel", 21011)
            .put("Clown Fish", 21012)
            .put("Deer", 21013)
            .put("Angel Fish", 21014)
            .put("Goat", 21015)
            .put("Desert Hare", 21016)
            .put("Royal Gramma", 21017)
            .put("Warthog", 21018)
            .put("Neon Fish", 21021)
            .put("Frog", 21022)
            .put("Pike", 21023)
            .put("Eagle", 21025)
            .put("Turtle", 21026)
            .put("Fly", 21027)
            .put("Toad", 21029)
            .put("Meerkat", 21030)
            .put("Toucan", 21031)
            .put("Dove", 21032)
            .put("Bee", 21033)
            .put("Wolf", 21034)
            .put("Dolphin", 21035)
            .put("Peacock", 21036)
            .put("Beaver", 21037)
            .put("Camel", 21039)
            .build();

//endregion

    public LocationManager(){
        this.queuedLocs = new ArrayList<>();
        this.checkedLocs = new HashSet<>();
    }

    public int numQueuedLocations(){
        return this.queuedLocs.size();
    }

    public boolean nonemptyQueue(){
        return !this.queuedLocs.isEmpty();
    }

    public void queueLocation(int id){
        this.queuedLocs.add(id);
    }

    public int popOldestLocation(){
        return queuedLocs.remove(0);
    }

    public boolean locAlreadyChecked(int id){
        return this.checkedLocs.contains(id);
    }

    public void markChecked(int id){
        this.checkedLocs.add(id);
    }

    public void setCheckedLocs(Set<Long> checkedLocs){
        this.checkedLocs.clear();
        for (long loc : checkedLocs){
            this.checkedLocs.add((int) loc);
        }
    }

}
