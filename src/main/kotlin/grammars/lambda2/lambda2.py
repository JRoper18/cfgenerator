from collections.abc import Iterable, Iterator
from functools import reduce
def canBeList(x):
    return isinstance(x, Iterable)

def expand_iters(e):
    if(canBeList(e)): # Expand it out.
        return [expand_iters(ele) for ele in e]
    return e
def mapt(f, x):
    if(len(x) == 0):
        return f(x)
    # TODO: this
    return x
def cons(a, b):
    blist = canBeList(b)
    alist = canBeList(a)
    al = expand_iters(a) if alist else [a]
    bl = expand_iters(b) if blist else [b]
    return al + bl

foldl = lambda f, acc, xs: reduce(f, xs, acc)
foldr = lambda f, acc, xs: reduce(lambda x, y: f(y, x), xs[::-1], acc)
def recl(f, e, x):
    if(len(x) == 0):
        return e
    return f(x[0], x[1:])

