
help:
    just --list

# Run project code
run:
    clojure -M -m gpt.main

# Run project from uberjar
run-jar:
    cp .env target/.env && java -jar target/ask-gpt*.jar

# Run nrepl with flowstorm
nrepl:
    clojure -M:flowstorm:dev -m nrepl.cmdline

nrepl-without-flowstorm:
    clojure -M:dev -m nrepl.cmdline

format-check:
    clojure -M:format -m cljfmt.main check src

format:
    clojure -M:format -m cljfmt.main fix src

lint:
    clojure -M:lint -m clj-kondo.main --lint .

clean-build:
    clj -T:build clean

# Build uberjar
build-uber:
    clj -T:build uber
