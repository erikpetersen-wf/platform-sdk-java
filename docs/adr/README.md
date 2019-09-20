# Architecture Decision Records

An architectural decision record (ADR) is a document that captures an important architectural decision made along with its context and consequences.

[[Source](https://github.com/joelparkerhenderson/architecture_decision_record)]


## Template

```markdown
# TITLE <short present tense imperative phrase, less than 50 characters, like a git commit message.>
Status: <proposed, accepted, rejected, deprecated, superseded, etc.>

## Context
<what is the issue that we're seeing that is motivating this decision or change.>

## Decision
<what is the change that we're actually proposing or doing.>

## Consequences
<what becomes easier or more difficult to do because of this change.>
```

[[Source](https://github.com/joelparkerhenderson/architecture_decision_record/blob/master/adr_template_by_michael_nygard.md)]

# Why ADRs?
Status: proposed

## Context
Answering the same question over and over is not good.
Answers become inconsistent and lose overall context.

## Decision
Document medium to large decisions in markdown form here.
Smaller decisions that are not externally consumable can be left as code comments.

## Consequences
Conversations about things that are documented turn into RTFM.
Takes a PR to get new documentation added.
