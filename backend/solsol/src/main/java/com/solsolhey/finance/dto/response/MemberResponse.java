package com.solsolhey.finance.dto.response;

import lombok.Data;

@Data
public class MemberResponse {
    private String userId;
    private String userName;
    private String institutionCode;
    private String userKey;
    private String created;
    private String modified;
}

