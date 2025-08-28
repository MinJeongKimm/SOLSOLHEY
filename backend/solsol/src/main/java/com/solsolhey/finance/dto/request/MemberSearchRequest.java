package com.solsolhey.finance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberSearchRequest {
    private String userId; // 금융 API용 이메일
    private String apiKey;
}

