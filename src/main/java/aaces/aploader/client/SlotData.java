package aaces.aploader.client;

import com.google.gson.annotations.SerializedName;

public class SlotData {

    public int money_sanity;

    public boolean getMoney_sanity(){
        return money_sanity == 1;
    }
}
