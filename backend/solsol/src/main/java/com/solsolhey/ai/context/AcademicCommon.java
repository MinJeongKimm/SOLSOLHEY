package com.solsolhey.ai.context;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AcademicCommon {
    public String today; // YYYY-MM-DD
    public String timezone; // IANA TZ e.g., Asia/Seoul
    public Period exam_period;
    public String semester_end;
    public String vacation_start;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Period {
        public String start; // YYYY-MM-DD
        public String end;   // YYYY-MM-DD
    }
}

