package com.github.hoshikurama.tmdiscord

import com.github.hoshikurama.tmdiscord.mode.client.ClientLocale

sealed interface Target {
    val name: String
    fun serialize(): String

    enum class Type {
        USER, CONSOLE, NOBODY, GROUP, PHRASE
    }
}

object Targets {

    fun deserialize(input: String, locale: ClientLocale): Target {
        val split = input.split(".", limit = 2)
        return when (split[0].run(Target.Type::valueOf)) {
            Target.Type.CONSOLE -> Console(locale.consoleName)
            Target.Type.NOBODY -> Nobody(locale.nobodyName)
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

    class Console(localizedName: String) : Target {
        override val name = localizedName
        override fun serialize() = Target.Type.CONSOLE.name
    }

    class Nobody(localizedName: String) : Target {
        override val name = localizedName
        override fun serialize() = Target.Type.NOBODY.name
    }
}