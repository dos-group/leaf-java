# LEAF 🌱

A simulator for **L**arge **E**nergy-**A**ware **F**og computing environments.
LEAF enables energy consumption modeling of distributed, heterogeneous, and resource-constrained infrastructure that executes complex application graphs.

Features include:

- **Power modeling**: Model the power usage of individual compute nodes, network traffic and applications
- **Energy-aware algorithms**: Implement dynamically adapting task placement strategies, routing policies, and other energy-saving mechanisms
- **Dynamic networks**: Nodes can be mobile and can join or leave the network during the simulation
- **Scalability**: Simulate thousands of devices and applications in magnitudes faster than real time
- **Exporting**: Export power usage characteristics and other results as CSV files for further analysis

<p align="center">
  <img src="/images/infrastructure.png">
</p>


## Python Implementation

We created a new, lightweight implementation of LEAF in Python.
Although the Python version is currently less performant, we hope the cleaner interface, improved usability, 
and bigger third party library support - especially for machine learning - will lead to a greater adoption of our model.

You can find the new implementation [here](https://github.com/dos-group/leaf) and its documentation [here](https://leaf.readthedocs.io/en/latest/).



## Core

The package `org.leaf` contains the infrastructure and application model as well as related power models.

The current implementation is based on [CloudSim Plus](https://github.com/manoelcampos/cloudsim-plus),
a modern and fully documented simulation framework.
Several features and improvements developed within LEAF were directly contributed to the CloudSim Plus core and
released in [version 6](https://github.com/manoelcampos/cloudsim-plus/releases/tag/v6.0.0).

Infrastructure and application graphs are implemented through [JGraphT](https://jgrapht.org/).


### Example Scenario
The package `org.examples.smart_city_traffic` implements an exemplary traffic management scenario in a smart city.
The example contains:
- multiple custom compute nodes, network links, and applications
- a mobility model for taxis modelled after a taxi traffic dataset from the  
[2015 DEBS Grand Challenge competition](http://www.debs2015.org/call-grand-challenge.html)
- two different application placement algorithms
- a **live visualization** to monitor experiments at runtime which contains
    - a map of the city and the location of taxis
    - the number of taxis on the map over time
    - the power usage of infrastructure components over time
    - the power usage of applications over time

<img src="/images/visualization.png" width="70%">

Experiments can be configured via the `Settings` class.
To improve simulation speed increase the `LOG_LEVEL` and reduce the `VISUALIZATION_REDRAW_INTERVAL`.


### Analysis
The directory `analysis` contains the experiment analysis code written in Python.
For running the analysis yourself, [install conda](https://docs.conda.io/projects/conda/en/latest/user-guide/install/) adapt `settings.py` and run:

```
cd analysis
conda env create
conda activate leaf_analysis
python create_plots.py
```

## Publications

- Philipp Wiesner and Lauritz Thamsen. "[LEAF: Simulating Large Energy-Aware Fog Computing Environments](https://ieeexplore.ieee.org/document/9458907)" In the Proceedings of the 2021 *5th IEEE International Conference on Fog and Edge Computing (ICFEC)*, IEEE, 2021. [[arXiv preprint]](https://arxiv.org/pdf/2103.01170.pdf) [[video]](https://youtu.be/G70hudAhd5M)
