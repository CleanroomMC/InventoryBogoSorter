# BogoSorter JSON Configuration Wiki

## Condition

Condition determines whether the action should be applied.

### Condition Types

#### 1. not
Inverts the result of a sub-condition.

**Properties:**
- `type`: "not" (required)
- `condition`: Condition object (required)

**Example:**
```json
{
  "type": "not",
  "condition": {
    "type": "mod",
    "id": "example_mod"
  }
}
```

#### 2. or
Returns true if any sub-condition is true.

**Properties:**
- `type`: "or" (required)
- `conditions`: Array of condition objects (required)

**Example:**
```json
{
  "type": "or",
  "conditions": [
    { "type": "mod", "id": "test_mod" },
    { "type": "mod", "id": "test_mod_v2" }
  ]
}
```

#### 3. mod
Checks for mod presence and version.

**Properties:**
- `type`: "mod" (required)
- `id`: Mod ID (required)
- `version_pattern`: RegEx pattern for version matching (optional)
- `version_range`: Maven Version Range syntax (optional)

**Example:**
```json
{
  "type": "mod",
  "id": "jei",
  "version_range": "[1.2.3,)"
}
```

#### 4. constant
Returns a fixed boolean value.

**Properties:**
- `type`: "constant" (required)
- `value`: Boolean value (required)

**Example:**
```json
{
  "type": "constant",
  "value": true
}
```

#### 5. and
Returns true if all sub-conditions are true.

**Properties:**
- `type`: "and" (required)
- `conditions`: Array of condition objects (required)

**Example:**
```json
{
  "type": "and",
  "conditions": [
    { "type": "mod", "id": "jei" },
    { "type": "constant", "value": true }
  ]
}
```
