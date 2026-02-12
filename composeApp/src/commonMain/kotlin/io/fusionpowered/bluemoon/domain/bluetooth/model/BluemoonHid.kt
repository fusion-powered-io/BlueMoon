package io.fusionpowered.bluemoon.domain.bluetooth.model

data object BluemoonHid {

    const val NAME = "BlueMoon Gamepad"
    const val DESCRIPTION = "BlueMoon HID Controller"
    const val MANUFACTURER = "Fusion"
    val DESCRIPTOR = ubyteArrayOf(
        // Keyboard
        0x05u, 0x01u, // Usage Page (Generic Desktop)
        0x09u, 0x06u, // Usage (Keyboard)
        0xA1u, 0x01u, // Collection (Application)
        0x85u, 0x01u, // Report ID
        0x05u, 0x07u, // Usage Page (Keyboard)
        0x19u, 0xE0u, // Usage Minimum (224)
        0x29u, 0xE7u, // Usage Maximum (231)
        0x15u, 0x00u, // Logical Minimum (0)
        0x25u, 0x01u, // Logical Maximum (1)
        0x75u, 0x01u, // Report Size (1)
        0x95u, 0x08u, // Report Count (8)
        0x81u, 0x02u, // Input (Data,Var,Abs) ; modifiers
        0x95u, 0x01u, // Report Count (1)
        0x75u, 0x08u, // Report Size (8)
        0x81u, 0x01u, // Input (Const) ; reserved
        0x95u, 0x05u, // Report Count (5)
        0x75u, 0x01u, // Report Size (1)
        0x05u, 0x08u, // Usage Page (LEDs)
        0x19u, 0x01u, // Usage Minimum (1)
        0x29u, 0x05u, // Usage Maximum (5)
        0x91u, 0x02u, // Output (Data,Var,Abs) ; LEDs
        0x95u, 0x01u, // Report Count (1)
        0x75u, 0x03u, // Report Size (3)
        0x91u, 0x01u, // Output (Const) ; padding
        0x95u, 0x06u, // Report Count (6)
        0x75u, 0x08u, // Report Size (8)
        0x15u, 0x00u, // Logical Minimum (0)
        0x25u, 0x65u, // Logical Maximum (101)
        0x05u, 0x07u, // Usage Page (Keyboard)
        0x19u, 0x00u, // Usage Minimum (0)
        0x29u, 0x65u, // Usage Maximum (101)
        0x81u, 0x00u, // Input (Data,Arr,Abs) ; keycodes
        0xC0u,        // End Collection (Keyboard)

        // Mouse
        0x05u, 0x01u, // Usage Page (Generic Desktop)
        0x09u, 0x02u, // Usage (Mouse)
        0xA1u, 0x01u, // Collection (Application)
        0x85u, 0x02u, // Report ID
        0x09u, 0x01u, // Usage (Pointer)
        0xA1u, 0x00u, // Collection (Physical)
        0x05u, 0x09u, // Usage Page (Buttons)
        0x19u, 0x01u, // Usage Minimum (1)
        0x29u, 0x03u, // Usage Maximum (3)
        0x15u, 0x00u, // Logical Minimum (0)
        0x25u, 0x01u, // Logical Maximum (1)
        0x95u, 0x03u, // Report Count (3)
        0x75u, 0x01u, // Report Size (1)
        0x81u, 0x02u, // Input (Data,Var,Abs) ; buttons
        0x95u, 0x01u, // Report Count (1)
        0x75u, 0x05u, // Report Size (5)
        0x81u, 0x01u, // Input (Const) ; padding
        0x05u, 0x01u, // Usage Page (Generic Desktop)
        0x09u, 0x30u, // Usage (X)
        0x09u, 0x31u, // Usage (Y)
        0x15u, 0x81u, // Logical Minimum (-127)
        0x25u, 0x7Fu, // Logical Maximum (127)
        0x75u, 0x08u, // Report Size (8)
        0x95u, 0x02u, // Report Count (2)
        0x81u, 0x06u, // Input (Data,Var,Rel) ; X,Y
        0x05u, 0x01u, // Usage Page (Generic Desktop)
        0x09u, 0x38u, // Usage (Wheel)
        0x15u, 0x81u, // Logical Minimum (-127)
        0x25u, 0x7Fu, // Logical Maximum (127)
        0x75u, 0x08u, // Report Size (8)
        0x95u, 0x01u, // Report Count (1)
        0x81u, 0x06u, // Input (Data,Var,Rel) ; Wheel
        0xC0u,        // End Collection (Physical)
        0xC0u,        // End Collection (Mouse)

        // Gamepad
        0x05u, 0x01u,                       // Usage Page (Generic Desktop)
        0x09u, 0x05u,                       // Usage (Game Pad)
        0xA1u, 0x01u,                       // Collection (Application)
        0x85u, 0x03u,                       // Report ID

        // Sticks: X, Y, Z, Rz (4 x 16-bit)
        0x05u, 0x01u,                       // Usage Page (Generic Desktop)
        0x09u, 0x30u,                       // Usage (X)
        0x09u, 0x31u,                       // Usage (Y)
        0x09u, 0x32u,                       // Usage (Z) - often Right Stick X
        0x09u, 0x35u,                       // Usage (Rz) - often Right Stick Y
        0x15u, 0x00u,                       // Logical Minimum (0)
        0x27u, 0xFFu, 0xFFu, 0x00u, 0x00u,  // Logical Maximum (65535)
        0x75u, 0x10u,                       // Report Size (16 bits)
        0x95u, 0x04u,                       // Report Count (4 axes)
        0x81u, 0x02u,                       // Input (Data, Variable, Absolute)*/

        // Triggers
        0x05u, 0x02u,                       // USAGE_PAGE (Simulation Control)
        0x15u, 0x00u,                       // LOGICAL_MINIMUM (0)
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
        0x15u, 0x00u,                       // Logical Minimum (0)
        0x25u, 0x01u,                       // Logical Maximum (1)
        0x95u, 0x10u,                       // Report Count (16)
        0x75u, 0x01u,                       // Report Size (1)
        0x81u, 0x02u,                       // Input (Data, Variable, Absolute)

        // Hat Switch (4 bits)
        0x05u, 0x01u,                       // Usage Page (Generic Desktop)
        0x09u, 0x39u,                       // Usage (Hat Switch)
        0x15u, 0x00u,                       // Logical Minimum (1)
        0x25u, 0x07u,                       // Logical Maximum (8)
        0x35u, 0x00u,                       // Physical Minimum (0)
        0x46u, 0x3Bu, 0x01u,                // Physical Maximum (315) - for degrees
        0x66u, 0x14u, 0x00u,                // Unit (English Rotation: Degrees)
        0x75u, 0x04u,                       // Report Size (4 bits)
        0x95u, 0x01u,                       // Report Count (1)
        0x81u, 0x42u,                       // Input (Data, Variable, Absolute, Null State)

        // Final Padding: 4 bits to finish the final byte
        0x75u, 0x04u,                       // Report Size (4)
        0x95u, 0x01u,                       // Report Count (1)
        0x81u, 0x03u,                       // Input (Constant, Variable, Absolute)

        0xC0u                               // End Collection
    )
}