"""Genera UML editable y SVG/HTML sin dependencias externas (Python 3.10+).

Las clases se extraen de src. Las interacciones se trazaron manualmente en
secuencias.py y se validan contra las firmas extraídas. No lee diagramas previos.
"""
from __future__ import annotations

import hashlib
import html
import json
import math
import re
import subprocess
import textwrap
import uuid
import zipfile
from pathlib import Path

from secuencias import SEQUENCES

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[1]
SRC = ROOT / "src"
VIS = {"public": "+", "private": "-", "protected": "#", "package": "~"}
COLORS = {"consola": "#e0f2fe", "controlador": "#e0e7ff", "modelo": "#dcfce7", "extra": "#fef3c7"}
NS = uuid.UUID("8a2862d3-c4c1-416e-a50e-87733e62939e")


def uid(key):
    return "PC" + uuid.uuid5(NS, str(key)).hex


def ref(obj):
    return {"$ref": obj["_id"] if isinstance(obj, dict) else obj}


def obj(kind, key, parent=None, **kw):
    d = {"_type": kind, "_id": uid(key)}
    if parent is not None:
        d["_parent"] = ref(parent)
    d.update(kw)
    return d


def view(kind, key, parent, model=None, **kw):
    style = dict(font="Arial;14;0", lineColor="#334155",
                 fontColor="#0f172a", fillColor="#ffffff", parentStyle=False)
    style.update(kw)
    d = obj(kind, key, parent, **style)
    if model is not None:
        d["model"] = ref(model)
    return d


def label(parent, key, text="", **kw):
    return view("LabelView", parent["_id"] + "/" + key, parent, text=text,
                height=20, **kw)


def embed(parent, name, child):
    parent.setdefault("subViews", []).append(child)
    parent[name] = ref(child)
    return child


def name_comp(parent, model, title, x, y, w, h=46):
    c = view("UMLNameCompartmentView", parent["_id"] + "/name", parent, model,
             left=x, top=y, width=w, height=h)
    embed(parent, "nameCompartment", c)
    for name in ["stereotypeLabel", "nameLabel", "namespaceLabel", "propertyLabel"]:
        l = label(c, name, title if name == "nameLabel" else "",
                  left=x+5, top=y+8, width=w-10, visible=name == "nameLabel")
        if name == "nameLabel":
            l["font"] = "Arial;15;1"
        embed(c, name, l)
    return c


def parse_java(path):
    raw = path.read_text(encoding="utf-8-sig")
    clean = re.sub(r"/\*.*?\*/|//[^\n]*", "", raw, flags=re.S)
    package = re.search(r"package\s+(\w+)\s*;", clean).group(1)
    decl = re.search(r"public\s+(abstract\s+)?(class|enum)\s+(\w+)(?:\s+extends\s+(\w+))?", clean)
    abstract, kind, name, base = decl.groups()
    info = {"name": name, "kind": kind, "package": package, "abstract": bool(abstract),
            "base": base, "path": path.relative_to(ROOT).as_posix(), "attributes": [], "operations": []}
    if kind == "enum":
        body = clean[clean.index("{", decl.end())+1:clean.rindex("}")]
        info["literals"] = re.findall(r"\b[A-Z][A-Z_]+\b", body)
    field_re = re.compile(r"^\s*(public|private|protected)\s+((?:(?:static|final)\s+)*)([\w<>?]+(?:\[\])*)\s+(\w+)\s*(?:=\s*([^;]*))?;", re.M)
    for a in field_re.finditer(clean):
        visibility, modifiers, typ, field_name, default = a.groups()
        info["attributes"].append({"name": field_name, "type": typ, "visibility": visibility,
            "static": "static" in modifiers, "final": "final" in modifiers,
            "default": re.sub(r"\s+", " ", default.strip()) if default else ""})
    meth_re = re.compile(r"^\s*(public|private|protected)\s+((?:(?:static|final|abstract)\s+)*)(?:(\w+(?:<[^\n]+?>)?(?:\[\])*)\s+)?(\w+)\s*\(([^)]*)\)\s*(?:\{|;)", re.M)
    for match in meth_re.finditer(clean):
        visibility, modifiers, typ, method_name, params = match.groups()
        params = re.sub(r"\s+", " ", params).strip()
        parsed_params = []
        if params:
            for p in re.split(r",\s*(?![^<]*>)", params):
                pt, pn = p.strip().rsplit(" ", 1)
                parsed_params.append((pn, pt))
        line = raw[:raw.find(method_name + "(")].count("\n") + 1
        # Multiline signatures need a name search independent of whitespace.
        actual = re.search(r"\b"+re.escape(method_name)+r"\s*\(", raw)
        if actual:
            line = raw[:actual.start()].count("\n")+1
        info["operations"].append({"name": method_name, "type": typ,
            "parameters": parsed_params, "visibility": visibility,
            "static": "static" in modifiers, "abstract": "abstract" in modifiers,
            "line": line})
    return info


def signatures(c):
    attrs, ops = [], []
    for a in c["attributes"]:
        default = a["default"]
        if len(default) > 55:
            default = "{ mapa de 15 × 15 }" if a["name"] == "MAPA_POR_DEFECTO" else "…"
        s = f'{VIS[a["visibility"]]} {a["name"]}: {a["type"]}'
        if default:
            s += " = " + default
        if a["final"]:
            s += " {readOnly}"
        attrs.append((s, a["static"], False))
    for o in c["operations"]:
        params = ", ".join(f"{pn}: {pt}" for pn, pt in o["parameters"])
        s = f'{VIS[o["visibility"]]} {o["name"]}({params})'
        if o["type"]:
            s += ": " + o["type"]
        ops.append((s, o["static"], o["abstract"]))
    return attrs, ops


def txt(x, y, text, size=15, **kw):
    props = " ".join(f'{k.replace("_", "-")}="{html.escape(str(v), quote=True)}"' for k, v in kw.items())
    return f'<text x="{x}" y="{y}" font-size="{size}" {props}>{html.escape(text)}</text>'


def svg_start(w, h, title):
    return [f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}" role="img" aria-label="{html.escape(title)}">',
      '<defs><marker id="arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="8" markerHeight="8" orient="auto-start-reverse"><path d="M0 0L10 5L0 10Z" fill="#334155"/></marker>'
      '<marker id="open" viewBox="0 0 12 12" refX="11" refY="6" markerWidth="9" markerHeight="9" orient="auto-start-reverse"><path d="M1 1L11 6L1 11" fill="none" stroke="#64748b" stroke-width="1.5"/></marker>'
      '<marker id="triangle" viewBox="0 0 14 14" refX="13" refY="7" markerWidth="15" markerHeight="15" orient="auto-start-reverse"><path d="M1 1L13 7L1 13Z" fill="white" stroke="#334155"/></marker>'
      '<marker id="diamond" viewBox="0 0 18 12" refX="1" refY="6" markerWidth="18" markerHeight="12" orient="auto"><path d="M1 6L9 1L17 6L9 11Z" fill="#334155" stroke="#334155"/></marker></defs>',
      f'<rect width="{w}" height="{h}" fill="#ffffff"/>',
      '<g font-family="Arial, sans-serif" fill="#0f172a">']


def svg_end(parts):
    return "\n".join(parts + ["</g></svg>"])


def caption(x, y, text, size=15, anchor="start", color="#0f172a"):
    """Fondo explícito: legible también en visores SVG sin CSS paint-order."""
    w = len(text)*size*0.54 + 10
    left = x-w/2 if anchor == "middle" else (x-w if anchor == "end" else x-4)
    return [f'<rect x="{left}" y="{y-size-2}" width="{w}" height="{size+7}" fill="white"/>',
            txt(x,y,text,size,text_anchor=anchor,fill=color)]


def arrow_shape(points, kind="arrow", start_diamond=False):
    """Puntas SVG reales, sin depender del soporte de marker de cada visor."""
    x,y=points[-1];px,py=next(p for p in reversed(points[:-1]) if p!=(x,y))
    dx,dy=x-px,y-py;length=math.hypot(dx,dy);dx/=length;dy/=length
    size=16 if kind=="triangle" else 10
    a=(x-size*dx+size*.45*dy,y-size*dy-size*.45*dx)
    b=(x-size*dx-size*.45*dy,y-size*dy+size*.45*dx)
    coords=f'{a[0]},{a[1]} {x},{y} {b[0]},{b[1]}'
    if kind=="open":
        result=[f'<polyline points="{coords}" fill="none" stroke="#334155" stroke-width="1.5"/>']
    else:
        result=[f'<polygon points="{coords}" fill="{"white" if kind=="triangle" else "#334155"}" stroke="#334155" stroke-width="1.5"/>']
    if start_diamond:
        x,y=points[0];px,py=next(p for p in points[1:] if p!=(x,y))
        dx,dy=px-x,py-y;length=math.hypot(dx,dy);dx/=length;dy/=length
        diamond=[(x,y),(x+9*dx+5*dy,y+9*dy-5*dx),(x+18*dx,y+18*dy),(x+9*dx-5*dy,y+9*dy+5*dx)]
        result.append(f'<polygon points="{" ".join(f"{a},{b}" for a,b in diamond)}" fill="#334155" stroke="#334155"/>')
    return result


def make_class_diagram(model, classes, models):
    diag = obj("UMLClassDiagram", "class/diagram", model, name="00 - Clases del código", defaultDiagram=True, ownedViews=[])
    model["ownedElements"].insert(0, diag)
    positions = {
        "JuegoConsola": (50, 180), "ControladorJuego": (50, 620),
        "Partida": (1200, 180), "Laberinto": (2580, 180),
        "Personaje": (1200, 1280), "Celda": (2580, 1120),
        "PacMan": (760, 1970), "Fantasma": (1560, 1970),
        "Punto": (2580, 1690), "PuntoComun": (2460, 2000), "SuperPunto": (2910, 2000),
        "EstadoPartida": (50, 1460), "EstadoFantasma": (50, 1740),
        "Direccion": (50, 2020), "Posicion": (50, 2310)}
    boxes, classviews = {}, {}
    parts = svg_start(3800, 2780, "Diagrama de clases completo")
    parts += [txt(50, 48, "PAC-MAN · CLASES DEL CÓDIGO", 30, font_weight="bold"),
              txt(50, 85, "15 clases y enumeraciones · atributos y operaciones declarados · Java 17", 19, fill="#475569"),
              txt(50, 116, "Herencia: triángulo vacío. Rombo negro: objetos propios. Asociación: referencia almacenada. Línea discontinua: uso o tipo.", 17, fill="#475569"),
              txt(50, 145, "+ público    − privado    # protegido    subrayado: static    cursiva: abstracto    {readOnly}: final", 17, fill="#475569")]
    for c in classes.values():
        name, kind = c["name"], c["kind"]
        attrs, ops = signatures(c)
        rows = [(lit, False, False) for lit in c.get("literals", [])] if kind == "enum" else attrs + ops
        w = max(320, max([len(s)*9+36 for s, _, _ in rows] + [len(name)*12+35]))
        w = math.ceil(w/10)*10
        h = 74 + len(rows)*24 + (22 if attrs and ops else 0)
        x, y = positions[name]
        boxes[name] = (x, y, w, h)
        v = view("UMLEnumerationView" if kind == "enum" else "UMLClassView", "class/view/"+name,
                 diag, models[name], left=x, top=y, width=w, height=h, showNamespace=False,
                 showVisibility=True, showOperationSignature=True, suppressReceptions=True,
                 suppressAttributes=False, suppressOperations=False)
        v["fillColor"] = COLORS[c["package"]]
        name_comp(v, models[name], name, x, y, w)
        for field, typ in [("attributeCompartment", "UMLAttributeCompartmentView"),
                           ("operationCompartment", "UMLOperationCompartmentView"),
                           ("receptionCompartment", "UMLReceptionCompartmentView"),
                           ("templateParameterCompartment", "UMLTemplateParameterCompartmentView")]:
            embed(v, field, view(typ, v["_id"]+"/"+field, v, models[name], left=x, top=y+46, width=w, height=24))
        if kind == "enum":
            embed(v, "enumerationLiteralCompartment", view("UMLEnumerationLiteralCompartmentView", v["_id"]+"/literals", v, models[name], left=x, top=y+46, width=w, height=h-46))
        diag["ownedViews"].append(v)
        classviews[name] = v
    relations = [
        ("JuegoConsola", "ControladorJuego", "association", "controlador", "1", "composite", "bottom"),
        ("ControladorJuego", "Partida", "association", "partida", "1", "none", "right"),
        ("Partida", "Laberinto", "association", "laberinto", "1", "none", "right"),
        ("Partida", "PacMan", "association", "pacMan", "1", "composite", "bottom"),
        ("Partida", "Fantasma", "association", "fantasmas", "4", "composite", "bottom"),
        ("Laberinto", "Celda", "association", "celdas", "1..*", "none", "bottom"),
        ("Celda", "Punto", "association", "punto", "0..1", "none", "bottom"),
        ("Celda", "Posicion", "association", "posicion", "0..1", "composite", "outer"),
        ("Personaje", "Posicion", "association", "posicion / posicionInicial", "2", "composite", "bottom"),
        ("PacMan", "Personaje", "inheritance", "", "", "none", "top"),
        ("Fantasma", "Personaje", "inheritance", "", "", "none", "top"),
        ("PuntoComun", "Punto", "inheritance", "", "", "none", "top"),
        ("SuperPunto", "Punto", "inheritance", "", "", "none", "top"),
        ("Partida", "EstadoPartida", "dependency", "tipo de estado", "", "none", "left"),
        ("Fantasma", "EstadoFantasma", "dependency", "tipo de estado", "", "none", "left"),
        ("Personaje", "Direccion", "dependency", "tipo de direccion", "", "none", "left"),
        ("Personaje", "Laberinto", "dependency", "calcularProximaPosicion", "", "none", "right"),
    ]
    # Aristas detrás de las cajas; los puntos también quedan guardados en MDJ.
    edge_svg = []
    for i, (a, b, kind, role, mult, agg, route) in enumerate(relations):
        ax, ay, aw, ah = boxes[a]; bx, by, bw, bh = boxes[b]
        if route == "right":
            sx, sy, ex, ey = ax+aw, ay+ah/2, bx, by+bh/2
            mid = (sx+ex)/2
            pts = [(sx,sy),(mid,sy),(mid,ey),(ex,ey)]
        elif route in ("bottom", "top"):
            if route == "bottom":
                sx, sy, ex, ey = ax+aw/2, ay+ah, bx+bw/2, by
            else:
                sx, sy, ex, ey = ax+aw/2, ay, bx+bw/2, by+bh
            # Partida owns the concrete characters; take a side channel around Personaje.
            if a == "Partida" and b in ("PacMan", "Fantasma"):
                sx = ax + (20 if b == "PacMan" else aw-20)
                channel = 710 if b == "PacMan" else 2400
                pts = [(sx,sy),(sx,1120),(channel,1120),(channel,1875),(ex,1875),(ex,ey)]
            elif a == "Personaje" and b == "Posicion":
                pts = [(sx,sy),(sx,1810),(620,1810),(620,2260),(ex,2260),(ex,ey)]
            else:
                mid = (sy+ey)/2
                pts = [(sx,sy),(sx,mid),(ex,mid),(ex,ey)]
        elif route == "outer":
            sx,sy,ex,ey = ax+aw,ay+ah/2,bx+bw/2,by+bh
            pts = [(sx,sy),(3690,sy),(3690,2680),(ex,2680),(ex,ey)]
        else:
            sx,sy,ex,ey = ax,ay+ah/2,bx+bw,by+bh/2
            channel = 1120 if a == "Partida" else (670 if a == "Fantasma" else 700)
            pts = [(sx,sy),(channel,sy),(channel,ey),(ex,ey)]
        key = f"class/relation/{i}"
        if kind == "association":
            rel = obj("UMLAssociation", key, models[a], name="")
            rel["end1"] = obj("UMLAssociationEnd", key+"/1", rel, reference=ref(models[a]),
                              multiplicity="", aggregation=agg, navigable="unspecified")
            rel["end2"] = obj("UMLAssociationEnd", key+"/2", rel, reference=ref(models[b]),
                              name=role, multiplicity=mult, navigable="navigable", aggregation="none")
            vt = "UMLAssociationView"
        else:
            rel = obj("UMLGeneralization" if kind == "inheritance" else "UMLDependency", key,
                      models[a], source=ref(models[a]), target=ref(models[b]),
                      name=role if kind == "dependency" else "")
            vt = "UMLGeneralizationView" if kind == "inheritance" else "UMLDependencyView"
        models[a].setdefault("ownedElements", []).append(rel)
        v = view(vt, key+"/view", diag, rel, tail=ref(classviews[a]), head=ref(classviews[b]),
                 points=";".join(f"{int(x)}:{int(y)}" for x,y in pts), lineStyle=1)
        diag["ownedViews"].append(v)
        dash = ' stroke-dasharray="9 6"' if kind == "dependency" else ""
        edge_svg.append(f'<polyline points="{" ".join(f"{x},{y}" for x,y in pts)}" fill="none" stroke="#64748b" stroke-width="1.8"{dash}/>')
        edge_svg += arrow_shape(pts,"triangle" if kind=="inheritance" else "open",agg=="composite")
        if role:
            lx, ly = pts[-2]
            edge_svg += caption(lx+10, ly-10, role+(" ["+mult+"]" if mult else ""),14,color="#334155")
    parts += edge_svg
    for c in classes.values():
        x,y,w,h = boxes[c["name"]]
        attrs, ops = signatures(c)
        rows = [(t,False,False) for t in c.get("literals", [])] if c["kind"] == "enum" else attrs+ops
        parts += [f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="4" fill="white" stroke="#334155" stroke-width="1.7"/>',
                  f'<path d="M{x} {y+60}H{x+w}V{y}H{x}Z" fill="{COLORS[c["package"]]}"/>',
                  txt(x+14,y+22,c["package"]+(" · «enumeration»" if c["kind"] == "enum" else ""),13,fill="#475569"),
                  txt(x+14,y+46,c["name"],20,font_weight="bold",font_style="italic" if c["abstract"] else "normal"),
                  f'<path d="M{x} {y+60}H{x+w}" stroke="#334155"/>']
        yy=y+84
        for j,(text,static,abstract) in enumerate(rows):
            if c["kind"] != "enum" and attrs and ops and j == len(attrs):
                parts.append(f'<path d="M{x} {yy-13}H{x+w}" stroke="#94a3b8"/>')
                yy += 22
            parts.append(txt(x+14,yy,text,15,font_family="Courier New",
                             text_decoration="underline" if static else "none",font_style="italic" if abstract else "normal"))
            yy += 24
    parts += [txt(2450,2340,"Alcance y detalles",22,font_weight="bold"),
              txt(2450,2380,"Se muestran las clases de la aplicación, sin las pruebas ni tipos de la biblioteca Java.",16),
              txt(2450,2410,"Operaciones heredadas se consultan en Personaje y Punto, sin duplicarlas en subclases.",16),
              txt(2450,2440,"Celda() permite posicion y punto nulos; por eso sus multiplicidades son 0..1.",16),
              txt(2450,2470,"Laberinto y Partida aceptan objetos externos: no se presupone composición exclusiva.",16),
              txt(2450,2500,"Personaje guarda dos Posicion: posicion y posicionInicial (dos referencias propias).",16)]
    for j, text in enumerate(["Código fuente en src/; secuencias R01–R10 derivadas de los mismos métodos.",
                              "Diagrama elaborado con asistencia de IA · 30/10/2026. No es un original docente."]):
        parts.append(txt(50,2710+j*28,text,17,fill="#475569"))
    return diag, svg_end(parts), boxes


def text_box(diag, key, text, x,y,w,h, title=False):
    return view("UMLTextView" if title else "UMLTextBoxView", key, diag,
                text=text, left=x,top=y,width=w,height=h,wordWrap=True,
                **({} if title else {"lineColor":"#cbd5e1"}))


def step_layout(steps, start=0, depth=0):
    result=[]; y=start
    for step in steps:
        s=dict(step); s["y"]=y; s["depth"]=depth
        if s["kind"] == "fragment":
            branches=[]; yy=y+30
            for branch in s["branches"]:
                # Guards are single line at the allocated diagram width.
                sub, end=step_layout(branch["steps"], yy+36, depth+1)
                branches.append({**branch,"y":yy,"height":end+18-yy,"steps":sub})
                yy=end+18
            s["branches"]=branches; s["height"]=yy+12-y
        elif s["kind"] == "note":
            s["lines"]=textwrap.wrap(s["text"], 124, break_long_words=False)
            s["height"]=len(s["lines"])*22+30
        else:
            s["height"]=80 if s["from"]==s["to"] else 64
        result.append(s);y+=s["height"]
    return result,y


def make_sequence(seq, parent, classes, models, opmodels):
    collab=obj("UMLCollaboration", seq["id"]+"/collab", parent, name=seq["id"]+" - "+seq["name"], attributes=[], ownedElements=[])
    inter=obj("UMLInteraction",seq["id"]+"/interaction",collab,name=seq["name"],participants=[],messages=[],fragments=[],ownedElements=[])
    collab["ownedElements"].append(inter); parent["ownedElements"].append(collab)
    diag=obj("UMLSequenceDiagram",seq["id"]+"/diagram",inter,name=seq["id"]+" - "+seq["name"],
             ownedViews=[],showSequenceNumber=False,showActivation=False,showSignature=True)
    inter["ownedElements"].append(diag)
    steps,end=step_layout(seq["steps"],230)
    count=len(seq["participants"]); gap=360; width=max(1420, count*gap+100); height=end+100
    xpos={key:180+i*gap for i,(key,_) in enumerate(seq["participants"])}
    lives,lines={},{}
    svg=svg_start(width,height,diag["name"])
    svg += [txt(40,46,seq["id"]+" · "+seq["name"].upper(),27,font_weight="bold"),
            txt(40,80,"Llamadas reales del código · notas para asignaciones y partes omitidas · lectura de arriba hacia abajo",16,fill="#475569")]
    diag["ownedViews"].append(text_box(diag,seq["id"]+"/title",diag["name"],30,20,width-60,46,True))
    for key,typ in seq["participants"]:
        role=obj("UMLAttribute",seq["id"]+"/role/"+key,collab,name=key,type=ref(models[typ]) if typ in models else typ)
        collab["attributes"].append(role)
        life=obj("UMLLifeline",seq["id"]+"/life/"+key,inter,name=key,represent=ref(role),isMultiInstance=False)
        inter["participants"].append(life); lives[key]=life
        x=xpos[key];v=view("UMLSeqLifelineView",seq["id"]+"/lifeview/"+key,diag,life,
                           left=x-135,top=120,width=270,height=height-175,showType=True,showNamespace=False,showVisibility=False)
        name_comp(v,life,key+":"+typ,x-135,120,270,52)
        lp=view("UMLLinePartView",seq["id"]+"/line/"+key,v,life,left=x,top=172,width=1,height=height-227)
        embed(v,"linePart",lp);lines[key]=lp
        diag["ownedViews"].append(v)
        svg += [f'<rect x="{x-135}" y="120" width="270" height="52" rx="4" fill="{COLORS.get(classes.get(typ,{}).get("package"),"#f1f5f9")}" stroke="#334155"/>',
                txt(x,152,key+":"+typ,17,text_anchor="middle",font_weight="bold"),
                f'<path d="M{x} 172V{height-55}" stroke="#94a3b8" stroke-dasharray="7 6"/>']
    message_no=0

    def emit(items):
        nonlocal message_no, svg
        for s in items:
            y=s["y"];d=s["depth"];key=f'{seq["id"]}/step/{y}'
            if s["kind"] == "note":
                x=48+d*18;w=width-2*x;hh=s["height"]-14
                # White notes cover lifelines and leave room before/after messages.
                svg.append(f'<rect x="{x}" y="{y}" width="{w}" height="{hh}" rx="4" fill="#f8fafc" stroke="#cbd5e1"/>')
                for i,line in enumerate(s["lines"]):
                    svg.append(txt(x+14,y+24+i*22,line,16,fill="#475569"))
                nv=text_box(diag,key,s["text"],x,y,w,hh);nv["fillColor"]="#f8fafc"
                diag["ownedViews"].append(nv)
            elif s["kind"] == "fragment":
                x=30+d*17;w=width-2*x
                frag=obj("UMLCombinedFragment",key,inter,name="",interactionOperator=s["op"],operands=[])
                inter["fragments"].append(frag)
                fv=view("UMLCombinedFragmentView",key+"/view",diag,frag,left=x,top=y,width=w,height=s["height"],zIndex=2)
                embed(fv,"nameLabel",label(fv,"nameLabel",visible=False,left=x+65,top=y+4,width=20))
                embed(fv,"frameTypeLabel",label(fv,"frameTypeLabel",s["op"],left=x+8,top=y+4,width=54))
                comp=view("UMLInteractionOperandCompartmentView",key+"/comp",fv,frag,left=x,top=y+30,width=w,height=s["height"]-30,subViews=[])
                embed(fv,"operandCompartment",comp)
                svg += [f'<rect x="{x}" y="{y}" width="{w}" height="{s["height"]}" fill="none" stroke="#64748b" stroke-width="1.5"/>',
                        f'<path d="M{x} {y}h65v22l-10 8h-55z" fill="#e2e8f0" stroke="#64748b"/>',
                        txt(x+10,y+21,s["op"],16,font_weight="bold")]
                for i,b in enumerate(s["branches"]):
                    operand=obj("UMLInteractionOperand",key+f"/operand/{i}",frag,name="",guard=b["guard"])
                    frag["operands"].append(operand)
                    ov=view("UMLInteractionOperandView",key+f"/operandview/{i}",comp,operand,left=x,top=b["y"],width=w,height=b["height"])
                    embed(ov,"guardLabel",label(ov,"guardLabel","["+b["guard"]+"]",left=x+10,top=b["y"]+3,width=w-20))
                    comp["subViews"].append(ov)
                    if i:
                        svg.append(f'<path d="M{x} {b["y"]}H{x+w}" stroke="#94a3b8" stroke-dasharray="7 5"/>')
                    svg += caption(x+12,b["y"]+23,"["+b["guard"]+"]",15,color="#334155")
                diag["ownedViews"].append(fv)
                for b in s["branches"]:
                    emit(b["steps"])
            else:
                message_no+=1
                a,b=s["from"],s["to"];sx,ex=xpos[a],xpos[b];yy=y+33
                is_reply=s["kind"]=="return"
                msg=obj("UMLMessage",key,inter,name=s["text"],source=ref(lives[a]),target=ref(lives[b]),
                        messageSort="reply" if is_reply else "synchCall")
                target_type=dict(seq["participants"])[b]
                if not is_reply:
                    method=s["text"].split("(")[0]
                    matches=find_method(classes,target_type,method)
                    if target_type != "Random":
                        assert matches, f'{seq["id"]}: no existe {target_type}.{method}'
                        op=matches[0]
                        # Match the overload by arity when possible.
                        inside=s["text"][s["text"].find("(")+1:s["text"].rfind(")")]
                        arity=0 if not inside.strip() else len(inside.split(","))
                        op=next((o for o in matches if len(o[1]["parameters"])==arity),op)
                        msg["signature"]=ref(opmodels[(op[0],op[1]["name"],tuple(op[1]["parameters"]))])
                        msg["name"]=method
                        msg["arguments"]=inside
                inter["messages"].append(msg)
                pts=[(sx,yy),(ex,yy)] if a!=b else [(sx,yy),(sx+82,yy),(sx+82,yy+22),(sx,yy+22)]
                mv=view("UMLSeqMessageView",key+"/view",diag,msg,head=ref(lines[b]),tail=ref(lines[a]),
                        points=";".join(f"{x}:{y}" for x,y in pts),lineStyle=1,showProperty=False,showType=False)
                for field in ("nameLabel","stereotypeLabel","propertyLabel"):
                    el=view("EdgeLabelView",key+"/"+field,mv,visible=field=="nameLabel",hostEdge=ref(mv),
                            edgePosition=1,alpha=math.pi/2,distance=12,height=20)
                    embed(mv,field,el)
                embed(mv,"activation",view("UMLActivationView",key+"/activation",mv,visible=False,left=ex-7,top=yy,width=14,height=20))
                diag["ownedViews"].append(mv)
                dash=' stroke-dasharray="6 5"' if is_reply else ""
                svg.append(f'<polyline points="{" ".join(f"{x},{y}" for x,y in pts)}" stroke="#334155" fill="none" stroke-width="1.5"{dash}/>')
                svg += arrow_shape(pts,"open" if is_reply else "arrow")
                if a==b:
                    # Long self calls go to the left of a rightmost participant.
                    tx=sx+15 if sx < width-400 else sx-15
                    anchor="start" if sx < width-400 else "end"
                else:
                    tx=(sx+ex)/2;anchor="middle"
                svg += caption(tx,yy-10,s["text"],15,anchor,"#475569" if is_reply else "#0f172a")
    emit(steps)
    svg.append(txt(40,height-20,"Código: src/ · R01–R10 según Requerimientos TPO PacMan 1.md · Elaboración asistida por IA",14,fill="#64748b"))
    return diag,svg_end(svg),{"width":width,"height":height,"messages":message_no}


def find_method(classes, typ, name):
    if typ not in classes:
        return []
    c=classes[typ]
    found=[(typ,o) for o in c["operations"] if o["name"]==name]
    return found or find_method(classes,c["base"],name)


def build_viewer(diagrams, commit):
    content=[]
    for key,title,svg,details in diagrams:
        content.append({"id":key,"title":title,"svg":svg,"details":details})
    data=json.dumps(content,ensure_ascii=False).replace("</","<\\/")
    return '''<!doctype html><html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Pac-Man · Diagramas del código</title><style>
*{box-sizing:border-box}body{margin:0;background:#f1f5f9;color:#0f172a;font:15px system-ui,Arial}header{background:#0f172a;color:white;padding:19px 28px;display:flex;justify-content:space-between;align-items:center}h1{font-size:22px;margin:0 0 5px}header p{margin:0;color:#cbd5e1}main{display:grid;grid-template-columns:245px 1fr;height:calc(100vh - 92px)}aside{background:white;border-right:1px solid #cbd5e1;padding:16px;overflow:auto}aside button{display:block;width:100%;text-align:left;border:0;padding:11px 12px;margin:3px 0;background:white;color:#334155;border-radius:7px;font:inherit;cursor:pointer}aside button.active{background:#dbeafe;color:#1e40af;font-weight:650}aside small{display:block;padding:14px 8px;line-height:1.6;color:#64748b}.content{min-width:0;display:flex;flex-direction:column}.tools{display:flex;gap:8px;align-items:center;padding:13px 18px;background:white;border-bottom:1px solid #cbd5e1;flex-wrap:wrap}.tools h2{font-size:17px;margin:0 auto 0 0}.tools button,.tools a{padding:8px 11px;background:white;border:1px solid #cbd5e1;border-radius:6px;color:#0f172a;text-decoration:none;cursor:pointer;font:inherit}#zoom{width:58px;text-align:center;font-variant-numeric:tabular-nums}.viewport{overflow:auto;flex:1;padding:24px;min-height:200px}#canvas{background:white;box-shadow:0 2px 12px #0f172a12;width:max-content}#canvas svg{display:block}details{padding:12px 20px;background:#fff;border-top:1px solid #cbd5e1;max-height:210px;overflow:auto}summary{cursor:pointer;font-weight:600}details p{line-height:1.5;color:#475569;margin-bottom:0}code{font-family:Consolas,monospace}button:focus-visible,a:focus-visible{outline:3px solid #60a5fa} @media(max-width:800px){main{grid-template-columns:1fr;height:auto}aside{display:flex;overflow:auto;padding:8px}aside button{min-width:160px}aside small{display:none}.viewport{height:72vh;flex:none}header{padding:15px}header small{display:none}}
</style></head><body><header><div><h1>Pac-Man · Diagramas del código</h1><p>Un diagrama de clases y diez secuencias</p></div><small>develop · COMMIT · 30/10/2026</small></header>
<main><aside><nav id="nav" aria-label="Diagramas"></nav><small>Sin conexión ni instalaciones.<br>Elegí un diagrama y usá el zoom.<br>Las notas aclaran asignaciones y llamadas omitidas.</small></aside><section class="content"><div class="tools"><h2 id="title"></h2><button id="less" aria-label="Reducir zoom">−</button><output id="zoom"></output><button id="more" aria-label="Aumentar zoom">+</button><button id="fit">Ajustar ancho</button><button id="actual">100%</button><a id="download" download>Descargar SVG</a></div><div class="viewport" id="viewport"><div id="canvas"></div></div><details><summary>Fuentes y alcance de este diagrama</summary><p id="details"></p></details></section></main>
<script>const diagrams=DATA;let selected=0,scale=1,blobUrl;const $=s=>document.querySelector(s);const nav=$('#nav');diagrams.forEach((d,i)=>{const b=document.createElement('button');b.textContent=d.title;b.onclick=()=>select(i);nav.append(b)});function resize(){const svg=$('#canvas svg');svg.style.width=svg.viewBox.baseVal.width*scale+'px';svg.style.height=svg.viewBox.baseVal.height*scale+'px';$('#zoom').textContent=Math.round(scale*100)+'%'}function fit(){scale=Math.max(.15,Math.min(1,($('#viewport').clientWidth-48)/$('#canvas svg').viewBox.baseVal.width));resize()}function select(i){selected=i;const d=diagrams[i];$('#title').textContent=d.title;$('#canvas').innerHTML=d.svg;$('#details').textContent=d.details;[...nav.children].forEach((b,j)=>b.classList.toggle('active',i===j));if(blobUrl)URL.revokeObjectURL(blobUrl);blobUrl=URL.createObjectURL(new Blob([d.svg],{type:'image/svg+xml;charset=utf-8'}));$('#download').href=blobUrl;$('#download').download=d.id+'.svg';$('#viewport').scrollTo(0,0);location.hash=d.id;fit()}$('#less').onclick=()=>{scale=Math.max(.15,scale/1.2);resize()};$('#more').onclick=()=>{scale=Math.min(3,scale*1.2);resize()};$('#fit').onclick=fit;$('#actual').onclick=()=>{scale=1;resize()};const hash=location.hash.slice(1);select(Math.max(0,diagrams.findIndex(d=>d.id===hash)));</script></body></html>'''.replace("COMMIT",commit[:7]).replace("DATA",data)


def validate_refs(project):
    ids={};refs=[]
    def scan(x):
        if isinstance(x,dict):
            if "_id" in x:
                assert x["_id"] not in ids, "Identificador repetido"
                ids[x["_id"]]=x
            if "$ref" in x:
                refs.append(x["$ref"])
            for v in x.values():scan(v)
        elif isinstance(x,list):
            for v in x:scan(v)
    scan(project)
    assert all(r in ids for r in refs), sorted(set(refs)-ids.keys())
    assert sum(x["_type"]=="UMLClassDiagram" for x in ids.values())==1
    assert sum(x["_type"]=="UMLSequenceDiagram" for x in ids.values())==10
    return len(ids),len(refs)


def main():
    files=sorted(p for p in SRC.rglob("*.java") if "test" not in p.relative_to(SRC).parts)
    classes={c["name"]:c for c in map(parse_java,files)}
    assert len(classes)==15
    project=obj("Project","project",name="Pac-Man - UML derivado del código",documentation="Elaboración asistida por IA, 30/10/2026. Fuentes: src/ y requerimientos escritos del proyecto. No se reutilizó la lógica de diagramas previos.",ownedElements=[])
    model=obj("UMLModel","model",project,name="Aplicación",ownedElements=[])
    project["ownedElements"].append(model)
    packages={}
    for p in ("consola","controlador","modelo","extra"):
        packages[p]=obj("UMLPackage","package/"+p,model,name=p,ownedElements=[])
        model["ownedElements"].append(packages[p])
    models={};opmodels={}
    for c in classes.values():
        cl=obj("UMLEnumeration" if c["kind"]=="enum" else "UMLClass","type/"+c["name"],packages[c["package"]],
               name=c["name"],documentation="Fuente: "+c["path"],isAbstract=c["abstract"],attributes=[],operations=[])
        packages[c["package"]]["ownedElements"].append(cl);models[c["name"]]=cl
    for c in classes.values():
        cl=models[c["name"]]
        for i,a in enumerate(c["attributes"]):
            default=a["default"] if len(a["default"])<=55 else ""
            ua=obj("UMLAttribute",cl["_id"]+f"/attr/{i}",cl,name=a["name"],type=ref(models[a["type"]]) if a["type"] in models else a["type"],
                   visibility=a["visibility"],isStatic=a["static"],isReadOnly=a["final"],defaultValue=default,
                   documentation="Inicializador Java: "+a["default"] if a["default"] else "")
            cl["attributes"].append(ua)
        for i,o in enumerate(c["operations"]):
            uo=obj("UMLOperation",cl["_id"]+f"/op/{i}",cl,name=o["name"],visibility=o["visibility"],isStatic=o["static"],
                   isAbstract=o["abstract"],parameters=[])
            for j,(pn,pt) in enumerate(o["parameters"]):
                uo["parameters"].append(obj("UMLParameter",uo["_id"]+f"/p/{j}",uo,name=pn,type=ref(models[pt]) if pt in models else pt))
            if o["type"]:
                uo["parameters"].append(obj("UMLParameter",uo["_id"]+"/return",uo,name="",type=ref(models[o["type"]]) if o["type"] in models else o["type"],direction="return"))
            cl["operations"].append(uo);opmodels[(c["name"],o["name"],tuple(o["parameters"]))]=uo
        if c["kind"]=="enum":
            cl["literals"]=[obj("UMLEnumerationLiteral",cl["_id"]+f"/lit/{i}",cl,name=lit) for i,lit in enumerate(c["literals"])]
    diag,svg,boxes=make_class_diagram(model,classes,models)
    svgs=HERE/"svg";svgs.mkdir(exist_ok=True)
    (svgs/"Clases.svg").write_text(svg,encoding="utf-8")
    diagrams=[("Clases","Clases · aplicación completa",svg,
        "Fuente: las 15 clases/enumeraciones de src/modelo, src/extra, src/controlador y src/consola. Se muestran todos los atributos y operaciones declarados, constructores, visibilidades, static, final y abstract. Las operaciones heredadas aparecen en su clase base. Los tests y las clases de biblioteca Java no son parte del modelo de la aplicación. Las asociaciones representan referencias almacenadas; las dependencias principales y los tipos enum se dibujan con línea discontinua. Los demás usos de tipos se conservan en las firmas para no saturar el gráfico.")]
    sequences=obj("UMLPackage","sequences",project,name="Secuencias por requerimiento",ownedElements=[])
    project["ownedElements"].append(sequences)
    stats={}
    for seq in SEQUENCES:
        for source in seq["sources"]:
            typ,meth=source.split(".")
            assert find_method(classes,typ,meth),source
        dg,svg,st=make_sequence(seq,sequences,classes,models,opmodels)
        stats[seq["id"]]=st
        (svgs/(seq["id"]+".svg")).write_text(svg,encoding="utf-8")
        details="Fuentes: "+", ".join(seq["sources"])+". Las flechas son llamadas o retornos; las cajas de texto son operaciones locales o resúmenes. Los fragmentos alt/opt/loop corresponden a condiciones y repeticiones del código. Las partes omitidas de actualizar() se indican expresamente. No se añaden métodos de diseño."
        diagrams.append((seq["id"],seq["id"]+" · "+seq["name"],svg,details))
    nids,nrefs=validate_refs(project)
    commit=subprocess.check_output(["git","-C",str(ROOT),"rev-parse","HEAD"],text=True).strip()
    (HERE/"Pacman-desde-codigo.mdj").write_text(json.dumps(project,ensure_ascii=False,indent=2),encoding="utf-8")
    (HERE/"Ver-diagramas.html").write_text(build_viewer(diagrams,commit),encoding="utf-8")
    manifest={"fecha":"2026-10-30","rama":"develop","commit_base":commit,"autoria":"Elaboración asistida por IA a pedido de Juan",
              "fuentes":{p.relative_to(ROOT).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in files},
              "clases":{c["name"]:{"atributos":len(c["attributes"]),"operaciones":len(c["operations"]),"origen":c["path"]} for c in classes.values()},
              "diagramas":stats,"validacion":{"identificadores_unicos":nids,"referencias_resueltas":nrefs,"clases_y_enums":len(classes),"secuencias":10}}
    (HERE/"fuentes-y-validacion.json").write_text(json.dumps(manifest,ensure_ascii=False,indent=2),encoding="utf-8")
    with zipfile.ZipFile(HERE/"Pacman-diagramas.zip","w",compression=zipfile.ZIP_DEFLATED) as z:
        names=["Pacman-desde-codigo.mdj","Ver-diagramas.html","README.md","generar.py","secuencias.py","fuentes-y-validacion.json"]
        for name in names:
            z.write(HERE/name,name)
        for p in sorted(svgs.glob("*.svg")):
            z.write(p,p.relative_to(HERE).as_posix())
    print(json.dumps({"clases":len(classes),"operaciones":sum(len(c["operations"]) for c in classes.values()),"secuencias":10,"objetos":nids,"referencias":nrefs,"mensajes":sum(s["messages"] for s in stats.values())}))


if __name__ == "__main__":
    main()
