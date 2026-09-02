package aaces.aploader.client;

import com.google.common.collect.ImmutableMap;

import java.util.*;

public class LocationManager {
    private final List<Integer> queuedLocs;
    private final Set<Integer> checkedLocs;

    public static final Map<String, Integer> TaskNameToLocID = ImmutableMap.of(
            "Watching Grass Grow", 1
    );

    public static final Map<String, Integer> SpeciesEvolutionToLocID = ImmutableMap.of(
            "Wheat", 1
    );

    public LocationManager(){
        this.queuedLocs = new ArrayList<>();
        this.checkedLocs = new HashSet<>();
    }

    public int numQueuedLocations(){
        return this.queuedLocs.size();
    }

    public boolean nonemptyQueue(){
        return !this.queuedLocs.isEmpty();
    }

    public void queueLocation(int id){
        this.queuedLocs.add(id);
    }

    public int popOldestLocation(){
        return queuedLocs.remove(0);
    }

    public boolean locAlreadyChecked(int id){
        return this.checkedLocs.contains(id);
    }

    public void markChecked(int id){
        this.checkedLocs.add(id);
    }

    public void setCheckedLocs(Set<Long> checkedLocs){
        this.checkedLocs.clear();
        for (long loc : checkedLocs){
            this.checkedLocs.add((int) loc);
        }
    }

}
