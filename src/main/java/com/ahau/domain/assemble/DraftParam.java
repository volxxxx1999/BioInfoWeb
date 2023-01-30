package com.ahau.domain.assemble;

import lombok.Data;

@Data
public class DraftParam {
    private Integer minAlign;
    private Integer minContig;
    private Integer minIdentity;
    private String prefix;
    private String aligner;
    private Boolean plot;
}
