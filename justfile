
help:
    just --list

run:
    clojure -M -m gpt.main

nrepl:
    clojure -M:dev -m nrepl.cmdline

format-check:
    clojure -M:format -m cljfmt.main check src

format:
    clojure -M:format -m cljfmt.main fix src

lint:
    clojure -M:lint -m clj-kondo.main --lint .

clean-build:
    clj -T:build clean

build-uber:
    clj -T:build uber
