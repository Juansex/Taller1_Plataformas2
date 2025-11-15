FROM golang:1.21-alpine AS builder

WORKDIR /app
COPY . .

RUN export GO111MODULE=on && \
    go mod init github.com/bortizf/microservice-app-example/tree/master/auth-api 2>/dev/null || true && \
    go mod tidy && \
    go build -o auth-api .

FROM alpine:latest

RUN apk --no-cache add ca-certificates

WORKDIR /root/

COPY --from=builder /app/auth-api .

EXPOSE 8000

CMD ["./auth-api"]
