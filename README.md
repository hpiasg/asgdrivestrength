ASGdrivestrength
----------------

ASGdrivestrength is a tool to select gate drive strengths in verilog netlists.

### Installation ###

Download and unpack the appropriate package for your operating system. All external tools needed for operation are included in the package. You don't have to install anything or make changes to environment variables. To run it you will need a Java runtime environment (JRE) v1.8 (or later).

### Usage ###

For the following example commands it is assumed that your current working directory is the ASGdrivestrength main directory.
If you want to run ASGdrivestrength from another directory you have to add the path to the ASGdrivestrength main directory in front of the following commands (or you could add the `bin/` directory to your `PATH` variable).

#### Runner ####

To run a graphical tool featuring input masks for all important command line arguments execute

    bin/ASGdrivestrength_run

#### List of supported arguments ####

To see a list of supported command line arguments execute

    bin/ASGdrivestrength

#### Default operation ####

	bin/ASGdrivestrength -lib tech/techname_liberty.lib -cellInfoJson tech/techname_addInfo.json infile.v

The `-lib` option expects the technology file in the Liberty format.
The `-cellInfoJson` option expects a JSON file providing information on the CMOS stage counts of the library’s cells and their fanout factors, which represent different drive strength sizes. An example file for a cell library with AND and NAND cells could be:

```
{"defaultStageCounts":
	{"ASG_AND2":     {"A2": 2, "A1": 2},
	"ASG_NAND2":     {"A2": 1, "A1": 1}},
"drivestrengthFanoutFactors":
	{"ASG_AND2_0P5": 0.5,
	"ASG_AND2_1": 1,
	"ASG_NAND2_0P5": 0.5,
	"ASG_NAND2_1": 1,
	"ASG_NAND2_2": 2}}
```

You can create and install technologies with [ASGtechMngr](https://github.com/hpiasg/asgtechmngr).

The command will print the sized netlist on the console. By setting the `-out outfile.v` option, ASGdrivestrength will export the verilog code to `outfile.v`.

#### Optimizers ####

`-optimizer <optimizer>`

* SA  (default) : Simulated Annealing optimizer, minimizing a locally-estimated cost function comprising speed, power and energy consumption
	* `-optimizeDelayFactor <val>`: Factor for optimisation towards delay
	* `-optimizeEnergyFactor <val>`: Factor for optimisation towards energy consumption
	* `-optimizePowerFactor <val>`: Factor for optimisation towards power consumption
* NOP : No optimizer, use default drive strengths (fanout factor 1)
* TOP : Use largest-available size for each cell
* BOT : Use smallest-available size for each cell
* SFL : Select fastest available cell size for each cell’s current output load, iteratively.
* ESE : Iteratively equalize stage-efforts borne by each cell stage (target effort: global average)
* NSE : Iteratively equalize stage-efforts borne by each cell stage (target effort: neighbor average)
* EDM (only for all-single-stage-cell circuits) : equal-delay matrix optimization as [proposed by Ebergen et al.](http://ieeexplore.ieee.org/abstract/document/1299287/)
* FO : Select cell size by fan-out factor for its number of successors

#### Constraints ####

`-inputDrivenMaxCIn` : limit in pF for the input-pin capacitance of cells driven by the circuit’s input pins.
`-outputPinCapacitance` : specified load capacitance in pF for each output-pin of the circuit
`-outSdc` : write an sdc file containing the output loads for further processing

### Build instructions ###

To build ASGdrivestrength, Apache Maven v3.1.1 (or later) and the Java Development Kit (JDK) v1.8 (or later) are required.

1. Build [ASGcommon v3](https://github.com/hpiasg/asgcommon/tree/main-v3)
2. Execute `mvn clean install -DskipTests`
