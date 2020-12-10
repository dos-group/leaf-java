# LEAF 🌱

A simulator for **L**arge **E**nergy-**A**ware **F**og computing environments.
LEAF enables energy consumption modeling of distributed, heterogeneous, and resource-constrained infrastructure that executes complex application graphs.

Features include:

- **Power modeling**: Model the power usage of individual compute nodes, network traffic and applications
- **Energy-aware algorithms**: Implement dynamically adapting task placement strategies, routing policies, and other energy-saving mechanisms
- **Dynamic networks**: Nodes can be mobile and can join or leave the network during the simulation
- **Scalability**: Simulate thousands of devices and applications in magnitudes faster than real time
- **Live visualization**: Monitor your experiments at runtime
- **Exporting**: Export power usage characteristics and other results as CSV files for further analysis

<p align="center">
  <img src="/images/infrastructure.png">
</p>


### Implementation

The package `org.leaf` contains the infrastructure and application model as well as related power models.

The current implementation depends on a patched version of CloudSim Plus which can be found [here](https://github.com/birnbaum/cloudsim-plus/pull/1).
Infrastructure and application graphs are implemented through [JGraphT](https://jgrapht.org/).


### Example Scenario
The package `org.cityexperiment` implements an exemplary traffic management scenario in a smart city.
Experiments can be configured via `org.cityexperiment.Settings` and output CSV files containing all measurements.

The live visualization allows insights on running experiments:
1. A map of the city and the location of taxis
2. The number of taxis that were present on the map at a given time
3. The power usage of infrastructure components over time
4. The power usage of applications over time

<p align="center">
  <img src="/images/visualization.png">
</p>


### Analysis
The directory `analysis` contains the experiment analysis code written in Python.
For running the analysis yourself, [install conda](https://docs.conda.io/projects/conda/en/latest/user-guide/install/) adapt `settings.py` and run:

```
cd analysis
conda env create
conda activate leaf
python create_plots.py
```
