# DOCUMENTATIE README VOOR COMPILER OPDRACHT


run het commando
`antlr4 ICSS.g4`
`
in de parser map om een parser te laten genereren



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
