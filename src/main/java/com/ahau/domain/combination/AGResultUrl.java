package com.ahau.domain.combination;

import com.ahau.domain.ProcessWarning;
import com.ahau.domain.assemble.DraftResultUrl;
import com.ahau.domain.gapFill.GapResultUrl;
import lombok.Data;

import java.util.Vector;


@Data
public class AGResultUrl {
    private DraftResultUrl draftResultUrl;
    private GapResultUrl gapResultUrl;
    private Vector<ProcessWarning> warnings;
}
