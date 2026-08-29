package aaces.aploader.client;

import aaces.aploader.APAgent;
import io.github.archipelagomw.Client;
import io.github.archipelagomw.flags.ItemsHandling;
import tasks.Task;

import java.net.URISyntaxException;

public class APClient extends Client {
    public static boolean Connected = false;

    private static APClient client;

    public SlotData slotData;

    public APClient(String slot, String password, ItemManager itemManager){
        super();
        this.setGame("Equilinox");
        this.setItemsHandlingFlags(ItemsHandling.SEND_ITEMS | ItemsHandling.SEND_OWN_ITEMS | ItemsHandling.SEND_STARTING_INVENTORY);

        this.getEventManager().registerListener(new ConnectResult(this));

        this.setName(slot);
        this.setPassword(password);
    }

    public static void tryConnect(String host, String slot, String password, ItemManager itemManager){
        if (Connected) return;
        APAgent.log("Trying to connect with host: "+host+", slot: "+slot+", password: "+password);
        client = new APClient(slot, password, itemManager);
        try {
            client.connect(host);
        } catch (URISyntaxException e) {
            APAgent.log("Couldn't connect!");
            e.printStackTrace();
        }
    }

    public SlotData getSlotData() {
        return slotData;
    }

    @Override
    public void onError(Exception ex) {
        String error = String.format("Connection error: %s", ex.getLocalizedMessage());
        APAgent.log(error);
    }

    @Override
    public void onClose(String reason, int attemptingReconnect) {
        if (attemptingReconnect > 0) {
            APAgent.log(String.format("%s \n... reconnecting in %ds", reason, attemptingReconnect));
        } else {
            APAgent.log(reason);
        }
    }

    public static void tryDisconnect(){
        if (!Connected) return;
        APAgent.log("trying to disconnect");
        client.close();
        Connected = false;
    }

    public static void trySendTaskCheck(Task task){

    }
}
