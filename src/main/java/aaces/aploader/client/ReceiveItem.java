package aaces.aploader.client;

import io.github.archipelagomw.events.ArchipelagoEventListener;
import io.github.archipelagomw.events.ReceiveItemEvent;
import io.github.archipelagomw.parts.NetworkItem;

public class ReceiveItem {

    private final ItemManager itemManager;

    public ReceiveItem(ItemManager itemManager){
        this.itemManager = itemManager;
    }

    @ArchipelagoEventListener
    public void onReceiveItem(ReceiveItemEvent event){
        NetworkItem item = event.getItem();
        boolean newItem = this.itemManager.processReceivedItem(item.itemID, (int) event.getIndex());
    }

}
