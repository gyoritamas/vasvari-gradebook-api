package org.vasvari.gradebookapi.security;

import com.google.common.collect.Sets;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

public enum ApplicationUserRole {
    ADMIN, TEACHER, STUDENT;

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        return Sets.newHashSet(new SimpleGrantedAuthority("ROLE_" + this.name()));
    }
}
