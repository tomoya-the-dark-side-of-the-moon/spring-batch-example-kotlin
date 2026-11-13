package com.example.demo.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "member")
class FullNameMember(
    
    @Id
    @Column(name = "id")
    var id: Int?,

    @Column(name = "first_name")
    var firstName: String?,

    @Column(name = "last_name")
    var lastName: String?,

    @Column(name = "full_name")
    var fullName: String?,

)