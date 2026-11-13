package com.example.demo.listener

import com.example.demo.domain.FullNameMember
import com.example.demo.domain.Member
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.listener.SkipListener
import org.springframework.batch.infrastructure.item.validator.ValidationException
import org.springframework.validation.BindException


class MemberItemSkipListener : SkipListener<Member, FullNameMember> {

    override fun onSkipInProcess(item: Member, t: Throwable) {
        if (t is ValidationException) {
            val cause: BindException = t.cause as BindException
            for (fieldError in cause.fieldErrors) {
                val field: String = fieldError.field
                val defaultMessage: String? = fieldError.defaultMessage
                logger.warn { "$field: $defaultMessage" }
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

}