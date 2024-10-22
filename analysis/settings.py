"""Settings for generating plots."""

SOURCE_DIR = "../results"
RESULTS_DIR = "../results"
EXPERIMENTS = ["cloud_only_0", "fog_1_0", "fog_2_0", "fog_3_0", "fog_4_0", "fog_5_0", "fog_6_0", "fog_6_shutdown5_0"]
EXPERIMENT_TITLES = ["Cloud only", "Fog 1", "Fog 2", "Fog 3", "Fog 4", "Fog 5", "Fog 6", "Fog 6s"]

COLORS = {
    "cloud": "#34495e",  # '#e48064',
    "fog": '#e74c3c',
    "fog_static": '#c0392b',
    "wan": '#3498db',
    "wifi": '#2ecc71',
    "cctv": '#0c6f68',
    "v2i": '#c9bc02',
}