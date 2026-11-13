package com.example.demo.domain

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length

class Member {
    @NotBlank
    var id: String? = null

    @NotBlank
    @Length(max = 20)
    var firstName: String? = null

    @NotBlank
    @Length(max = 20)
    var lastName: String? = null
}