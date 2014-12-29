rbx-toolset
===========

This is a toolset for Robox 3d printer. Intention is for it to work on all platforms where Java and <a href="https://github.com/rxtx/rxtx">rxtx</a> library works.

All commands are executions of one Java jar file where first non option is a command name. It can be invoked like:

```
java -jar robox-toolset.jar {<general-options>} <command> {<specific-options>} [<sub-command>]
```

where elements enclosed in '{}' represent none or more occurences and '[]' none or one occurence of an element (EBNF syntax). Syntax is similar to 'git' command.

Until implemented as another command ('install' command) the easiest way of invoking toolset is to create /usr/bin/rbx shellscript like:
```
#!/bin/bash

java -jar <path-to-jar>/robox-toolset.jar "$@"
```

for Unix bases machines or similar for Windows machines where .bat file should be in the PATH. 

So, with rbx shell script, invocation of toolset commands should really look like this:
```
rbx {<general-options>} <command> {<specific-options>} [<sub-command>]
```
Example:
```
rbx status
```

General options for most commands are:

- -v or --verbose to increase verbosity of selected command
- -d or --debug to print debug information
- -p or --printer <printer-id> to select one printer this command is going to be applicable to

If there is only one printer attached to the computer then it will be automatically selected. If there is more than one printer attached then -p/--printer option must be selected. It is not applicable on commands that do not directly work with printer like 'list' or 'upload'. 

Also, many commands will accept:

- -h or --help command and display further options/sub-commands.

Here are currently provided commands:

Help
----

Lists commands available in the toolset.

List
----

This command lists currently attached printers. Usage:

```
rbx list
``` 

Status
------

Status command provides status of attached printer. Usage:
```
Usage: rbx [<general-options>] status [<specific-options>]

  General options are one of these:
  -v | --verbose   - increases voutput erbosity level
  -d | --debug     - increases debug level
  -p | --printer   - if more than one printer is connected to your
                     computer you must select which one command is
                     going to be applied on. You can get list of
                     available printers using 'list' command

  Specific options are:

  -h | --help | -?     - this page

  -h | --help | -?     - this page
  -a | --all           - displays all status information
  -s | --short         - displays values only
                         It is machine readable format.
  -e | --estimate      - displays estimate time until job completion.
                         See -f/--file option for more details.
  -j | --job           - displays job id
  -b | --busy          - displays busy flag
  -ps | --pause-status - displays pause status
  -cl | --current-line - displays current line number
  -tl | --total-lines  - displays total line number. Only if file was supplied.

For estimate to work, this utility needs original xxx_robox.gcode file.
See rbx upload command.

More time passed, estimate might be more correct. Estimate is calculated
by amount of lines processed per amount of time starting from when
<jobid>.estimate file is created.
```
Example:
```
rbx status -e
```

Upload
------

This command is to send xxxx_robox.gcode file to allow estimate to work. Also, same file is needed for estimate and work of 'web' command. Usage:

```
Usage: rbx [<general-options>] update [<specific-options>]

  General options are one of these:
  -v | --verbose   - increases voutput erbosity level
  -d | --debug     - increases debug level

  Specific options are:

  -h | --help | -?     - this page

  -f | --file          - gcode file needed for estimate
  -i | --job-id        - job id
```

Note: if -f/--file option is not set, this command will try to
read job file from the stdin. But for it to work you must specify
-i/--job-id option with job-id. In case of -f/--file option you
may omit -i/--job-id option if file name comes form AutoMaker and
is in form xxxxxxxx_robox.gcode or xxxxxxxx.gcode where xxxxxxxx
is job id.

For estimate to work, this utility needs original xxx_robox.gcode file.
It will store file in ~/.robox/ dir, along with two more files:
  <jobid>.lines    - file that contains number of non-empty .gcode lines
  <jobid>.estimate - file with line number from job file that is higher
                     that 100 (warming bed and head). Also, that' file's
                     last modified date is going to serve for estimate
                     calculation.
You don't need to specify -f each time to obtain status - only once
at the beginning. <jobid>.estimate file is going to be create no matter
if estimate is going to succeed or fail (due to lack of <jobid> file
previously specified). It is going to be written only once per detected
job.

Also, the moment new job is detected, all other files from previous jobs
are going to be removed.

More time passed, estimate might be more correct. Estimate is calculated
by amount of lines processed per amount of time starting from when
<jobid>.estimate file is created.

Pause
-----
Pauses currently executing job. Usage:
```
Usage: rbx [<general-options>] pause [<specific-options>]

  General options are one of these:
  -v | --verbose   - increases voutput erbosity level
  -d | --debug     - increases debug level
  -p | --printer   - if more than one printer is connected to your
                     computer you must select which one command is
                     going to be applied on. You can get list of
                     available printers using 'list' command

  Specific options are:

  -h | --help | -?     - this page
```

Resume
------
Resumes paused job. Usage:
```
Usage: rbx [<general-options>] resume [<specific-options>]

  General options are one of these:
  -v | --verbose   - increases voutput erbosity level
  -d | --debug     - increases debug level
  -p | --printer   - if more than one printer is connected to your
                     computer you must select which one command is
                     going to be applied on. You can get list of
                     available printers using 'list' command

  Specific options are:

  -h | --help | -?     - this page
```

Abort
-----
Aborts current job. Usage:
```
Usage: rbx [<general-options>] abort [<specific-options>]

  General options are one of these:
  -v | --verbose   - increases voutput erbosity level
  -d | --debug     - increases debug level
  -p | --printer   - if more than one printer is connected to your
                     computer you must select which one command is
                     going to be applied on. You can get list of
                     available printers using 'list' command

  Specific options are:

  -h | --help | -?     - this page
```

Web
---
This command starts small web server and provides access to printers' status and ability to issue commands. Unlike 'abort', 'resume', 'pause' and 'status' commands this command do not accepts printer id and works with all printers attachet to the computer. Or none. It will discover printers attached after it has started. Also, will 'keep track' of disconnected and re-attached printers.

Usage:
```
Usage: rbx [<general-options>] web [<specific-options>] [<command>]

  General options are one of these:
  -v | --verbose   - increases voutput erbosity level
  -d | --debug     - increases debug level

  Specific options are:

  -h | --help | -?     - this page

  -h | --help | -?     - this page
  -a | --all           - displays all status information
  -s | --short         - displays values only
                         It is machine readable format.
  -p | --port                     - port to start web server.
  -rs | --refresh-status-interval <value-in-seconds>
        Refresh status interval in seconds. It is how often printer is going
        to be queried for the status. Default is 15 seconds.
  -ri | --refresh-image-interval <value-in-seconds>
        Refresh image interval in seconds. It is how image is fetched is going
        to be queried for the status. Also, if on RPi and raspistill is detected
        it will automatically be used. Default is 5 seconds
  -ic | --image-command <shell-command>
        Imaage command. This is shell command to be used to fetch image.
        Command should send image data in jpg format to stdout.
  -pc | --post-refresh-command <shell-command>
        Comamnd to be called after printer status was fetched.
        It will be called with estimage format string as first parameter.
  -cf | --post-refresh-command-format <format-string>
        Format post refresh command is going to get estimate in.
        Placeholders are %c - command, %h -hours, %m - minutes (in.
        two digit format), %s - seconds (in two digit format).
        Default format is %c: %h:%m
  -ac | --allow-commands 
        If set web pages will allow commaindg printer:
        sending pause, resume and abort commands.
  -t  | --template-file <file>
        Template html file for status file. See
        -sf/--static-files switch for extra resources like css/images...
  -sf | --static-files <directory>
        Directory where static files are stored.
        They are going to be served from root of web app ('/').
  -ar | --automatic-refresh <value-in-seconds>
        Enables internal template to create html page refresh. External templates
        can utilise it by adding ${automatic-refresh} placeholder in html head part.
```

Commands are optional. If none specified then web server will start in current process.

-  start  - starts the server in background. Not implemented yet.
-  status - displays status of the server.
-  stop   - stops the server.


Template file should have following placeholders:

- ${status}   - printing status ("Unknown", "Working", "Pausing", "Paused", "Resuming")
- ${busy}     - is printer busy or not ("true", "false"). Can be used directly in javascript.
- ${job_id}   - printer job id.
- ${error_msg}      - previous request error message
- ${estimate}       - estimate time in %h:%m:%s format
- ${estimate_hours} - estimate time hours
- ${estimate_mins}  - estimate time minutes (with leading zero)
- ${estimate_secs}  - estimate time seconds (with leading zero)
- ${current_line}   - current line
- ${total_lines}    - total lines
- ${current_line_and_total_line} - current line followed, optionally, with '/' and total lines
- ${all_printers_link}           - link (or empty) to url with list of all printers.
                                 It is set to empty if one or no printers available.
- ${capture_image_tag}           - capture image tag. It is set to ```<img src="/capture.jpg"/>```
                                 when capture is enabled or empty string if not.
- ${capture_image_css_display}   - css for display attribute for capture image section.
                                 It is set to inline when capture is enabled or none if not.
- ${commands_css_display}        - css for display attribute for commands section.
                                 It is set to inline when commands are enabled or none if not.
- ${printers_list}               - applicable only to printers page - list of <li> tags
                                 with links to known printers. Not connected printers will
                                 have no links associanted.
- ${automatic-refresh}           - tag for html head. It will be empty if -ar|--automatic-refresh
                                 option is not added. 
- ${x_limit}       - x limit switch (on/off)
- ${y_limit}       - y limit switch (on/off)
- ${z_limit}       - z limit switch (on/off)
- ${filament_1}    - filament 1 switch (on/off)
- ${filament_2}    - filament 2 switch (on/off)
- ${nozzle_switch} - nozzle switch (on/off)
- ${door_Closed}   - door closed switch (on/off)
- ${reel_button}   - reel button switch (on/off)
- ${nozzle_temp}           - nozzle temperature
- ${nozzle_set_temp}       - nozzle set temperature
- ${nozzle_temp_combined}  - nozzle + nozzle set temperature divided by '/'
- ${bed_temp}              - bed temperature
- ${bed_set_temp}          - bed set temperature
- ${nozzle_temp_combined}  - bed + bed set temperature divided by '/'
- ${ambient_temp}          - ambient temperature
- ${ambient_set_temp}      - ambient set temperature
- ${ambient_temp_combined} - ambient temperature + ambient set temperature divided by '/
- ${fan}           - fan (on/off)
- ${head_Fan}      - head fan (on/off)
- ${x_position}    - x position
- ${y_position}    - y position
- ${z_position}    - z position
- ${filament_nultiplier}  - filament multiplier
- ${feed_rate_nultiplier} - feed rate multiplier
- ${temp_state}    - temperature state ('working', 'cooling', 'heating bed' or 'heating nozzles')


Example how to start server:
```
nohup rbx >web.log web --allow-commands &
```

Stopping server:
```
rbx web stop
```

Note: On Raspberry Pi, web command is going to automatically detect existence of /usr/bin/raspistill command and add it as if ```--image-command /usr/bin/raspistill``` was supplied to the command. In order to deliberately not use it you need to specify ```--image-command ""```.

GCode
-----

This command is sending gcode directly to the printer. Usage:

```
Usage: rbx [<general-options>] gcode [<specific-options>] [<gcode-commands>]

  General options are one of these:
  -v | --verbose   - increases voutput erbosity level
  -d | --debug     - increases debug level
  -p | --printer   - if more than one printer is connected to your
                     computer you must select which one command is
                     going to be applied on. You can get list of
                     available printers using 'list' command

  Specific options are:

  -h | --help | -?     - this page
```
All arguments that do not start with '-' will be processed as gcode commands
and sent to the printer. Also, all sysin will be processed line by line and
sent to the printer. Resposes are prefixed with 'A' + number of argument + ': '
sent out or 'L' + number of line from sysin + ': ' (note trailing space).

Jobs
----

Lists print jobs stored on the printer. They can be started using 'start' command. Usage:
```
Usage: rbx [<general-options>] jobs [<specific-options>]

  General options are one of these:
  -v | --verbose   - increases voutput erbosity level
  -d | --debug     - increases debug level
  -p | --printer   - if more than one printer is connected to your
                     computer you must select which one command is
                     going to be applied on. You can get list of
                     available printers using 'list' command

  Specific options are:

  -h | --help | -?     - this page
```

Send
----

Sends gcode (file) to the printer as a print job. It is, then stored for later use. Job can be started using 'start' comamnd or -p/--initiate-print option. Usage:
```
Usage: rbx [<general-options>] send [<specific-options>]

  General options are one of these:
  -v | --verbose   - increases voutput erbosity level
  -d | --debug     - increases debug level
  -p | --printer   - if more than one printer is connected to your
                     computer you must select which one command is
                     going to be applied on. You can get list of
                     available printers using 'list' command

  Specific options are:

  -h | --help | -?     - this page
  -f | --file          - gcode file. Mandatory option.
  -id | --print-job-id - job id. If not specified random one
                         is going to be generated.
  -p | --initiate-print - print is going to be started as well,
                          like start command is invoked.
```

Start
-----
Starts existing job on the printer. Use 'jobs' command to see what jobs are available. Usage:
```
Usage: rbx [<general-options>] start [<specific-options>] <print-job-id>

  General options are one of these:
  -v | --verbose   - increases voutput erbosity level
  -d | --debug     - increases debug level
  -p | --printer   - if more than one printer is connected to your
                     computer you must select which one command is
                     going to be applied on. You can get list of
                     available printers using 'list' command

  Specific options are:

  -h | --help | -?     - this page
 ```