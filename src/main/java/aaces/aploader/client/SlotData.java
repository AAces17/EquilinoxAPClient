package aaces.aploader.client;

import aaces.aploader.APAgent;
import aaces.aploader.APBridge;
import utils.BinaryReader;
import utils.BinaryWriter;

import java.util.List;
import java.util.stream.Collectors;

public class SlotData {

    public String ap_world_version;

    public int tasks_reward_dp;
    public int starting_dp;
    public int itempool_dp;

    public int goal_selection;
    public String[] evo_goal_species;
    public int evo_goal_count;

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
                return GoalType.EVOLUTION;
            default:
                return GoalType.NONE;
        }
    }

    public static boolean evolvedEnoughSpeciesToGoal(List<String> evolvedSpecies){
        return numAlreadyEvolvedSpeciesNeededForGoal(evolvedSpecies) >= numNeededSpeciesForEvoGoal();
    }

    public static int numAlreadyEvolvedSpeciesNeededForGoal(List<String> evolvedSpecies){
        int numberEvolvedFromReqs = 0;

        List<String> lowerCaseList = evolvedSpecies.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        for(String species : evoGoalSpecies){
            if (lowerCaseList.contains(species.toLowerCase())) {
                numberEvolvedFromReqs++;
            }
        }
        return numberEvolvedFromReqs;
    }

    public static int numNeededSpeciesForEvoGoal(){
        if(evoGoalCount == 0 || evoGoalCount > evoGoalSpecies.length){
            return evoGoalSpecies.length;
        } else {
            return evoGoalCount;
        }
    }

    public static boolean tasksRewardDP, freeEvolution, sizeChecks;

    public static int startingDP, itempoolDP, goalCondition, dpPerMinBonusCount, evoGoalCount;

    public static String[] evoGoalSpecies;

    public static void saveSlotData(BinaryWriter writer){
        try{
            writer.writeBoolean(tasksRewardDP);
            writer.writeBoolean(freeEvolution);
            writer.writeBoolean(sizeChecks);

            writer.writeInt(startingDP);
            writer.writeInt(itempoolDP);
            writer.writeInt(goalCondition);

            if (goalCondition == 1){
                writer.writeInt(evoGoalCount);
                writer.writeInt(evoGoalSpecies.length);
                for(String species : evoGoalSpecies){
                    writer.writeString(species);
                }
            }
        } catch (Exception e) {
            APAgent.log("Error saving slot data: ");
            APAgent.log("\t" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadSlotDataFromSave(BinaryReader reader, long versionLastSavedOn){
        try{
            tasksRewardDP = reader.readBoolean();
            freeEvolution = reader.readBoolean();
            sizeChecks = reader.readBoolean();

            startingDP = reader.readInt();
            itempoolDP = reader.readInt();
            goalCondition = reader.readInt();

            if(goalCondition == 1){
                if(versionLastSavedOn >= 200) { // 0.2.0
                    evoGoalCount = reader.readInt();
                    int goalSpeciesLength = reader.readInt();
                    evoGoalSpecies = new String[goalSpeciesLength];
                    for (int i = 0; i < goalSpeciesLength; i++){
                        evoGoalSpecies[i] = reader.readString();
                    }
                } else {
                    evoGoalCount = 1;
                    evoGoalSpecies = new String[]{"Camel"};
                }
            }
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

        evoGoalCount = 1;
        evoGoalSpecies = new String[]{"Camel"};
    }

    public static long setCachedOnConnect(APClient client){
        tasksRewardDP = client.slotData.tasks_reward_dp == 1;
        freeEvolution = client.slotData.free_evolution == 1;
        sizeChecks = client.slotData.size_checks == 1;

        startingDP = client.slotData.starting_dp;
        itempoolDP = client.slotData.itempool_dp;
        goalCondition = client.slotData.goal_selection;

        String apworldVersionString = client.slotData.ap_world_version;

        long apworldVersion = APBridge.modVersionToLong(apworldVersionString);

        APAgent.log("Connected to world generated on version " + apworldVersionString + " ("+apworldVersion+")");

        if (apworldVersion >= 200) {
            evoGoalCount = client.slotData.evo_goal_count;

            APAgent.log("Evo goal count: " + evoGoalCount);

            evoGoalSpecies = client.slotData.evo_goal_species;
        }

        return apworldVersion;
    }


}


