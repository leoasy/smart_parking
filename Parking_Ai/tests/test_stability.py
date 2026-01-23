# tests/test_stability.py
from inference.stability import SlotStability


def test_basic_stability():
    stab = SlotStability(win=3)

    # frame 1
    out = stab.update({1: True})
    assert out[1] is True

    # frame 2
    out = stab.update({1: False})
    assert out[1] is True  # 平票，保持

    # frame 3
    out = stab.update({1: False})
    assert out[1] is False


def test_multi_slots():
    stab = SlotStability(win=2)

    out = stab.update({1: True, 2: False})
    assert out[1] is True
    assert out[2] is False

    out = stab.update({1: False, 2: False})
    assert out[1] is False
    assert out[2] is False


def test_reset():
    stab = SlotStability(win=2)
    stab.update({1: True})
    stab.reset()
    assert stab.get_state() == {}
