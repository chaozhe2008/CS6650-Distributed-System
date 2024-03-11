package com.zycao.entity;


import lombok.Data;
import jakarta.persistence.*;


@Data
@Entity
public class LiftRide {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer time;
}
