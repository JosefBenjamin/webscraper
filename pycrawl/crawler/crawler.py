from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, Dict
from crawl4ai import AsyncWebCrawler

app = FastAPI()

class Field(BaseModel):
    selector: str
    attr: Optional[str] = None

class SelectorSchema(BaseModel):
    list: Optional[str] = None
    fields: Dict[str, Field]

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
            def get_val(node, rule: Field):
                return node.get(rule.attr) if rule.attr else node.text()

            if req.schema.list:
                nodes = r.select(req.schema.list) or []
                items = []
                for n in nodes:
                    row = {}
                    for k, rule in req.schema.fields.items():
                        el = n.select_one(rule.selector)
                        row[k] = get_val(el, rule) if el else None
                    items.append(row)
                out["extracted"] = {"items": items}
            else:
                row = {}
                for k, rule in req.schema.fields.items():
                    el = r.select_one(rule.selector)
                    row[k] = get_val(el, rule) if el else None
                out["extracted"] = row

        return out
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
