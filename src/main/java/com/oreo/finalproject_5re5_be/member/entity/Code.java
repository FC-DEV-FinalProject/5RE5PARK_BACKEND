package com.oreo.finalproject_5re5_be.member.entity;

import com.oreo.finalproject_5re5_be.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "code")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Code extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code_seq", nullable = false)
    private Long codeSeq;

    @Column(name = "cate_num", nullable = false, length = 30)
    private String cateNum;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", length = 30)
    private String name;

    @Column(name = "ord")
    private Integer ord;

    @Column(name = "chk_use", length = 1, columnDefinition = "char(1) default 'Y'")
    private String chkUse;

    @Column(name = "comt", length = 250)
    private String comt;

}

