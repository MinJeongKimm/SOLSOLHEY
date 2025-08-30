package com.solsolhey.ai.context;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserContext {
    public String user_id;
    public List<String> role_tags;
    public String major_group;
    public Stats stats;
    public Timetable timetable;
    public Library library;
    public String notes;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stats {
        public Integer likes;
        public Integer friends;
        public Integer items;
        public Double join_rate;
        public Double success_rate;
        public Integer points;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Timetable {
        public Boolean morning;
        public Boolean afternoon;
        public Boolean back_to_back;
        public Boolean long_free_day;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Library {
        public Integer streak_days;
    }
}

