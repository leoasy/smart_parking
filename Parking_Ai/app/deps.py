# app/deps.py
from functools import lru_cache

from core.config import load_config
from core.engine import ParkingEngine


@lru_cache()
def get_engine() -> ParkingEngine:
    """
    全局单例 ParkingEngine
    """
    cfg = load_config()
    return ParkingEngine(cfg)
