package com.github.hoshikurama.tmdiscord


sealed interface Target {
    val name: String

    fun serialize(): String

    enum class Type {
        USER, CONSOLE, NOBODY, GROUP, PHRASE
    }
}

object Targets {

    fun deserialize(input: String, locale: Locale): Target {
        val split = input.split(".", limit = 2)
        return when (split[0].run(Target.Type::valueOf)) {
            Target.Type.CONSOLE -> Console(locale)
            Target.Type.NOBODY -> Nobody(locale)
            Target.Type.USER -> User(split[1])
            Target.Type.GROUP -> Group(split[1])
            Target.Type.PHRASE -> Phrase(split[1])
        }
    }

    @JvmInline
    value class User(override val name: String) : Target {
        override fun serialize() = "${Target.Type.USER.name}.$name"
    }

    @JvmInline
    value class Group(override val name: String) : Target {
        override fun serialize() = "${Target.Type.GROUP.name}.$name"
    }

    @JvmInline
    value class Phrase(override val name: String) : Target {
        override fun serialize() = "${Target.Type.PHRASE.name}.$name"
    }

    class Console(locale: Locale) : Target {
        override val name = locale.consoleName
        override fun serialize() = Target.Type.CONSOLE.name
    }

    class Nobody(locale: Locale) : Target {
        override val name = locale.nobodyName
        override fun serialize() = Target.Type.NOBODY.name
    }
}