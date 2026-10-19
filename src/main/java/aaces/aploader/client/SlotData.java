package aaces.aploader.client;

import aaces.aploader.APAgent;
import aaces.aploader.APBridge;
import org.lwjgl.opengl.APPLEFlushBufferRange;
import utils.BinaryReader;
import utils.BinaryWriter;

public class SlotData {

    public int tasks_reward_dp;
    public int starting_dp;
    public int itempool_dp;

    public int goal_selection;

    public int free_evolution;

    public int size_checks;

    public static boolean tasksRewardDP(){
        return tasksRewardDP;
    }

    public static boolean includeSizeChecks() {
        return sizeChecks;
    }

    public static boolean freeEvolution() {
        return freeEvolution;
    }

    public static GoalType getGoalCondition() {
        switch(goalCondition){
            case 0:
                return GoalType.COMPLETIONIST;
            case 1:
                return GoalType.CAMEL;
            default:
                return GoalType.NONE;
        }
    }


    public static boolean tasksRewardDP, freeEvolution, sizeChecks;

    public static int startingDP, itempoolDP, goalCondition;

    public static void saveSlotData(BinaryWriter writer){
        try{
            writer.writeBoolean(tasksRewardDP);
            writer.writeBoolean(freeEvolution);
            writer.writeBoolean(sizeChecks);

            writer.writeInt(startingDP);
            writer.writeInt(itempoolDP);
            writer.writeInt(goalCondition);
        } catch (Exception e) {
            APAgent.log("Error saving slot data: ");
            APAgent.log("\t" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadSlotDataFromSave(BinaryReader reader){
        try{
            tasksRewardDP = reader.readBoolean();
            freeEvolution = reader.readBoolean();
            sizeChecks = reader.readBoolean();

            startingDP = reader.readInt();
            itempoolDP = reader.readInt();
            goalCondition = reader.readInt();
        } catch (Exception e) {
            APAgent.log("Error loading slot data: ");
            APAgent.log("\t" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadDefaults(){
        tasksRewardDP = false;
        freeEvolution = true;
        sizeChecks = false; // needs to be false to avoid trying to send a check not configured

        startingDP = 5000;
        itempoolDP = 150000;
        goalCondition = 0; // might eventually change to not accidentally miss a goal
    }

    public static void setCachedOnConnect(APClient client){
        tasksRewardDP = client.slotData.tasks_reward_dp == 1;
        freeEvolution = client.slotData.free_evolution == 1;
        sizeChecks = client.slotData.size_checks == 1;

        startingDP = client.slotData.starting_dp;
        itempoolDP = client.slotData.itempool_dp;
        goalCondition = client.slotData.goal_selection;
    }


}


