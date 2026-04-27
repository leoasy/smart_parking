"""
Prometheus metrics for Parking AI Service.
"""
from prometheus_client import Counter, Histogram, Gauge, Info

# === Info ===
SERVICE_INFO = Info("parking_ai_service", "Parking AI Service information")
SERVICE_INFO.info({"version": "1.0.0", "service": "parking-ai"})

# === Counters ===
DETECT_IMAGE_TOTAL = Counter(
    "parking_ai_detect_image_total",
    "Total number of /parking/detect/image requests",
    ["parking_lot_id", "camera_id"]
)

DETECT_IMAGE_ERRORS = Counter(
    "parking_ai_detect_image_errors_total",
    "Total number of errors in /parking/detect/image",
    ["parking_lot_id", "camera_id", "error_type"]
)

RUOYI_PUSH_TOTAL = Counter(
    "parking_ai_ruoyi_push_total",
    "Total number of push attempts to Ruoyi",
    ["parking_lot_id", "camera_id", "status"]
)

# === Histograms ===
DETECT_IMAGE_DURATION = Histogram(
    "parking_ai_detect_image_duration_seconds",
    "Duration of /parking/detect/image processing",
    ["parking_lot_id", "camera_id"],
    buckets=[0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0]
)

RUOYI_PUSH_DURATION = Histogram(
    "parking_ai_ruoyi_push_duration_seconds",
    "Duration of push requests to Ruoyi",
    ["parking_lot_id", "camera_id"],
    buckets=[0.1, 0.25, 0.5, 1.0, 2.0, 5.0]
)

REQUEST_DURATION = Histogram(
    "parking_ai_request_duration_seconds",
    "HTTP request duration by method and path",
    ["method", "path", "status_code"],
    buckets=[0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0]
)

# === Gauges ===
PARKING_LOT_GAUGE = Gauge(
    "parking_ai_parking_lots_loaded",
    "Number of parking lots currently loaded in engine"
)

SLOTS_GAUGE = Gauge(
    "parking_ai_slots_detected",
    "Number of parking slots detected in last detection",
    ["parking_lot_id", "camera_id"]
)

ACTIVE_REQUESTS = Gauge(
    "parking_ai_active_requests",
    "Number of currently active requests"
)

# === Helper functions ===
def record_detect_success(parking_lot_id: str, camera_id: int, duration: float, slots_count: int):
    DETECT_IMAGE_TOTAL.labels(parking_lot_id=parking_lot_id, camera_id=str(camera_id)).inc()
    DETECT_IMAGE_DURATION.labels(parking_lot_id=parking_lot_id, camera_id=str(camera_id)).observe(duration)
    SLOTS_GAUGE.labels(parking_lot_id=parking_lot_id, camera_id=str(camera_id)).set(slots_count)

def record_detect_error(parking_lot_id: str, camera_id: int, error_type: str):
    DETECT_IMAGE_ERRORS.labels(parking_lot_id=parking_lot_id, camera_id=str(camera_id), error_type=error_type).inc()

def record_ruoyi_push(parking_lot_id: str, camera_id: int, status: str, duration: float):
    RUOYI_PUSH_TOTAL.labels(parking_lot_id=parking_lot_id, camera_id=str(camera_id), status=status).inc()
    if status == "success":
        RUOYI_PUSH_DURATION.labels(parking_lot_id=parking_lot_id, camera_id=str(camera_id)).observe(duration)

def set_parking_lots_loaded(count: int):
    PARKING_LOT_GAUGE.set(count)
