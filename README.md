# Eric's Advent of Code 2023 Solutions

## The Blog

For the last three years, I've blogged my approaches to the Advent of Code puzzles on my
[personal site](https://www.ericburden.work/blog/). Assuming I hold true to form, each 
blog post will include code and commentary on my thinking behind the approach, my thoughts
about the puzzles, and vain attempts at wit.

## Project Structure

This year, I'm using Kotlin! Much like Rust and Julia in prior years, Kotlin is a
new language for me. Given the popularity of the language and the (relatively) easy
access it promises to provide for mobile app development, it seemed like a good
language to learn.

```
<project root>
├─src
│ ├─main
│ │ ├─dev.ericburden.aoc2023
│ │ │ ├─Day##.kt
│ │ │ └─Resources.kt
│ │ └resources
│ │   └─day##.txt
│ └─test
│   ├─dev.ericburden.aoc2023
│   │ └─Day##Test.kt
│   └resources
│     └─day##ex##.txt
├─pom.xml
└─README.md
```

There are a few organizational notes to point out here:

- Each day includes code in `src/main/dev/ericburden/aoc2023/Day##.kt` and 
  the associated tests in `src/test/dev/ericburden/aoc2023/Day##Test.kt`. Each
  `Day##.kt` file contains a single `Day##` class with a `parsed` private 
  value representing the parsed input and `solvePart1()` and `solvePart2()`
  member functions. There may be other helper functions, attributes, or inner
  classes as needed to support readability.
- The `Resources.kt` file defines the `Resources` object, used for accessing the
  input files and example input files. This object has a few convenience methods
  for accessing this file as a single string, a list of strings by line, a list of
  numeric values by line, etc.
- Input files are stored in `src/main/resources`, named by day. Examples from the 
  puzzle text are stored in `src/test/resources`, named by day and by the order in
  which the example appears in the text. Both are accessible through the
  `Resources` object.
  
## Usage
  
This project uses Maven as the build system. Dependencies can be installed with 
`mvn compile`. Run all tests with `mvn test`. More to come in this section as
I discover more functionality :D.
 
