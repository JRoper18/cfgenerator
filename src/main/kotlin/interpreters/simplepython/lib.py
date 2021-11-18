from collections.abc import Iterable, Iterator
from functools import reduce
import ast
def canBeList(x):
    return isinstance(x, Iterable)
def hasSyntaxErr(progStr):
    try:
        ast.parse(str(progStr))
        print("false")
    except SyntaxError:
        print("true")

def expand_iters(e):
    if(canBeList(e)): # Expand it out.
        return [expand_iters(ele) for ele in e]
    return e

def cons(a, b):
    blist = canBeList(b)
    if not blist:
        # Can't cons something to a not-list
        return None
    alist = canBeList(a)
    al = expand_iters(a) if alist else a
    bl = expand_iters(b) if blist else b
    if b == []:
        return [a]
    assert type(a) is type(b[0])
    bl.insert(0, al)
    return bl

def concat(a, b):
    if b == []:
        return a
    if a == []:
        return b
    assert type(a) is type(b)
    for ai in a:
        for bi in b:
            assert type(ai) is type(bi)
    assert canBeList(a)
    assert canBeList(b)
    return expand_iters(a) + expand_iters(b)

def insert(idx, item, list):
    assert canBeList(list)
    expanded = expand_iters(list)
    for ai in expanded:
        assert type(ai) is type(item)
    return expanded[:idx] + [item] + expanded[idx:]

foldl = lambda f, acc, xs: reduce(f, xs, acc)
foldr = lambda f, acc, xs: reduce(lambda x, y: f(y, x), xs[::-1], acc)
def recl(f, e, x):
    if(len(x) == 0):
        return e
    return f(x[0], x[1:])

