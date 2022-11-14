package com.ahau.domain;

import lombok.Data;

@Data
public class BlastParam {
    private Integer minLength;
    private Integer minIdentity;
    private String prefix;
    private Integer threads;
    private String aligner;
    private Boolean plot;
    private Boolean overwrite;
    private String miniMapOption;
    private String nucmerOption;
    private String deltaFilterOption;
}
