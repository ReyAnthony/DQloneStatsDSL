package fr.anthonyrey.stats;

import java.util.ArrayList;
import java.util.List;

public class ClassData {

    private List<StatsData> statsData;

    public ClassData() {
        statsData = new ArrayList<>();
    }

    public void addStatsData(StatsData data) {
        statsData.add(data);
    }

    public List<StatsData> getStatsData() {
        return statsData;
    }

    public boolean isValid() {

        StatsData previous = StatsData.builder().forLevel("0").neededXp("-1").build();

        for (StatsData statsDatum : statsData) {

            final Integer level = Integer.valueOf(statsDatum.getForLevel());
            final Integer previousLevel = Integer.valueOf(previous.getForLevel());

            final Integer xp = Integer.valueOf(statsDatum.getNeededXp());
            final Integer previousXP = Integer.valueOf(previous.getNeededXp());

            if(!(level == (previousLevel + 1)) || !(xp > previousXP)) {
                return false;
            }

            previous = statsDatum;
        }

        return true;
    }
}
