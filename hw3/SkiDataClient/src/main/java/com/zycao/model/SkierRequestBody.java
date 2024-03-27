package com.zycao.model;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SkierRequestBody {
    private Integer time;
    private Integer liftID;
}