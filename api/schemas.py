from pydantic import BaseModel

class Query(BaseModel):
    query: str | None

class UserDetails(BaseModel):
    user_id: str
    preferences: str | None
