---
layout: default
title: "Differences between Scalac and Dotty"
---

Overview explanation how symbols, named types and denotations hang together:
[Denotations.scala:22]

### Denotation ###
Comment with a few details: [Denotations.scala:70]

A `Denotation` is the result of a name lookup during a given period

* Most properties of symbols are now in the denotation (name, type, owner,
  etc.)
* Denotations usually have a reference to the selected symbol
* Denotations may be overloaded (`MultiDenotation`). In this case the symbol
  may be `NoSymbol` (the two variants have symbols).
* Non-overloaded denotations have an `info`

Denotations of methods have a signature ([Signature.scala:7]), which
uniquely identifies overloaded methods.

#### Denotation vs. SymDenotation ####
A `SymDenotation` is an extended denotation that has symbol-specific properties
(that may change over phases)
* `flags`
* `annotations`
* `info`

`SymDenotation` implements lazy types (similar to scalac). The type completer
assigns the denotation's `info`.

#### Implicit Conversion ####
There is an implicit conversion:
```scala
core.Symbols.toDenot(sym: Symbol)(implicit ctx: Context): SymDenotation
```

Because the class `Symbol` is defined in the object `core.Symbols`, the
implicit conversion does **not** need to be imported, it is part of the
implicit scope of the type `Symbol` (check the Scala spec). However, it can
only be applied if an implicit `Context` is in scope.

### Symbol ###
* `Symbol` instances have a `SymDenotation`
* Most symbol properties in scalac are now in the denotation (in dotc)

Most of the `isFooBar` properties in scalac don't exist anymore in dotc. Use
flag tests instead, for example:

```scala
if (sym.isPackageClass)         // scalac
if (sym is Flags.PackageClass)  // dotc (*)
```

`(*)` Symbols are implicitly converted to their denotation, see above. Each
`SymDeotation` has flags that can be queried using the `is` method.

### Flags ###
* Flags are instances of the value class `FlagSet`, which encapsulates a
  `Long`
* Each flag is either valid for types, terms, or both

```
000..0001000..01
        ^     ^^
        flag  | \
              |  valid for term
              valid for type
```

* Example: `Module` is valid for both module values and module classes,
  `ModuleVal` / `ModuleClass` for either of the two.
* `flags.is(Method | Param)`: true if `flags` has either of the two
* `flags.is(allOf(Method | Deferred))`: true if `flags` has both. `allOf`
  creates a `FlagConjunction`, so a different overload of `is` is chosen.
  - Careful: `flags.is(Method & Deferred)` is always true, because `Method &
    Deferred` is empty.

### Tree ###
* Trees don't have symbols
  - `tree.symbol` is `tree.denot.symbol`
  - `tree.denot` is `tree.tpe.denot` where the `tpe` is a `NamdedType` (see
    next point)
* Subclasses of `DenotingTree` (`Template`, `ValDef`, `DefDef`, `Select`,
  `Ident`, etc.) have a `NamedType`, which has a `denot` field. The
  denotation has a symbol.
  - The `denot` of a `NamedType` (prefix + name) for the current period is
    obtained from the symbol that the type refers to. This symbol is searched
    using `prefix.member(name)`.


### Type ###
 * `MethodType(paramSyms, resultType)` from scalac =>
    `mt @ MethodType(paramNames, paramTypes)`. Result type is `mt.resultType`

`@todo`

[Denotations.scala:22]: https://github.com/lampepfl/dotty/blob/master/src/dotty/tools/dotc/core/Denotations.scala#L22
[Denotations.scala:70]: https://github.com/lampepfl/dotty/blob/master/src/dotty/tools/dotc/core/Denotations.scala#L70
[Signature.scala:7]: https://github.com/lampepfl/dotty/blob/master/src/dotty/tools/dotc/core/Signature.scala#L7
