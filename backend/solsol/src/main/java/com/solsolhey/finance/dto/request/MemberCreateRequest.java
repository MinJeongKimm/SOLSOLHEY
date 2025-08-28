package com.solsolhey.finance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateRequest {
    private String apiKey;
    private String userId; // 금융 API용 이메일
}

