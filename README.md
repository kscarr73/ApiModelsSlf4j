# Purpose


# Settings

Api Models Slf4j uses a single yaml file to setup configuration: `apilogging.yaml`

## Fields

* **outputs**: ArrayList of Output Objects
  * **name**: Name for the Output
  * **type**: JSON|PLAIN|YAML The Type of Output
  * **output**: SysOut, SysErr or `Path and File Name`
  * **format**: Optional: Name of the format to use for the stream.
                Format MUST exist in the formats section. 
                If the format is omitted, it will use a format with the same name as the output.
* **formats**: ArrayList of Format Objects
  * **name**: The name of the format
  * **fields**: ArrayList of Field Objects
    * **name**: Field Name for the output
    * **mapping**: Output field Mapping
    * **prefix**: String to add to front of field
    * **postfix**: String to add to end of field
* **logs**: ArrayList of Log Levels
  * **Log Name**: Name of the Log to Set Level and Output. Partial match on pathname, or full className
    * **level**: The Level for this Log entry
    * **output**: The Log Output this logger should use.  String Array will point to multiple outputs

## Example

```yaml
logs:
  default: 
    level: INFO
    output: SysOut
  com.progbits: 
    level: DEBUG
    output: SysOut
outputs:
  SysOut:
    type: JSON
    output: SysOut
    format: default
formats:
  - name: default
    fields:
      - name: timestamp
        mapping: timestamp
      - name: level
        mapping: level
      - name: message
        mapping: message
      - name: logger
        mapping: logger
      - name: exceptionName
        mapping: exception.name
      - name: stackTrace
        mapping: exception.stacktrace
```