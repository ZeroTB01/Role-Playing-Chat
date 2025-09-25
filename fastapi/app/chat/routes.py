
from fastapi import APIRouter
from pydantic import BaseModel
from typing import List
from openai import OpenAI

# 百度千帆应用配置
client = OpenAI(
    api_key="bce-v3/ALTAK-um3v8vhXVtEcQDXEdp9LB/74a116123c87f6b43fa8b9d8af1a10659d37266a",  # 请替换为你的 token
    base_url="https://qianfan.baidubce.com/v2",
    default_headers={"appid": ""}
)

router = APIRouter()

# 请求体模型
class ChatContextItem(BaseModel):
    role: str
    content: str

class ChatRequest(BaseModel):
    character: str
    context: List[ChatContextItem]

@router.post("/chat")
def chat(req: ChatRequest):
    # 拼接角色设定
    role_prompt = f"你现在扮演{req.character}，请用该角色的身份和风格回复。"
    # 构造 messages 列表
    messages = [{"role": "system", "content": role_prompt}]
    for item in req.context:
        # user/ai角色分别对应user/assistant
        if item.role == "user":
            messages.append({"role": "user", "content": item.content})
        else:
            messages.append({"role": "assistant", "content": item.content})
    response = client.chat.completions.create(
        model="ernie-4.0-turbo-8k",
        messages=messages
    )
    reply = response.choices[0].message.content
    return {"reply": reply}
