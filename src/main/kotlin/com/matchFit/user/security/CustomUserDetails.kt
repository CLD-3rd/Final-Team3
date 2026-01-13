package com.matchFit.user.security

import com.matchFit.user.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


class CustomUserDetails(
    val user: User
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> =
        setOf(SimpleGrantedAuthority("ROLE_USER"))

    override fun getPassword(): String = user.password

    override fun getUsername(): String = user.email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    val userId: Long?
        get() = user.id
}
