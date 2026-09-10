package aaces.aploader.client;

public class SlotData {

    public int tasks_reward_dp;
    public int starting_dp;
    public int itempool_dp;

    public boolean tasksRewardDP(){
        return tasks_reward_dp == 1;
    }
}
