# tests/test_geometry.py
from utils.geometry import normalize_polygon, point_in_polygon, polygon_bbox, bbox_iou, polygon_area, scale_polygon

def test_normalize_and_bbox():
    poly = normalize_polygon([[0,0], [10,0], [10,10]])
    assert len(poly) == 3
    assert polygon_bbox(poly) == (0.0, 0.0, 10.0, 10.0)

def test_point_in_polygon():
    poly = [(0,0),(10,0),(10,10),(0,10)]
    assert point_in_polygon((5,5), poly)
    assert not point_in_polygon((15,5), poly)
    # on edge
    assert point_in_polygon((0,0), poly)

def test_bbox_iou():
    a = (0,0,10,10)
    b = (5,5,15,15)
    iou = bbox_iou(a,b)
    assert abs(iou - (25.0 / (100+100-25))) < 1e-6

def test_scale_polygon():
    poly = [(1,1),(2,1),(2,2)]
    scaled = scale_polygon(poly, 2.0, 3.0)
    assert scaled[0] == (2.0, 3.0)
