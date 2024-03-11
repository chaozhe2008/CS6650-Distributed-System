package com.zycao.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkiDataPayload {
    private Integer resortID;
    private Integer seasonID;
    private Integer dayID;
    private Integer skierID;
    private Integer time;
    private Integer liftID;
}
