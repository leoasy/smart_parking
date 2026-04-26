from core.engine import ParkingEngine
from core.config import (
    Config,
    ModelConfig,
    DetectorConfig,
    MatcherConfig,
    StabilityConfig,
    VisualizeConfig,
    LoggingConfig,
)


class _DummyDetector:
    def detect(self, image):
        return []


def _build_cfg():
    return Config(
        model=ModelConfig(path="model/parking.pt"),
        detector=DetectorConfig(img_size=(320, 240), conf_threshold=0.25, iou_threshold=0.45),
        matcher=MatcherConfig(method="center", center_margin=0.5, iou_threshold=0.2),
        stability=StabilityConfig(win=2),
        visualize=VisualizeConfig(enable=False, save_dir="data/outputs"),
        logging=LoggingConfig(level="INFO"),
    )


def test_engine_get_current_status(monkeypatch):
    monkeypatch.setattr(
        "core.engine.load_roi_dir_grouped",
        lambda _: {
            "roi_1": {
                1: type(
                    "C",
                    (),
                    {
                        "slots": [
                            type(
                                "S",
                                (),
                                {"slot_id": 1, "slot_code": "roi_1-1", "polygon": [(0, 0), (1, 0), (1, 1)]},
                            )()
                        ]
                    },
                )()
            }
        },
    )

    engine = ParkingEngine(_build_cfg())
    engine.detector = _DummyDetector()
    engine.process_image(image=[[0]], parking_lot_id="roi_1", camera_id=1, visualize=False)
    status = engine.get_current_status()

    assert "snapshots" in status
    assert len(status["snapshots"]) == 1
    assert status["snapshots"][0]["parking_lot_id"] == "roi_1"
