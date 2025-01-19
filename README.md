# Simple Web App to access stateless GPT LLM completions endpoint

# Project uses Babashka tasks

Use `bb tasks` to check available tasks

Make sure you have `.env` file in your local project directory or near jar file. 
The structure as follows
```
ENVIRONMENT=prod
PORT=7777
GPTKEY=
GOOGLE-CLIENT-ID=
GOOGLE-CLIENT-TOKEN=
allowed-emails="user1@email.com user2@email.com"

```

## To start nRepl use
`bb nrepl`
Then connect to it and evaluate `(start-system!)` from `user` ns. 
Evaluate `(restart-system!)` to reload routes logic etc. 

## To build uberjar:
`build-jar`

## To start from jar:
`start-jar`

# TODO:
- Fix styling
