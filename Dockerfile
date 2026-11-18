FROM ghcr.io/graalvm/native-image-community:25 AS builder
# gradlew が使う xargs を入れる
RUN microdnf install -y findutils && microdnf clean all

COPY ./src/ /tmp/src/
COPY ./gradle/ /tmp/gradle/
COPY ./gradlew /tmp/
COPY ./build.gradle.kts /tmp/
COPY ./settings.gradle.kts /tmp/
WORKDIR /tmp
# Docker のメモリ(8GB)に収まるよう、native-image のヒープを抑え、
# Kotlin コンパイラは Gradle と同じプロセスで動かす(常駐デーモンを残さない)
ENV NATIVE_IMAGE_OPTIONS="-J-Xmx4g"
RUN ./gradlew clean nativeCompile --no-daemon -Pkotlin.compiler.execution.strategy=in-process

FROM debian:bookworm-slim AS runner
RUN apt-get update && apt-get install -y --no-install-recommends tzdata && \
    ln -sf /usr/share/zoneinfo/Asia/Tokyo /etc/localtime && \
    echo "Asia/Tokyo" > /etc/timezone && \
    rm -rf /var/lib/apt/lists/*
RUN groupadd springgroup && useradd -r -g springgroup springuser
WORKDIR /usr/app
COPY --from=builder --chown=springuser:springgroup /tmp/build/native/nativeCompile/spring-batch-example-kotlin spring-batch-example
USER springuser
CMD ["./spring-batch-example"]
