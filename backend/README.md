# Backend

## Getting started

- Install Java 21
- Generate a key pair for generating JWTs for authentication (for example by using
  OpenSSL): `openssl ecparam -genkey -name secp521r1 -noout -out ./backend/server/src/main/resources/keys/key_pair.pem`
- Run `./gradlew :server:run`

## Common pitfalls

- Currently, we persist all data in SQLite databases. These are stored under `~/.author/db`. If you want to start with a
  clean database, just delete this folder and restart the server.