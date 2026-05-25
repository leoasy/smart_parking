# main.py
import cv2
from pathlib import Path

from core.config import load_config
from core.engine import ParkingEngine


def main():
    import argparse
    parser = argparse.ArgumentParser(description="Parking occupancy detection")
    parser.add_argument("--img", type=str, required=True, help="Path to image file")
    parser.add_argument("--parking-lot-id", type=str, required=True, help="Parking lot ID")
    parser.add_argument("--camera-id", type=int, required=True, help="Camera ID")
    args = parser.parse_args()

    cfg = load_config()
    engine = ParkingEngine(cfg)

    img_path = Path(args.img)
    if not img_path.exists():
        raise FileNotFoundError(f"测试图片不存在: {img_path}")

    image = cv2.imread(str(img_path))
    if image is None:
        raise RuntimeError("图片读取失败")

    result = engine.process_image(
        image=image,
        parking_lot_id=args.parking_lot_id,
        camera_id=args.camera_id,
        visualize=True
    )

    print("=== 检测结果 ===")
    print(f"停车场: {result['parking_lot_id']}")
    print(f"摄像头: {result['camera_id']}")
    for slot in result["slots"]:
        print(
            f"车位 {slot['slot_id']}: "
            f"{'占用' if slot['occupied'] else '空闲'}"
        )

    print("\n可视化结果已保存至:", cfg.visualize.save_dir)


if __name__ == "__main__":
    main()
