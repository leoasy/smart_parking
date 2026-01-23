# main.py
import cv2
from pathlib import Path

from core.config import load_config
from core.engine import ParkingEngine


def main():
    # ---------- 1️⃣ 加载配置 ----------
    cfg = load_config()

    # ---------- 2️⃣ 初始化引擎 ----------
    engine = ParkingEngine(cfg)

    # ---------- 3️⃣ 读取测试图片 ----------
    img_path = Path("data/images/test.jpg")
    if not img_path.exists():
        raise FileNotFoundError(f"测试图片不存在: {img_path}")

    image = cv2.imread(str(img_path))
    if image is None:
        raise RuntimeError("图片读取失败")

    # ---------- 4️⃣ 指定测试 ROI ----------
    # ⚠️ 这里一定要与你的 roi json 对应
    parking_lot_id = "roi_1"
    camera_id = 1

    # ---------- 5️⃣ 执行检测 ----------
    result = engine.process_image(
        image=image,
        parking_lot_id=parking_lot_id,
        camera_id=camera_id,
        visualize=True
    )

    # ---------- 6️⃣ 打印结果 ----------
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
