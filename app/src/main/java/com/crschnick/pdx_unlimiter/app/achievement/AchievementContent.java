package com.crschnick.pdx_unlimiter.app.achievement;

import com.crschnick.pdx_unlimiter.app.installation.GameInstallation;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.*;

public class AchievementContent {

    public static AchievementContent EU4;

    static {
        List<AchievementVariable> eu4Variables = new ArrayList<>();
        eu4Variables.add(new AchievementVariable.PathVariable("player", "meta", "$.player", false));

        eu4Variables.add(new AchievementVariable.FilterCountVariable(
                "custom_nation_count", "countries", "$[*][?(@.is_custom == true)]"));

        eu4Variables.add(new AchievementVariable.FilterCountVariable(
                "nation_count", "countries", "$"));
        //eu4Variables.add(new Variable.PathVariable("tags", "countries", "$..tag"));

        eu4Variables.add(new AchievementVariable.PathVariable(
                "vassals", "diplomacy",
                "$['dependencies'][?(@.subject_type == 'vassal' && @.first == ${player})].second", true));

        eu4Variables.add(new AchievementVariable.PathVariable(
                "marches", "diplomacy",
                "$['dependencies'][?(@.subject_type == 'march' && @.first == ${player})].second", true));

        eu4Variables.add(new AchievementVariable.PathVariable(
                "personal_union", "diplomacy",
                "$['dependencies'][?(@.subject_type == 'personal_union' && @.first == ${player})].second", true));

        eu4Variables.add(new AchievementVariable.PathVariable(
                "tributaries", "diplomacy",
                "$['dependencies'][?(@.subject_type == 'tributary_state' && @.first == ${player})].second", true));

        eu4Variables.add(new AchievementVariable.ValueVariable("eu4.installDir", GameInstallation.EU4.getPath().toString()));


        Map<String,AchievementCondition> eu4Conditions = new HashMap<>();
        eu4Conditions.put("no_custom_nation", new AchievementCondition(
                "No custom nation", "countries", "[?(${custom_nation_count} == 0)]"));

        eu4Conditions.put("normal_start_date", new AchievementCondition(
                "Normal start date", "gamestate", "[?($.start_date.days_since_beginning == 2352374)]"));

        eu4Conditions.put("normal_province_values", new AchievementCondition(
                "Normal province values", "gamestate", "[?($.gameplaysettings.setgameplayoptions.province_values == 'normal')]"));

        eu4Conditions.put("historical_lucky_nations", new AchievementCondition(
                "Historical lucky nations", "gamestate", "[?($.gameplaysettings.setgameplayoptions.lucky_nations == 'historical')]"));

        eu4Conditions.put("ironman", new AchievementCondition(
                "Ironman", "meta", "[?($.ironman == true)]"));

        eu4Conditions.put("normal_difficulty", new AchievementCondition(
                "Normal difficulty", "gamestate", "[?($.gameplaysettings.setgameplayoptions.difficulty == 'normal')]"));


        Map<String,AchievementScorer> eu4Scorers = new HashMap<>();
        eu4Scorers.put("years_since_start", new AchievementScorer.ChainedScorer("divide",
                List.of(new AchievementScorer.ChainedScorer("subtract", List.of(
                        new AchievementScorer.PathValueScorer("meta", "$.date.days_since_beginning", Optional.empty()),
                        new AchievementScorer.PathValueScorer("gamestate", "$.start_date.days_since_beginning", Optional.empty())),
                        Optional.empty()), new AchievementScorer.ValueScorer(365.0)), Optional.of("Years since start date")));

        EU4 = new AchievementContent(eu4Variables, eu4Conditions, eu4Scorers);
    }


    private List<AchievementVariable> variables;

    private Map<String, AchievementCondition> conditions;

    private Map<String, AchievementScorer> scorers;

    public AchievementContent(List<AchievementVariable> variables, Map<String, AchievementCondition> conditions, Map<String, AchievementScorer> scorers) {
        this.variables = variables;
        this.conditions = conditions;
        this.scorers = scorers;
    }

    public List<AchievementVariable> getVariables() {
        return variables;
    }

    public Map<String, AchievementCondition> getConditions() {
        return conditions;
    }

    public Map<String, AchievementScorer> getScorers() {
        return scorers;
    }
}
