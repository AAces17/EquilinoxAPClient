package aaces.aploader.client;

import aaces.aploader.APAgent;
import aaces.aploader.APBridge;
import io.github.archipelagomw.events.ArchipelagoEventListener;
import io.github.archipelagomw.events.ConnectionResultEvent;
import io.github.archipelagomw.network.ConnectionResult;

public class ConnectResult {

    private final APClient client;

    public ConnectResult(APClient APClient){
        this.client = APClient;
    }
    @ArchipelagoEventListener
    public void onConnectResult(ConnectionResultEvent event) {
        if (event.getResult() == ConnectionResult.Success) {
            APAgent.log("Successfully connected to " + client.getConnectedAddress());
            client.slotData = event.getSlotData(SlotData.class);

            APBridge.setCheckedLocs(client.getLocationManager().getCheckedLocations());

            APBridge.tryFlushLocationQueue(false);

            APBridge.sendNotification("[AP] Connected!", "Successfully connected to the Archipelago server.");
        } else if (event.getResult() == ConnectionResult.InvalidPassword) {
            APAgent.log("Connection Failed: Invalid Password.");

            APBridge.sendNotification("[AP] Failed to Connect!",
                    "Unable to connect to server (invalid password). Try generating a new world.");
        } else if (event.getResult() == ConnectionResult.IncompatibleVersion) {
            APAgent.log("Connection Failed: Server Sent Incompatible Version Error.");

            APBridge.sendNotification("[AP] Failed to Connect!",
                    "Unable to connect to server (incompatible version). You are currently running version "+APBridge.getModVersion()+".");
        } else if (event.getResult() == ConnectionResult.InvalidSlot) {
            APAgent.log("Connection Failed: Invalid Slot Name. (this is case sensitive)");

            APBridge.sendNotification("[AP] Failed to Connect!",
                    "Unable to connect to server (invalid slot). Try generating a new world.");
        } else if (event.getResult() == ConnectionResult.SlotAlreadyTaken) {
            APAgent.log("Connection Failed: Room Slot has already been taken.");

            APBridge.sendNotification("[AP] Failed to Connect!",
                    "Unable to connect to server (slot already taken). Try generating a new world.");
        }
    }
}
