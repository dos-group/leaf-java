"""Creates plots for one or more experiment runs.

The follow plots are created:
- A bar plot comparing all experiments in the specified directory
- A plot comparing experiments Fog 4 and Fog6s
- For every experiment a detailed plot on infrastructure component and application consumption.
  Additionally a plot for both applications that illustrates the static consumption.
"""

import os
import warnings
from typing import Tuple, Dict, List

import pandas as pd

from figures import infrastructure_figure, applications_figure, cctv_application_figure, \
    stm_application_figure, comparison_plot_figure, infrastructure_barplot_figure
from settings import RESULTS_DIR, EXPERIMENTS, EXPERIMENT_TITLES


def _create_plots(df_i, df_a, path):
    tuples = [
        (infrastructure_figure(df_i), "infrastructure"),
        (applications_figure(df_a), "applications"),
        (cctv_application_figure(df_a), "application_cctv"),
        (stm_application_figure(df_a), "application_stm"),
    ]
    for fig, filename in tuples:
        fig.write_image(os.path.join(path, filename + ".pdf"))


def experiment_results() -> Dict[str, Tuple[pd.DataFrame, pd.DataFrame]]:
    result = {}
    for experiment in EXPERIMENTS:
        df_i = pd.read_csv(os.path.join(RESULTS_DIR, experiment, "infrastructure.csv"), index_col="time")
        df_a = pd.read_csv(os.path.join(RESULTS_DIR, experiment, "applications.csv"), index_col="time")
        result[experiment] = (df_i, df_a)
    return result


def create_barplot(results):
    print("Creating barplot...")
    infrastructure_barplot_figure(results).write_image(os.path.join(RESULTS_DIR, "barplot.pdf"))


def print_experiment_table(results):
    print("Experiment Results:\n")
    print(f"{''.ljust(17)}Total\tCloud\tFog (d)\tFog (s)\tWAN\tWiFI")
    print("-" * 70)
    for experiment, (df_i, _) in results.items():
        cloud = df_i["cloud dynamic"].sum() / 3600
        fog_dynamic = df_i["fog dynamic"].sum() / 3600
        fog_static = df_i["fog static"].sum() / 3600
        wifi = df_i["wifi dynamic"].sum() / 3600
        wan = df_i["wan dynamic"].sum() / 3600
        total = cloud+fog_dynamic+fog_static+wifi+wan
        print(f"{experiment.ljust(17)}{int(total)}\t{int(cloud)}\t{int(fog_dynamic)}\t{int(fog_static)}\t{int(wan)}\t{int(wifi)}")
    print()


def create_experiment_plots(results):
    for experiment, (df_i, df_a) in results.items():
        print(f"Generating plots for experiment {experiment}...")
        plots_dir = os.path.join(RESULTS_DIR, experiment, "plots")
        os.makedirs(plots_dir, exist_ok=True)
        _create_plots(df_i, df_a, path=plots_dir)


def create_comparison_plot(results, experiments_to_compare: List[str]):
    print(f"Creating comparision plot between {' and '.join(experiments_to_compare)}...")
    comparison_dfs = {EXPERIMENT_TITLES[EXPERIMENTS.index(experiment)]: results[experiment][0]
                      for experiment in results
                      if experiment in experiments_to_compare}
    comparison_plot_figure(comparison_dfs).write_image(os.path.join(RESULTS_DIR, "fog4_vs_fog6s.pdf"))


if __name__ == '__main__':
    warnings.filterwarnings("ignore")
    results = experiment_results()
    print_experiment_table(results)

    create_barplot(results)
    create_experiment_plots(results)
    create_comparison_plot(results, ["fog_4", "fog_6_shutdown5"])
