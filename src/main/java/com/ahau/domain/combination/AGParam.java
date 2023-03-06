package com.ahau.domain.combination;

import com.ahau.domain.assemble.DraftParam;
import com.ahau.domain.gapFill.GapParam;
import lombok.Data;

@Data
public class AGParam {
    private DraftParam draftParam;
    private GapParam gapParam;
    private String onlineGenome;
}
