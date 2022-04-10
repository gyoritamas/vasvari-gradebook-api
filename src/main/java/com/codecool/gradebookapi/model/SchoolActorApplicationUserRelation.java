package com.codecool.gradebookapi.model;

import com.codecool.gradebookapi.security.ApplicationUserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolActorApplicationUserRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long appUserId;

    @Enumerated(EnumType.STRING)
    private ApplicationUserRole userRole;

    private Long schoolActorId;

}
