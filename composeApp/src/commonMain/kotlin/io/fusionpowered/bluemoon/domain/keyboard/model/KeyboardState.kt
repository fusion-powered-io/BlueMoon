package io.fusionpowered.bluemoon.domain.keyboard.model

data class KeyboardState(
    val activeModifiers: Set<Key.Modifier> = emptySet(),
    val pressedKeys: Set<Key> = emptySet()
) {

    sealed interface Key {

        enum class Modifier : Key {
            LeftControl, LeftShift, LeftAlt, LeftMeta,
            RightControl, RightShift, RightAlt
        }

        enum class Letter : Key {
            A, B, C, D, E, F, G, H, I, J, K, L, M,
            N, O, P, Q, R, S, T, U, V, W, X, Y, Z
        }

        enum class Number : Key {
            Key1, Key2, Key3, Key4, Key5, Key6, Key7, Key8, Key9, Key0
        }

        enum class Symbol : Key {
            Minus, Equal, LeftBrace, RightBrace, Backslash,
            Semicolon, Apostrophe, Grave, Comma, Dot, Slash
        }

        enum class Function : Key {
            Enter, Escape, Backspace, Tab, Space, CapsLock,
            Up, Down, Left, Right, Delete, Home, End
        }
    }

}
