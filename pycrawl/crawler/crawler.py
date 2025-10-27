from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, validator
from typing import Optional, Dict
from crawl4ai import AsyncWebCrawler

app = FastAPI()

class Field(BaseModel):
    selector: str
    attr: Optional[str] = None

class SelectorSchema(BaseModel):
    list: Optional[str] = None
    fields: Dict[str, Field]

    @validator("fields", pre=True)
    def normalize_fields(cls, values):
        normalized = {}
        for key, value in (values or {}).items():
            if isinstance(value, dict):
                normalized[key] = value
            else:
                normalized[key] = {"selector": str(value)}
        return normalized

class CrawlRequest(BaseModel):
    url: str
    schema: Optional[SelectorSchema] = None

@app.post("/crawl")
async def crawl(req: CrawlRequest):
    try:
        async with AsyncWebCrawler() as crawler:
            r = await crawler.arun(url=req.url)

        out = {
            "url": req.url,
            "status": "ok",
            "title": getattr(r, "title", None),
            "markdown": getattr(r, "markdown", None),
            "metadata": getattr(r, "metadata", {}),
            "extracted": None
        }

        if req.schema:
            schema_dict = req.schema.fields
            def get_val(node, rule: Field):
                return node.get(rule.attr) if rule.attr else node.text()

            if req.schema.list:
                nodes = r.select(req.schema.list) or []
                items = []
                for n in nodes:
                    row = {}
                    for k, rule_dict in schema_dict.items():
                        rule = Field(**rule_dict)
                        el = n.select_one(rule.selector)
                        row[k] = get_val(el, rule) if el else None
                    items.append(row)
                out["extracted"] = {"items": items}
            else:
                row = {}
                for k, rule_dict in schema_dict.items():
                    rule = Field(**rule_dict)
                    el = r.select_one(rule.selector)
                    row[k] = get_val(el, rule) if el else None
                out["extracted"] = row

        return out
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
