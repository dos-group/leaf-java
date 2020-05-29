"""Utility class for generating plots."""

from typing import Tuple, Dict

import numpy as np
import pandas as pd
import plotly.graph_objs as go
from plotly.subplots import make_subplots
from scipy import signal

from settings import COLORS, EXPERIMENT_TITLES


def base_figure(fig: go.Figure = None) -> go.Figure:
    if not fig:
        fig = go.Figure()

    fig.update_layout(template="plotly_white",
                      legend_orientation="h",
                      legend=dict(x=0, y=1.1),
                      xaxis=dict(mirror=True, ticks='outside', showline=True, linecolor="#444", linewidth=1),
                      yaxis=dict(mirror=True, ticks='outside', showline=True, linecolor="#444", linewidth=1))
    return fig


def timeline_figure(fig: go.Figure = None, yaxes_title: str = "Watt") -> go.Figure:
    fig = base_figure(fig)
    fig.update_xaxes(
        title="Time",
        ticktext=["00:00", "02:00", "04:00", "06:00", "08:00", "10:00", "12:00", "14:00", "16:00", "18:00", "20:00", "22:00", "24:00"],
        tickvals=[h * 120 * 60 for h in range(13)],
    )
    fig.update_yaxes(
        title=yaxes_title,
        rangemode="nonnegative",
    )
    return fig


def barplot_figure(fig: go.Figure = None) -> go.Figure:
    fig = base_figure(fig)
    fig.update_layout(barmode='stack')
    fig.update_yaxes(title="kWh consumed in 24h")
    return fig


def infrastructure_figure(df: pd.DataFrame) -> go.Figure:
    fig = timeline_figure()
    fig.add_trace(go.Scatter(x=df.index, y=df["cloud dynamic"], name="Cloud", line=dict(width=1, color=COLORS["cloud"])))
    if (df["fog static"] + df["fog dynamic"]).sum() > 0:
        fig.add_trace(go.Scatter(x=df.index, y=df["fog static"] + df["fog dynamic"], name="Fog", line=dict(width=1, color=COLORS["fog"])))
        fig.add_trace(go.Scatter(x=df.index, y=df["fog static"], name="Fog static", line=dict(width=1, dash="dot", color=COLORS["fog"])))
    fig.add_trace(go.Scatter(x=df.index, y=df["wan dynamic"], name="WAN", line=dict(width=1, color=COLORS["wan"])))
    fig.add_trace(go.Scatter(x=df.index, y=df["wifi dynamic"], name="WiFi", line=dict(width=1, color=COLORS["wifi"])))
    return fig


def applications_figure(df: pd.DataFrame) -> go.Figure:
    fig = timeline_figure()
    fig.add_trace(go.Scatter(x=df.index, y=df["cctv static"] + df["cctv dynamic"], name="CCTV", line=dict(width=1, color=COLORS["cctv"])))
    fig.add_trace(go.Scatter(x=df.index, y=df["stm static"] + df["stm dynamic"], name="STM", line=dict(width=1, color=COLORS["stm"])))
    return fig


def cctv_application_figure(df: pd.DataFrame) -> go.Figure:
    fig = timeline_figure()
    fig.add_trace(go.Scatter(x=df.index, y=df["cctv static"] + df["cctv dynamic"], name="CCTV", line=dict(width=1, color=COLORS["cctv"])))
    fig.add_trace(go.Scatter(x=df.index, y=df["cctv static"], name="CCTV (static)", line=dict(width=1, dash="dot", color=COLORS["cctv"])))
    return fig


def stm_application_figure(df: pd.DataFrame) -> go.Figure:
    fig = timeline_figure()
    fig.add_trace(go.Scatter(x=df.index, y=df["stm static"] + df["stm dynamic"], name="STM", line=dict(width=1, color=COLORS["stm"])))
    fig.add_trace(go.Scatter(x=df.index, y=df["stm static"], name="STM (static)", line=dict(width=1, dash="dot", color=COLORS["stm"])))
    return fig


def infrastructure_barplot_figure(result_dict: Dict[str, Tuple[pd.DataFrame, pd.DataFrame]]) -> go.Figure:
    cloud = []
    fog_static = []
    fog_dynamic = []
    wifi = []
    wan = []
    for experiment, (df_i, _) in result_dict.items():
        cloud.append(df_i["cloud dynamic"].sum() / 3600000)
        fog_static.append(df_i["fog static"].sum() / 3600000)
        fog_dynamic.append(df_i["fog dynamic"].sum() / 3600000)
        wifi.append(df_i["wifi dynamic"].sum() / 3600000)
        wan.append(df_i["wan dynamic"].sum() / 3600000)

    total = list(np.array(cloud) + np.array(fog_static) + np.array(fog_dynamic) + np.array(wifi) + np.array(wan))

    fig = barplot_figure()
    fig.add_trace(go.Bar(name='Fog static', x=EXPERIMENT_TITLES, y=fog_static, marker_color=COLORS["fog_static"]))
    fig.add_trace(go.Bar(name='Fog dynamic', x=EXPERIMENT_TITLES, y=fog_dynamic, marker_color=COLORS["fog"]))
    fig.add_trace(go.Bar(name='Cloud', x=EXPERIMENT_TITLES, y=cloud, marker_color=COLORS["cloud"]))
    fig.add_trace(go.Bar(name='WiFi', x=EXPERIMENT_TITLES, y=wifi, marker_color=COLORS["wifi"]))
    fig.add_trace(go.Bar(name='WAN', x=EXPERIMENT_TITLES, y=wan, marker_color=COLORS["wan"],
                         text=total, texttemplate='%{text:.2f}', textposition='outside'))
    # Fine tuning: Make chart a little higher than plotly suggests so the labels aren't cut off
    fig.update_yaxes(range=[0, 65])
    return fig


def comparison_plot_figure(result_dict: Dict[str, pd.DataFrame]):
    fig = timeline_figure()
    for experiment, df_i in result_dict.items():
        series = df_i.drop(columns="taxis").sum(axis=1)
        fig.add_trace(go.Scatter(x=series.index, y=signal.savgol_filter(series, 3601, 3), name=experiment, line=dict(width=1)))
    return fig


def taxi_figure(taxi_count_distribution: pd.Series, taxi_speed_distribution: pd.Series) -> go.Figure:
    y_count = taxi_count_distribution * 50  # 50 corresponds to MAX_CARS_PER_MINUTE in the experiments
    y_speed = taxi_speed_distribution * 3.6  # m/s to km/h

    # Create figure with secondary y-axis
    fig = make_subplots(specs=[[{"secondary_y": True}]])
    fig = timeline_figure(fig=fig)
    fig.add_trace(go.Scatter(x=taxi_count_distribution.index * 60, y=y_count,
                             name="Taxis generated per minute (left scale)", line=dict(width=1)), secondary_y=False)
    fig.add_trace(go.Scatter(x=taxi_count_distribution.index * 60, y=y_speed,
                             name="Driving speed (right scale)", line=dict(width=1)), secondary_y=True)

    fig.update_yaxes(title_text="Taxis generated per minute", secondary_y=False)
    fig.update_yaxes(title_text="Driving speed (km/h)", secondary_y=True)
    fig.update_xaxes(range=[0, 86400])
    return fig
