# tools/roi_labeler.py
import cv2
import json
import numpy as np
import os
from typing import Any

WINDOW = "ROI_LABEL"


current_polygon = []
rois = []
next_slot_id = 1

PARKING_LOT_ID = "roi_3"   # 停车场唯一标识
CAMERA_ID = 1   #摄像头标识
IMAGE_PATH = "../data/images/parking_3.jpg"


def mouse_cb(event: int, x: int, y: int, flags: int, param: Any) -> None:
    global current_polygon, rois, next_slot_id

    if event == cv2.EVENT_LBUTTONDOWN:
        current_polygon.append([int(x), int(y)])
        print("点：", (x, y))

    elif event == cv2.EVENT_RBUTTONDOWN:
        if len(current_polygon) >= 3:
            rois.append({
                "slot_id": next_slot_id,
                "camera_id": CAMERA_ID,
                "polygon": current_polygon.copy()
            })
            print(f"保存 ROI slot_id={next_slot_id}")
            next_slot_id += 1
            current_polygon = []
        else:
            print("⚠ 至少需要 3 个点")

def draw_frame(img: np.ndarray) -> np.ndarray:
    show = img.copy()

    # 已保存 ROI
    for r in rois:
        poly = np.array(r["polygon"], dtype=np.int32)
        cv2.polylines(show, [poly], True, (0, 255, 0), 2)
        cv2.putText(
            show,
            f"slot {r['slot_id']}",
            tuple(poly[0]),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.6,
            (0, 255, 0),
            2
        )

    # 正在绘制的 polygon
    if len(current_polygon) >= 2:
        cur = np.array(current_polygon, dtype=np.int32)
        cv2.polylines(show, [cur], False, (0, 0, 255), 2)

    return show

def save_to_file(img: np.ndarray) -> None:
    h, w = img.shape[:2]
    out = {
        "parking_lot_id": PARKING_LOT_ID,
        "camera_id": CAMERA_ID,
        "image_size": [w, h],
        "slots": rois
    }

    os.makedirs("../data/roi", exist_ok=True)
    path = f"../data/roi/{PARKING_LOT_ID}_camera_{CAMERA_ID}.json"
    with open(path, "w", encoding="utf-8") as f:
        json.dump(out, f, ensure_ascii=False, indent=2)

    print(f"✅ ROI 已保存：{path}")

def main():
    img = cv2.imread(IMAGE_PATH)
    assert img is not None, "❌ 图片加载失败"

    cv2.namedWindow(WINDOW)
    cv2.setMouseCallback(WINDOW, mouse_cb)

    while True:
        show = draw_frame(img)
        cv2.imshow(WINDOW, show)
        k = cv2.waitKey(10) & 0xFF

        if k == 27:      # ESC 退出并保存
            save_to_file(img)
            break
        elif k == ord('c'):
            current_polygon.clear()
            print("清除当前绘制")

    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()
