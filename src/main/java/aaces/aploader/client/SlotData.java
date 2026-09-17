package aaces.aploader.client;

public class SlotData {

    public int tasks_reward_dp;
    public int starting_dp;
    public int itempool_dp;

    public int free_evolution;

    public int size_checks;

    public boolean tasksRewardDP(){
        return tasks_reward_dp == 1;
    }

    public boolean includeSizeChecks() {
        return size_checks == 1;
    }

    public boolean freeEvolution() {
        return free_evolution == 1;
    }
}
