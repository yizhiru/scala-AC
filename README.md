# Scala implementation of Aho-Corasick algorithm

A byte-oriented [Aho-Corasick](https://en.wikipedia.org/wiki/Aho%E2%80%93Corasick_algorithm) implementation in pure Scala. Keywords can carry a user-defined property tag.

## Features

- UTF-8 is used explicitly for string APIs, so behavior is consistent across platforms
- Failure links are built automatically on the first search (or call `build()` eagerly)
- Array-based goto table and vocabulary indexing for better performance and memory use on large dictionaries

## Requirements

- Java 11+
- Maven 3.6+

## Build and test

```bash
mvn test
```

## Usage

```scala
import io.yizhiru.ac.Automaton

val ac = new Automaton
ac.addWords("pronoun", "he", "she", "his", "hers")

// build() is optional; search triggers construction automatically
val results: Set[(String, String)] = ac.search("ushers")
// Set(("he", "pronoun"), ("she", "pronoun"), ("hers", "pronoun"))
```

Search raw bytes when you already have UTF-8 (or other byte-level) data:

```scala
ac.addWordBytes("tag", "he".getBytes(java.nio.charset.StandardCharsets.UTF_8))
ac.searchBytes("ushers".getBytes(java.nio.charset.StandardCharsets.UTF_8))
```

## API notes

| Method | Description |
| --- | --- |
| `addWord(property, word)` | Register one UTF-8 keyword |
| `addWords(property, words*)` | Register multiple keywords with the same property |
| `addWordBytes(property, bytes)` | Register a keyword from raw bytes |
| `build()` | Eagerly construct failure links and complete the goto table |
| `search(text)` | Match UTF-8 text, returns `Set[(keyword, property)]` |
| `searchBytes(data)` | Match raw bytes |

`setFailTransitions()` is deprecated and kept only for backward compatibility; it delegates to `build()`.

Duplicate matches of the same `(keyword, property)` pair in one text are deduplicated because results are returned as a `Set`.

## Encoding

All string-based methods encode and decode with `StandardCharsets.UTF_8`. If your input uses another charset, convert to bytes yourself and use `addWordBytes` / `searchBytes`.
