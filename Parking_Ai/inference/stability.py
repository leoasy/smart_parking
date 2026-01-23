# inference/stability.py
from collections import deque
from typing import Dict


class SlotStability:
    """
    滑动窗口稳定性判断：
    - 多数票胜出
    - 平票时：
        - win <= 2 ：使用最新帧
        - win >= 3 ：保持原状态
    """

    def __init__(self, win: int = 3):
        if win <= 0:
            raise ValueError("win must be a positive integer")
        self.win = win
        self._buffers: Dict[int, deque] = {}
        self._states: Dict[int, bool] = {}

    def update(self, current: Dict[int, bool]) -> Dict[int, bool]:
        for slot_id, occupied in current.items():
            if slot_id not in self._buffers:
                self._buffers[slot_id] = deque(maxlen=self.win)
                self._states[slot_id] = False

            buf = self._buffers[slot_id]
            buf.append(bool(occupied))

            true_count = sum(buf)
            false_count = len(buf) - true_count

            if true_count > false_count:
                self._states[slot_id] = True
            elif false_count > true_count:
                self._states[slot_id] = False
            else:
                # 平票策略
                if self.win <= 2:
                    self._states[slot_id] = bool(occupied)
                # win >= 3：保持原状态

        return dict(self._states)

    def reset(self):
        self._buffers.clear()
        self._states.clear()

    def get_state(self) -> Dict[int, bool]:
        return dict(self._states)

    def dump(self) -> Dict[int, list]:
        return {k: list(v) for k, v in self._buffers.items()}

    def load(self, data: Dict[int, list]):
        self._buffers.clear()
        self._states.clear()
        for slot_id, seq in data.items():
            dq = deque(seq, maxlen=self.win)
            self._buffers[slot_id] = dq
            self._states[slot_id] = sum(dq) > (len(dq) / 2)
