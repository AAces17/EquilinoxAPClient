package aaces.aploader.client;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ItemManager {
    private int index = 0;

    public static final Map<Integer, Integer> ItemIDtoShopID = ImmutableMap.of(
            1, 1
    );

    public ItemManager(int index){
        this.index = index;
    }

    public int getIndex(){
        return this.index;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public boolean processReceivedItem(long itemId, int index){
        if(index <= this.index) return false;
        if(index > this.index + 1){
            //skipped item?
        }
        //Handle the received item:
        this.index++;
        return true;
    }
}
