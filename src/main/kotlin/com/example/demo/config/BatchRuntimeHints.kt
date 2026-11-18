package com.example.demo.config

import com.example.demo.domain.FullNameMember
import com.example.demo.domain.Member
import com.example.demo.domain.Reservation
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.batch.core.scope.context.StepContext

class BatchRuntimeHints : RuntimeHintsRegistrar {
    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        // DataClassRowMapper / BeanWrapperFieldSetMapper / Bean Validation 用
        hints.reflection().registerType(
            Reservation::class.java, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.ACCESS_DECLARED_FIELDS
        )
        hints.reflection().registerType(
            Member::class.java, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.ACCESS_DECLARED_FIELDS
        )

        // Hibernate がエンティティのフィールド(@Id などのアノテーション)を読むため
        hints.reflection().registerType(
            FullNameMember::class.java, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.ACCESS_DECLARED_FIELDS
        )

        // @StepScope の SpEL (#{jobExecutionContext[...]}, #{stepExecutionContext[...]}) 用
        hints.reflection().registerType(StepContext::class.java, MemberCategory.INVOKE_PUBLIC_METHODS)

        // FlatFileItemReader が読み込む classpath 上の CSV ファイル
        hints.resources().registerPattern("*.csv")

        // ExecutionContext の Java シリアライズ用
        hints.reflection().registerJavaSerialization(Reservation::class.java)
        hints.reflection().registerJavaSerialization(ArrayList::class.java)
        //hints.serialization().registerType(Reservation.class);
        //hints.serialization().registerType(ArrayList.class);
    }
}