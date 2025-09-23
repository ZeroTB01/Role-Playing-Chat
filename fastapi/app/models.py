from pydantic import BaseModel

class Role(BaseModel):
    id: int
    name: str
    desc: str

class ChatRequest(BaseModel):
    role_id: int
    message: str
