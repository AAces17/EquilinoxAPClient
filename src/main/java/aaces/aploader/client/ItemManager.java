package aaces.aploader.client;

public class ItemManager {
    private int index = 0;


    public ItemManager(int index){
        this.index = index;
    }

    public int getIndex(){
        return this.index;
    }

    public void setIndex(int index){
        this.index = index;
    }
}
