from typing import Dict, Any

from core.handle.textMessageHandler import TextMessageHandler
from core.handle.textMessageType import TextMessageType
from core.handle.sendAudioHandle import send_stt_message, send_tts_message
from core.providers.tts.dto.dto import ContentType


class ReadTextMessageHandler(TextMessageHandler):
    """Read消息处理器"""

    @property
    def message_type(self) -> TextMessageType:
        return TextMessageType.READ

    async def handle(self, conn, msg_json: Dict[str, Any]) -> None:
        if "text" in msg_json:
            try:
                await send_stt_message(conn, msg_json["text"])
                conn.tts.tts_one_sentence(conn, ContentType.TEXT, content_detail=msg_json["text"])
            finally:
                await send_tts_message(conn, "stop", None)