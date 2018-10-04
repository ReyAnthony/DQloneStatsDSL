package fr.anthonyrey.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StatsData {

    private String forLevel;
    private String neededXp;

    private String hp;
    private String agi;
    private String atk;
    private String def;
    private String mag;

    public boolean isValid () {

        return !(neededXp == null || forLevel == null || hp == null || agi == null
                || atk == null || def == null || mag == null);
    }
}
