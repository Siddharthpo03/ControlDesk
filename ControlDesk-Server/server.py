import asyncio
import websockets
import json
from pynput.mouse import Button, Controller as MouseController
from pynput.keyboard import Key, Controller as KeyboardController
import subprocess

mouse = MouseController()
keyboard = KeyboardController()

async def handle(websocket):
    print("Phone connected!")
    async for message in websocket:
        try:
            data = json.loads(message)
            action = data.get("action")

            if action == "MOUSE_MOVE":
                mouse.move(data.get("dx", 0), data.get("dy", 0))

            elif action == "LEFT_CLICK":
                mouse.click(Button.left)

            elif action == "RIGHT_CLICK":
                mouse.click(Button.right)

            elif action == "MIDDLE_CLICK":
                mouse.click(Button.middle)

            elif action == "DOUBLE_CLICK":
                mouse.click(Button.left, 2)

            elif action == "SCROLL":
                mouse.scroll(0, data.get("dy", 0))

            elif action == "SCROLL_H":
                mouse.scroll(data.get("dx", 0), 0)

            elif action == "ZOOM_IN":
                keyboard.press(Key.ctrl)
                mouse.scroll(0, 1)
                keyboard.release(Key.ctrl)

            elif action == "ZOOM_OUT":
                keyboard.press(Key.ctrl)
                mouse.scroll(0, -1)
                keyboard.release(Key.ctrl)

            elif action == "DRAG_START":
                mouse.press(Button.left)

            elif action == "DRAG_END":
                mouse.release(Button.left)

            # 3 finger swipes — Windows shortcuts
            elif action == "SWIPE_3_UP":
                keyboard.press(Key.cmd)
                keyboard.press(Key.tab)
                keyboard.release(Key.tab)
                keyboard.release(Key.cmd)

            elif action == "SWIPE_3_DOWN":
                keyboard.press(Key.cmd)
                keyboard.press(Key.d)
                keyboard.release(Key.d)
                keyboard.release(Key.cmd)

            elif action == "SWIPE_3_LEFT":
                keyboard.press(Key.cmd)
                keyboard.press(Key.ctrl)
                keyboard.press(Key.left)
                keyboard.release(Key.left)
                keyboard.release(Key.ctrl)
                keyboard.release(Key.cmd)

            elif action == "SWIPE_3_RIGHT":
                keyboard.press(Key.cmd)
                keyboard.press(Key.ctrl)
                keyboard.press(Key.right)
                keyboard.release(Key.right)
                keyboard.release(Key.ctrl)
                keyboard.release(Key.cmd)

            # 4 finger gestures
            elif action == "SWIPE_4_UP":
                keyboard.press(Key.cmd)
                keyboard.press(Key.tab)
                keyboard.release(Key.tab)
                keyboard.release(Key.cmd)

            elif action == "SWIPE_4_DOWN":
                keyboard.press(Key.cmd)
                keyboard.press(Key.ctrl)
                keyboard.press(Key.f4)
                keyboard.release(Key.f4)
                keyboard.release(Key.ctrl)
                keyboard.release(Key.cmd)

            elif action == "TAP_4":
                keyboard.press(Key.cmd)
                keyboard.press(Key.a)
                keyboard.release(Key.a)
                keyboard.release(Key.cmd)

            # Media controls
            elif action == "MEDIA_PLAY_PAUSE":
                keyboard.press(Key.media_play_pause)
                keyboard.release(Key.media_play_pause)

            elif action == "MEDIA_NEXT":
                keyboard.press(Key.media_next)
                keyboard.release(Key.media_next)

            elif action == "MEDIA_PREV":
                keyboard.press(Key.media_previous)
                keyboard.release(Key.media_previous)

            elif action == "VOLUME_UP":
                keyboard.press(Key.media_volume_up)
                keyboard.release(Key.media_volume_up)

            elif action == "VOLUME_DOWN":
                keyboard.press(Key.media_volume_down)
                keyboard.release(Key.media_volume_down)

            elif action == "KEY_PRESS":
                keyboard.type(data.get("key", ""))

        except Exception as e:
            print(f"Error: {e}")

async def main():
    print("ControlDesk Server starting...")
    async with websockets.serve(handle, "0.0.0.0", 5000):
        print("Server running on port 5000. Waiting for phone...")
        await asyncio.Future()

asyncio.run(main())