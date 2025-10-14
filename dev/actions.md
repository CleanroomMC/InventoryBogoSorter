# BogoSorter JSON Configuration Wiki

## Action Types

### 1. set_button_pos
Sets the position of the sorting button for a container.

**Properties:**
- `type`: "set_button_pos" (required)
- `condition`: Condition object (optional), see [Condition System](condition.md) for documentation
- `target`: Container class name (required, e.g., "net.minecraft.inventory.Container")
- `pos_setter`: Button position configuration (required), see [Button Position Setters](#button-position-setters) for documentation.

**Example:**
```json
{
  "type": "set_button_pos",
  "target": "net.minecraft.inventory.Container",
  "pos_setter": {
    "type": "top_right_horizontal"
  }
}
```

### 2. generic
The most basic support for normal containers.

**Properties:**
- `type`: "generic" (required)
- `condition`: Condition object (optional), see [Condition System](condition.md) for documentation
- `target`: Container class name (required)

**Example:**
```json
{
  "type": "generic",
  "target": "net.minecraft.inventory.Container"
}
```

### 3. slot_mapped
Registers a slot group with custom filtering and conversion rules.

**Properties:**
- `type`: "slot_mapped" (required)
- `condition`: Condition object (optional), see [Condition System](condition.md) for documentation
- `target`: Container class name (required)
- `row_size`: Number of slots per row (required)
- `slot_filter`: Slot filter configuration (optional), see [Slot Filters](#slot-filters)
- `slot_reducer`: Slot converter configuration (optional), see [Slot Reducers](#slot-reducers)

**Example:**
```json
{
  "type": "slot_mapped",
  "target": "com.example.CustomContainer",
  "row_size": 9,
  "slot_filter": {
    "type": "instanceof",
    "class": "com.example.CustomSlot"
  }
}
```

### 4. remove
Removes sorting compatibility for a container.

**Properties:**
- `type`: "remove" (required)
- `condition`: Condition object (optional), see [Condition System](condition.md) for documentation
- `target`: Container class name (required)

**Example:**
```json
{
  "type": "remove",
  "target": "net.minecraft.inventory.Container"
}
```

### 5. slot_range
Registers a slot group for slots within a specific index range.

**Properties:**
- `type`: "slot_range" (required)
- `condition`: Condition object (optional), see [Condition System](condition.md) for documentation
- `target`: Container class name (required)
- `start`: First slot index (inclusive, required)
- `end`: End slot index (exclusive, required)
- `row_size`: Number of slots per row (required)

**Example:**
```json
{
  "type": "slot_range",
  "target": "net.minecraft.inventory.Container",
  "start": 0,
  "end": 27,
  "row_size": 9
}
```

## Button Position Setters

### 1. top_right_horizontal
Places buttons horizontally at the top-right of the container.

**Properties:**
- `type`: "top_right_horizontal" (required)

**Example:**
```json
{
  "type": "top_right_horizontal"
}
```

### 2. top_right_vertical
Places buttons vertically at the top-right of the container.

**Properties:**
- `type`: "top_right_vertical" (required)

**Example:**
```json
{
  "type": "top_right_vertical"
}
```

### 3. custom
Custom button positioning.

**Properties:**
- `type`: "custom" (required)
- `at_container_left`: Boolean (required). If `true`, buttons will be placed next to the first slot. If `false`, buttons will be placed next to the last slot in the first row.
- `x_offset`: Integer (required)
- `y_offset`: Integer (required)
- `alignment`: Enum (optional, values: "TOP_LEFT", "BOTTOM_RIGHT", "TOP_RIGHT", "BOTTOM_LEFT")
- `layout`: Enum (optional, values: "VERTICAL", "HORIZONTAL")

**Example:**
```json
{
  "type": "custom",
  "at_container_left": true,
  "x_offset": 5,
  "y_offset": 5,
  "alignment": "TOP_RIGHT",
  "layout": "HORIZONTAL"
}
```

## Slot Filters

### 1. instanceof
Filters slots by class. Will only accept slots that are instance of the specified class.

**Properties:**
- `type`: "instanceof" (required)
- `class`: Class name (required)

**Example:**
```json
{
  "type": "instanceof",
  "class": "net.minecraft.inventory.Slot"
}
```

### 2. index_in_range
Filters slots by index range.

**Properties:**
- `type`: "index_in_range" (required)
- `start`: Start index (inclusive, required)
- `end`: End index (exclusive, required)

**Example:**
```json
{
  "type": "index_in_range",
  "start": 0,
  "end": 27
}
```

### 3. and
Combines multiple filters with AND logic.

**Properties:**
- `type`: "and" (required)
- `filters`: Array of filter objects (required)

**Example:**
```json
{
  "type": "and",
  "filters": [
    { "type": "instanceof", "class": "net.minecraft.inventory.Slot" },
    { "type": "index_in_range", "start": 0, "end": 27 }
  ]
}
```

### 4. or
Combines multiple filters with OR logic.

**Properties:**
- `type`: "or" (required)
- `filters`: Array of filter objects (required)

**Example:**
```json
{
  "type": "or",
  "filters": [
    { "type": "index_in_range", "start": 0, "end": 9 },
    { "type": "index_in_range", "start": 27, "end": 36 }
  ]
}
```

### 5. not
Inverts a filter.

**Properties:**
- `type`: "not" (required)
- `filter`: Filter object (required)

**Example:**
```json
{
  "type": "not",
  "filter": {
    "type": "index_in_range",
    "start": 0,
    "end": 9
  }
}
```

## Slot Reducers

### 1. general
Uses default BogoSorter slot representation.

**Properties:**
- `type`: "general" (required)

**Example:**
```json
{
  "type": "general"
}
```

### 2. custom_stack_limit
Slot with custom stack size limit.

**Properties:**
- `type`: "custom_stack_limit" (required)
- `limit`: Maximum stack size (required)

**Example:**
```json
{
  "type": "custom_stack_limit",
  "limit": 16
}
```
