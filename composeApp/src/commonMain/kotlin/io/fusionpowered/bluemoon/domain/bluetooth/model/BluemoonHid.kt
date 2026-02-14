package io.fusionpowered.bluemoon.domain.bluetooth.model

data object BluemoonHid {

    const val NAME = "BlueMoon Gamepad"
    const val DESCRIPTION = "BlueMoon HID Controller"
    const val MANUFACTURER = "Fusion"
    val DESCRIPTOR = ubyteArrayOf(
        // Keyboard
        0x05u, 0x01u,                       // Usage Page (Generic Desktop)
        0x09u, 0x06u,                       // Usage (Keyboard)
        0xA1u, 0x01u,                       // Collection (Application)
        0x85u, 0x02u,                       // Report ID
        0x05u, 0x07u,                       // Usage Page (Keyboard)
        0x19u, 0xE0u,                       // Usage Minimum (224)
        0x29u, 0xE7u,                       // Usage Maximum (231)
        0x25u, 0x01u,                       // Logical Maximum (1)
        0x75u, 0x01u,                       // Report Size (1)
        0x95u, 0x08u,                       // Report Count (8)
        0x81u, 0x02u,                       // Input (Data,Var,Abs) ; modifiers
        0x95u, 0x01u,                       // Report Count (1)
        0x75u, 0x08u,                       // Report Size (8)
        0x81u, 0x01u,                       // Input (Const) ; reserved
        0x95u, 0x05u,                       // Report Count (5)
        0x75u, 0x01u,                       // Report Size (1)
        0x05u, 0x08u,                       // Usage Page (LEDs)
        0x19u, 0x01u,
        0x29u, 0x05u,
        0x91u, 0x02u,                       // Output (LEDs)
        0x95u, 0x01u, 0x75u, 0x03u, 0x91u, 0x01u, // Output padding
        0x95u, 0x06u, 0x75u, 0x08u,         // Report Size (8)
        0x15u, 0x00u, 0x25u, 0x65u,         // Logical Max (101)
        0x05u, 0x07u,                       // Usage Page (Keyboard)
        0x19u, 0x00u, 0x29u, 0x65u,
        0x81u, 0x00u,                       // Input (Keycodes)
        0xC0u,

        // Mouse
        0x05u, 0x01u,                       // Usage Page (Generic Desktop)
        0x09u, 0x02u,                       // Usage (Mouse)
        0xA1u, 0x01u,                       // Collection (Application)
        0x85u, 0x03u,                       // Report ID
        0x09u, 0x01u,                       // Usage (Pointer)
        0xA1u, 0x00u,                       // Collection (Physical)
        0x05u, 0x09u,                       // Usage Page (Buttons)
        0x19u, 0x01u, 0x29u, 0x03u,
        0x15u, 0x00u, 0x25u, 0x01u,
        0x95u, 0x03u, 0x75u, 0x01u, 0x81u, 0x02u, // Buttons
        0x95u, 0x01u, 0x75u, 0x05u, 0x81u, 0x01u, // Padding
        0x05u, 0x01u,                       // Usage Page (Generic Desktop)
        0x09u, 0x30u, 0x09u, 0x31u,
        0x15u, 0x81u, 0x25u, 0x7Fu,
        0x75u, 0x08u, 0x95u, 0x02u, 0x81u, 0x06u, // Rel X, Y
        0x09u, 0x38u,                       // Usage (Wheel)
        0x95u, 0x01u, 0x81u, 0x06u,         // Rel Wheel
        0xC0u, 0xC0u,

        // Gamepad
        0x05u, 0x01u,                       // Usage Page (Generic Desktop)
        0x09u, 0x05u,                       // Usage (Game Pad)
        0xA1u, 0x01u,                       // Collection (Application)
        0x85u, 0x01u,                       // Report ID

        // Sticks: X, Y, Z, Rz (4 x 16-bit)
        0x09u, 0x30u,                       // Usage (X)
        0x09u, 0x31u,                       // Usage (Y)
        0x09u, 0x32u,                       // Usage (Z)
        0x09u, 0x35u,                       // Usage (Rz)
        0x15u, 0x00u,                       // Logical Minimum (0)
        0x26u, 0xFFu, 0xFFu,                // Logical Maximum (65535) - Optimized to 16-bit
        0x75u, 0x10u,                       // Report Size (16 bits)
        0x95u, 0x04u,                       // Report Count (4 axes)
        0x81u, 0x02u,                       // Input (Data, Variable, Absolute)

        // Triggers
        0x05u, 0x02u,                       // USAGE_PAGE (Simulation Control)
        // Logical Minimum (0) is already set globally
        0x26u, 0xFFu, 0x00u,                // LOGICAL_MAXIMUM (255)
        0x09u, 0xC4u,                       // USAGE(Acceleration)
        0x09u, 0xC5u,                       // USAGE(Brake)
        0x75u, 0x08u,                       // REPORT_SIZE (8)
        0x95u, 0x02u,                       // REPORT_COUNT (2)
        0x81u, 0x02u,                       // INPUT (Data,Var,Abs)

        // Buttons: 16 Buttons (16 bits)
        0x05u, 0x09u,                       // Usage Page (Button)
        0x19u, 0x01u,                       // Usage Minimum (Button 1)
        0x29u, 0x10u,                       // Usage Maximum (Button 16)
        // Logical Minimum (0) is already set
        0x25u, 0x01u,                       // Logical Maximum (1)
        0x95u, 0x10u,                       // Report Count (16)
        0x75u, 0x01u,                       // Report Size (1)
        0x81u, 0x02u,                       // Input (Data, Variable, Absolute)

        // Hat Switch (4 bits)
        0x05u, 0x01u,                       // Usage Page (Generic Desktop)
        0x09u, 0x39u,                       // Usage (Hat Switch)
        0x15u, 0x00u,                       // Logical Minimum (0)
        0x25u, 0x07u,                       // Logical Maximum (7) - Corrected for 8 positions
        0x35u, 0x00u,                       // Physical Minimum (0)
        0x46u, 0x3Bu, 0x01u,                // Physical Maximum (315)
        0x66u, 0x14u, 0x00u,                // Unit (English Rotation: Degrees)
        0x75u, 0x04u,                       // Report Size (4 bits)
        0x95u, 0x01u,                       // Report Count (1)
        0x81u, 0x42u,                       // Input (Data, Variable, Absolute, Null State)

        // Final Padding
        0x75u, 0x04u,                       // Report Size (4)
        0x81u, 0x03u,                       // Input (Constant, Variable, Absolute)
        0xC0u,                               // End Collection

        // Media Remote
        0x05u, 0x0Cu,                       // USAGE_PAGE (Consumer Devices)
        0x09u, 0x01u,                       // USAGE (Consumer Control)
        0xA1u, 0x01u,                       // COLLECTION (Application)
        0x85u, 0x04u,                       // REPORT_ID
        0x15u, 0x00u, 0x25u, 0x01u,
        0x75u, 0x01u, 0x95u, 0x08u,
        0x09u, 0xE9u,                       // USAGE (Volume Up)
        0x09u, 0xEAu,                       // USAGE (Volume Down)
        0x09u, 0xE2u,                       // USAGE (Mute)
        0x09u, 0xCDu,                       // USAGE (Play/Pause)
        0x09u, 0xB5u,                       // USAGE (Scan Next Track)
        0x09u, 0xB6u,                       // USAGE (Scan Previous Track)
        0x09u, 0x94u,                       // USAGE (AL Local Browser) - Fixed to 16-bit
        0x09u, 0x92u,                       // USAGE (AL Calculator) - Fixed to 16-bit
        0x81u, 0x02u,                       // INPUT (Data,Var,Abs)
        0xC0u                               // END_COLLECTION
    )
}