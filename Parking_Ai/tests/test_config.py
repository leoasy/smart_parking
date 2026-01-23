# tests/test_config.py
import tempfile
from pathlib import Path
import yaml
from core.config import load_config, Config

def test_load_config_minimal(tmp_path):
    # create a minimal config file
    cfg_dict = {
        "model": {"path": "model/parking.pt"},
        "detector": {"img_size": [320, 240], "conf_threshold": 0.2, "iou_threshold": 0.4},
        "matcher": {"method": "center", "iou_threshold": 0.2},
        "stability": {"win": 2},
        "visualize": {"enable": True, "save_dir": "data/outputs"},
        "logging": {"level": "DEBUG"}
    }
    p = tmp_path / "config.yaml"
    p.write_text(yaml.safe_dump(cfg_dict), encoding="utf-8")
    cfg = load_config(path=p)
    assert cfg.model.path == "model/parking.pt"
    assert cfg.detector.img_size == (320, 240)
    assert cfg.stability.win == 2
    assert cfg.logging.level == "DEBUG"
