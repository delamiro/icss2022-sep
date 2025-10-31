# DOCUMENTATIE README VOOR COMPILER OPDRACHT


run het commando
`mvn clean generate-sources`
Om de gegenereerde resources opnieuwe te genereren.
dit word gegenereerd op basis van de ICSS.g4 file. 



## Wat te fuck moeten we doen

Wij gaan een eigen versie van CSS maken, ICSS, dit word dan weer TERUG getranslate naar CSS.

[De opdracht](https://aim-cni.github.io/app/docs/Compiler%20opdracht)

Wij gaan een compiler schrijven voor de ICSS css taal. een compiler heeft verschillende fases

1. Lexing - inlezen van code en opdelen in tokens
2. Prasing - opbouwen van parse tree (Abstract syntax tree)
3. Semantic analysis - Controleren van types en operatorgebruik
4. Optimization - Vereenvoudigen van de code en verbeteren van prestaties
5. Code generation - vertalen naar machinecode of bytecode
6. Linking - Samenvoegen van verschillende modules tot 1 executable


# Hoe werkt een compiler

Een compiler bestaat uit de volgende onderdelen:

1. Lexer
2. Parser
3. Checker - Controleert types en semantiek
4. Generator - Genereert uitvoerbare code



| id   | gedaan? |
|------|---------|
| AL01 | Ja      |
| AL02 | Ja      |
| AL03 | Ja      |
| AL04 | Ja      |



| id   | gedaan? |
|------|---------|
| PA00 | Ja      |
| PA01 | Ja      |
| PA02 | Ja      |
| PA03 | Ja      |
| PA04 | Ja      |
| PA04 | Ja      |


| id   | gedaan? |
|------|---------|
| CH00 | Ja      |
| CH01 | Ja      |
| CH02 | Ja      |
| CH03 | Nee     |
| CH04 | Ja      |
| CH05 | Nee     |
| CH06 | Ja      |


| id   | gedaan? |
|------|---------|
| TR01 | Ja      |
| TR02 | Ja      |


| id   | gedaan? |
|------|---------|
| GE01 | Ja      |
| GE02 | Ja      |


## Eigen notes

ASTListener MOET IK in werken, volgens chat is dat de ASTBuilder!!!! bouw hierop verder uit