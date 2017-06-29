ASGdrivestrength
----------------

ASGdrivestrength is a tool to select gate drive strengths in verilog netlists

### Installation ###

Download and unpack the appropriate package for your operating system. All external tools needed for operation are included in the package. You don't have to install anything or make changes to environment variables. To run it you will need a Java runtime environment (JRE) v1.8 (or later).

### Usage ###

For the following example commands it is assumed that your current working directory is the ASGdrivestrength main directory. If you want run ASGdrivestrength from another directory you have to add the path to the ASGdrivestrength main directory in front of the following commands (or you could add the `bin/` directory to your `PATH` variable).

#### Runner ####

To run a graphical tool featuring input masks for all important command line arguments execute

    bin/ASGdrivestrength_run

#### List of supported arguments ####

To see a list of supported command line arguments execute

    bin/ASGdrivestrength

#### Default operation ####

	bin/ASGdrivestrength -lib tech/techname_liberty.lib -cellInfoJson tech/techname_addInfo.json infile.v

The `-lib` option expects the technology file in the Liberty format. The `-cellInfoJson` option expects a file in ??? format. You can create and install technologies with [ASGtechMngr](https://github.com/hpiasg/asgtechmngr).

The command will print the sized netlist on the console. By setting the `-out outfile.v` option, ASGdrivestrength will export the verilog code to `outfile.v`.

#### Optimizers ####

`-optimizer <optimizer>`

* SA  (default) `-optimizeEnergyPercentage`
* NOP
* TOP
* BOT
* SFL
* ESE
* NSE
* EDM (only for all-single-stage cells)
* FO

#### Constraints ####

`-outSdc`
`-inputDrivenMaxCIn`
`-outputPinCapacitance`

### Build instructions ###

To build ASGdrivestrength, Apache Maven v3 (or later) and the Java Development Kit (JDK) v1.8 (or later) are required.

1. Build [ASGcommon](https://github.com/hpiasg/asgcommon)
2. Execute `mvn clean install -DskipTests`
