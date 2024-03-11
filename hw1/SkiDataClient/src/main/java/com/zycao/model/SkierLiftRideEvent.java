package com.zycao.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class SkierLiftRideEvent {
    private Integer skierID;
    private Integer resortID;
    private Integer liftID;
    private Integer seasonID;
    private Integer dayID;
    private Integer time;
}
