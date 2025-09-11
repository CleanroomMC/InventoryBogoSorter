# BogoSorter JSON Configuration Wiki

## Overview

This JSON configuration format allows you to customize how BogoSorter handles different container types in Minecraft. The configuration centers around defining `actions` that specify sorting behavior, button placement, and slot grouping rules.

## Root Object

The root object contains a single required property:

```json5
{
  "actions": [
    // Array of action objects
  ]
}
```

See [Actions.md](actions.md) for documentation for action object.

You can also add a `$schema` property to get better IDE support for your JSON writing:

```json5
{
  "$schema": "path/to/your/schema.json",
  "actions": [
    // Array of action objects
  ]
}
```

Schema for Data driven BogoSorter compat: [**here**](bogo.compat.schema.json)

## Example

```json
{
  "actions": [
    {
      "type": "set_button_pos",
      "target": "net.minecraft.inventory.Container",
      "pos_setter": {
        "type": "top_right_horizontal"
      }
    },
    {
      "type": "slot_mapped",
      "condition": {
        "type": "mod",
        "id": "example_mod",
        "version_range": "[1.2.0,)"
      },
      "target": "com.example.CustomContainer",
      "row_size": 9,
      "slot_filter": {
        "type": "and",
        "filters": [
          {
            "type": "instanceof",
            "class": "com.example.CustomSlot"
          },
          {
            "type": "index_in_range",
            "start": 0,
            "end": 27
          }
        ]
      },
      "slot_reducer": {
        "type": "custom_stack_limit",
        "limit": 16
      }
    }
  ]
}
```
