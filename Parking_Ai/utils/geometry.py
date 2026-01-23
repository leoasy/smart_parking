# utils/geometry.py
from typing import List, Tuple, Union, Iterable
import math

Point = Tuple[float, float]
PolyIn = Union[List[Point], List[dict], List[List[float]]]


def _to_float(v) -> float:
    try:
        return float(v)
    except Exception:
        return 0.0


def normalize_polygon(polygon: PolyIn) -> List[Point]:
    """
    统一将 polygon 转为 List[ (x,y) ].
    支持输入格式：
      - [(x,y), ...]
      - [[x,y], ...]
      - [{'x':..,'y':..}, ...] 或 [{'cx':..,'cy':..}, ...]
      - strings like "100,200" -> parsed
    返回值点为 float 类型。
    """
    out: List[Point] = []
    if polygon is None:
        return out
    for p in polygon:
        if isinstance(p, dict):
            # common keys
            x = p.get("x", p.get("cx", p.get(0, None)))
            y = p.get("y", p.get("cy", p.get(1, None)))
            out.append((_to_float(x), _to_float(y)))
        elif isinstance(p, (list, tuple)):
            if len(p) >= 2:
                out.append((_to_float(p[0]), _to_float(p[1])))
        else:
            # try parse "x,y"
            try:
                s = str(p)
                a, b = s.split(",")
                out.append((_to_float(a), _to_float(b)))
            except Exception:
                continue
    return out


def point_in_polygon(point: Point, polygon: PolyIn) -> bool:
    """
    射线法判断点是否在多边形内。
    注意：点在边或顶点时视为 True（即包含边界）。
    """
    poly = normalize_polygon(polygon)
    if not poly or len(poly) < 3:
        return False

    x, y = point
    inside = False
    n = len(poly)
    for i in range(n):
        xi, yi = poly[i]
        xj, yj = poly[(i - 1) % n]
        # 检查是否在顶点或边上（包含边界）
        # 对边的投影判断
        if (_on_segment((xi, yi), (xj, yj), (x, y))):
            return True
        intersect = ((yi > y) != (yj > y)) and (
            x < (xj - xi) * (y - yi) / ((yj - yi) if (yj - yi) != 0 else 1e-9) + xi
        )
        if intersect:
            inside = not inside
    return inside


def _on_segment(a: Point, b: Point, p: Point) -> bool:
    """
    判断点 p 是否在线段 ab 上（包含端点）。使用向量积和范围判断。
    """
    (x1, y1), (x2, y2) = a, b
    (x, y) = p
    # collinear: cross product == 0
    cross = (x - x1) * (y2 - y1) - (y - y1) * (x2 - x1)
    if abs(cross) > 1e-6:
        return False
    # check within bounding box
    minx, maxx = min(x1, x2) - 1e-6, max(x1, x2) + 1e-6
    miny, maxy = min(y1, y2) - 1e-6, max(y1, y2) + 1e-6
    return (minx <= x <= maxx) and (miny <= y <= maxy)


def polygon_bbox(polygon: PolyIn) -> Tuple[float, float, float, float]:
    """
    返回 polygon 的外接 bbox: (minx, miny, maxx, maxy)
    空 polygon 返回 (0,0,0,0)
    """
    poly = normalize_polygon(polygon)
    if not poly:
        return (0.0, 0.0, 0.0, 0.0)
    xs = [p[0] for p in poly]
    ys = [p[1] for p in poly]
    return (min(xs), min(ys), max(xs), max(ys))


def centroid_of_polygon(polygon: PolyIn) -> Point:
    """
    多边形质心（当作点集合的均值）
    对于凹多边形/非均匀顶点并不等于几何质心，但在画标注/文本时够用。
    """
    poly = normalize_polygon(polygon)
    if not poly:
        return (0.0, 0.0)
    x = sum(p[0] for p in poly) / len(poly)
    y = sum(p[1] for p in poly) / len(poly)
    return (x, y)


def center_of_bbox(bbox: Iterable[float]) -> Point:
    """
    bbox: (x1, y1, x2, y2)
    返回中心 (cx, cy)
    """
    x1, y1, x2, y2 = bbox
    return ((x1 + x2) / 2.0, (y1 + y2) / 2.0)


def bbox_iou(boxA: Iterable[float], boxB: Iterable[float]) -> float:
    """
    计算两个 bbox 的 IoU（box = [x1,y1,x2,y2]）
    返回 0.0 ~ 1.0
    """
    ax1, ay1, ax2, ay2 = map(float, boxA)
    bx1, by1, bx2, by2 = map(float, boxB)

    xA = max(min(ax1, ax2), min(bx1, bx2))
    yA = max(min(ay1, ay2), min(by1, by2))
    xB = min(max(ax1, ax2), max(bx1, bx2))
    yB = min(max(ay1, ay2), max(by1, by2))

    interW = max(0.0, xB - xA)
    interH = max(0.0, yB - yA)
    interArea = interW * interH

    boxAArea = max(0.0, abs(ax2 - ax1) * abs(ay2 - ay1))
    boxBArea = max(0.0, abs(bx2 - bx1) * abs(by2 - by1))
    denom = boxAArea + boxBArea - interArea
    if denom <= 0.0:
        return 0.0
    return interArea / denom


def polygon_area(polygon: PolyIn) -> float:
    """
    多边形面积（绝对值）。使用叉积（Shoelace formula）。
    """
    poly = normalize_polygon(polygon)
    if len(poly) < 3:
        return 0.0
    area = 0.0
    n = len(poly)
    for i in range(n):
        x1, y1 = poly[i]
        x2, y2 = poly[(i + 1) % n]
        area += x1 * y2 - x2 * y1
    return abs(area) / 2.0


def is_clockwise(polygon: PolyIn) -> bool:
    """
    判断多边形点序是顺时针还是逆时针（基于 signed area）
    返回 True 表示顺时针（clockwise）
    """
    poly = normalize_polygon(polygon)
    if len(poly) < 3:
        return True
    # 有向面积
    s = 0.0
    n = len(poly)
    for i in range(n):
        x1, y1 = poly[i]
        x2, y2 = poly[(i + 1) % n]
        s += (x2 - x1) * (y2 + y1)
    # 若 s > 0 则通常为顺时针（具体定义与实现有关）
    return s > 0


def scale_polygon(polygon: PolyIn, sx: float, sy: float) -> List[Point]:
    """
    对 polygon 做缩放（用于不同分辨率之间的映射）
    """
    poly = normalize_polygon(polygon)
    return [(x * sx, y * sy) for x, y in poly]


def clip_bbox_to_image(bbox: Iterable[float], image_size: Tuple[int, int]) -> Tuple[int, int, int, int]:
    """
    将 bbox 限制在图像范围内，返回 int bbox
    """
    x1, y1, x2, y2 = map(float, bbox)
    w, h = image_size
    x1 = max(0, min(w - 1, x1))
    x2 = max(0, min(w - 1, x2))
    y1 = max(0, min(h - 1, y1))
    y2 = max(0, min(h - 1, y2))
    # 保证 x1<=x2, y1<=y2
    x1, x2 = min(x1, x2), max(x1, x2)
    y1, y2 = min(y1, y2), max(y1, y2)
    return (int(round(x1)), int(round(y1)), int(round(x2)), int(round(y2)))
