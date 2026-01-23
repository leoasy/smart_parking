# core/config.py
from dataclasses import dataclass, field
from pathlib import Path
from typing import List, Dict, Any, Tuple
import yaml

DEFAULT_CONFIG_PATH = Path("config/config.yaml")

# typed dataclasses for clarity and auto-completion
@dataclass
class ModelConfig:
    path: str = "model/parking.pt"

@dataclass
class DetectorConfig:
    img_size: Tuple[int, int] = (640, 384)
    conf_threshold: float = 0.25
    iou_threshold: float = 0.45

@dataclass
class MatcherConfig:
    method: str = "center"   # "center" or "iou"
    iou_threshold: float = 0.2
    center_margin: float = 0.5

@dataclass
class StabilityConfig:
    win: int = 3

@dataclass
class VisualizeConfig:
    enable: bool = True
    save_dir: str = "data/outputs"

@dataclass
class LoggingConfig:
    level: str = "INFO"

@dataclass
class Config:
    model: ModelConfig = field(default_factory=ModelConfig)
    detector: DetectorConfig = field(default_factory=DetectorConfig)
    matcher: MatcherConfig = field(default_factory=MatcherConfig)
    stability: StabilityConfig = field(default_factory=StabilityConfig)
    visualize: VisualizeConfig = field(default_factory=VisualizeConfig)
    logging: LoggingConfig = field(default_factory=LoggingConfig)
    raw: Dict[str, Any] = field(default_factory=dict)  # preserve raw for debug

def _to_tuple_int(seq) -> Tuple[int, int]:
    if not isinstance(seq, (list, tuple)):
        return (640, 384)
    try:
        a = int(seq[0]); b = int(seq[1])
        return (a, b)
    except Exception:
        return (640, 384)

def load_config(path: Path = None) -> Config:
    """
    Load configuration from YAML and return Config dataclass.
    If file missing or malformed, this function raises an informative exception.
    """
    cfg_path = path or DEFAULT_CONFIG_PATH
    if not Path(cfg_path).exists():
        raise FileNotFoundError(f"配置文件没有找到: {cfg_path}")

    with open(cfg_path, "r", encoding="utf-8") as f:
        raw = yaml.safe_load(f) or {}

    # model
    model_p = raw.get("model", {}).get("path") or raw.get("model", {}).get("model_path") or "model/parking.pt"
    model = ModelConfig(path=str(model_p))

    # detector
    det = raw.get("detector", {})
    img_size = _to_tuple_int(det.get("img_size") or det.get("image_size") or (640, 384))
    conf_threshold = float(det.get("conf_threshold", det.get("conf_threshold", 0.25)))
    iou_threshold = float(det.get("iou_threshold", det.get("iou_threshold", 0.45)))
    detector = DetectorConfig(img_size=img_size, conf_threshold=conf_threshold, iou_threshold=iou_threshold)

    # matcher
    matcher_raw = raw.get("matcher", {})
    matcher = MatcherConfig(
        method=str(matcher_raw.get("method", "center")),
        iou_threshold=float(matcher_raw.get("iou_threshold", 0.2)),
        center_margin=float(matcher_raw.get("center_margin", 0.5))
    )

    # stability
    stability_raw = raw.get("stability", {})
    stability = StabilityConfig(win=int(stability_raw.get("win", 3)))

    # visualize
    viz_raw = raw.get("visualize", {})
    visualize = VisualizeConfig(
        enable=bool(viz_raw.get("enable", True)),
        save_dir=str(viz_raw.get("save_dir", "data/outputs"))
    )

    # logging
    logging_raw = raw.get("logging", {})
    logging = LoggingConfig(level=str(logging_raw.get("level", "INFO")))

    config = Config(
        model=model,
        detector=detector,
        matcher=matcher,
        stability=stability,
        visualize=visualize,
        logging=logging,
        raw=raw
    )

    # basic validations
    if config.detector.conf_threshold <= 0 or config.detector.conf_threshold >= 1:
        raise ValueError("detector.conf_threshold should be in (0,1)")
    if config.stability.win <= 0:
        raise ValueError("stability.win should be a positive integer")
    if config.matcher.method not in ("center", "iou"):
        raise ValueError("matcher.method must be 'center' or 'iou'")

    return config

PARKING_LOT_CODE_MAP = {
    "roi_1": "停车场A",
    "roi_2": "停车场B",
    "roi_3": "停车场C",
}

# 停车场 → 摄像头编号 → camera_name
CAMERA_NAME_MAP = {
    "roi_1": {
        1: "停车场A-广角"
    },
    "roi_2": {
        1: "停车场B-广角"
    },
    "roi_3": {
        1: "停车场C-广角"
    }
}


