package com.zycao.entity;

import lombok.Data;
import jakarta.persistence.*;


@Data
@Entity
public class Skier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
}
